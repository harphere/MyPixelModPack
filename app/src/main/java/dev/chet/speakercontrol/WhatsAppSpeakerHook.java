package dev.chet.speakercontrol;

import android.content.*;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.atomic.AtomicBoolean;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/** Runs inside WhatsApp's audio-owning process; no dependency on obfuscated UI classes. */
public final class WhatsAppSpeakerHook {
    private static final AtomicBoolean INSTALLED = new AtomicBoolean();
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static volatile AudioManager owner;
    private static volatile boolean communicating;
    private static volatile boolean requestedByPack;
    private static volatile long generation;
    static boolean isHost(String pkg) { return "com.whatsapp".equals(pkg) || "com.whatsapp.w4b".equals(pkg); }
    static int desiredType(String command, int currentType) {
        if ("ENABLE".equals(command)) return AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
        if ("DISABLE".equals(command)) return AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
        if ("TOGGLE".equals(command)) return currentType == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                ? AudioDeviceInfo.TYPE_BUILTIN_EARPIECE : AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
        throw new IllegalArgumentException("Unsupported speaker command: " + command);
    }
    public static void install(Context app) {
        if (!INSTALLED.compareAndSet(false,true)) return;
        try {
            XposedHelpers.findAndHookMethod(AudioManager.class,"setMode",int.class,new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam hook) {
                    if (hook.hasThrowable()) return;
                    AudioManager manager=(AudioManager)hook.thisObject;
                    int mode=(Integer)hook.args[0];
                    owner=manager; communicating=mode==AudioManager.MODE_IN_COMMUNICATION;
                    generation++;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (communicating) register(app);
                        else if (requestedByPack) {
                            requestedByPack=false;
                            try { manager.clearCommunicationDevice(); }
                            catch (RuntimeException error) { XposedBridge.log("SpeakerControl: WhatsApp route cleanup: "+error); }
                        }
                    });
                }
            });
            XposedBridge.log("SpeakerControl: WhatsApp audio-mode hook ready in "+android.os.Process.myPid()+"; waiting for communication audio");
        } catch(Throwable error) { INSTALLED.set(false); XposedBridge.log("SpeakerControl: WhatsApp hook failed: "+error); }
    }
    private static boolean active(AudioManager manager) {
        return communicating && owner==manager && manager.getMode()==AudioManager.MODE_IN_COMMUNICATION;
    }
    private static int route(AudioDeviceInfo device) {
        if(device==null)return -1;
        switch(device.getType()) {
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:return android.telecom.CallAudioState.ROUTE_SPEAKER;
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:return android.telecom.CallAudioState.ROUTE_EARPIECE;
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
            case AudioDeviceInfo.TYPE_BLE_HEADSET:return android.telecom.CallAudioState.ROUTE_BLUETOOTH;
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
            case AudioDeviceInfo.TYPE_USB_HEADSET:return android.telecom.CallAudioState.ROUTE_WIRED_HEADSET;
            default:return -1;
        }
    }
    private static void register(Context app) {
        if(!REGISTERED.compareAndSet(false,true))return;
        try {
            IntentFilter filter=new IntentFilter();
            for(String action:new String[]{"ENABLE","DISABLE","TOGGLE","STATUS"})filter.addAction(Protocol.PREFIX+action);
            app.registerReceiver(new BroadcastReceiver(){
                @Override public void onReceive(Context context,Intent intent) {
                    if(!Protocol.TOKEN.equals(intent.getStringExtra(Protocol.KEY)))return;
                    AudioManager manager=owner;
                    // Ignore dormant processes so they cannot compete with the call owner.
                    if(manager==null || !active(manager))return;
                    String action=intent.getAction();
                    if(action==null || !action.startsWith(Protocol.PREFIX))return;
                    String command=action.substring(Protocol.PREFIX.length());
                    String reply=Protocol.TASKER.equals(intent.getStringExtra("reply_package"))?Protocol.TASKER:Protocol.APP;
                    execute(context,manager,command,reply,intent.getStringExtra("request_id"));
                }
            },filter,null,new Handler(Looper.getMainLooper()),Context.RECEIVER_EXPORTED);
            XposedBridge.log("SpeakerControl: WhatsApp command receiver ready in "+android.os.Process.myPid());
        }catch(Throwable error){REGISTERED.set(false);XposedBridge.log("SpeakerControl: WhatsApp receiver failed: "+error);}
    }
    private static void execute(Context context,AudioManager manager,String command,String reply,String request) {
        try {
            AudioDeviceInfo before=manager.getCommunicationDevice();
            if("STATUS".equals(command)) {
                SpeakerHook.respond(context,reply,command,request,true,true,route(before),"WhatsApp communication audio active; route="+(before==null?"unknown":before.getType()));return;
            }
            int type=desiredType(command,before==null?-1:before.getType());
            AudioDeviceInfo desired=null;
            for(AudioDeviceInfo device:manager.getAvailableCommunicationDevices())if(device.getType()==type){desired=device;break;}
            if(desired==null){SpeakerHook.respond(context,reply,command,request,false,true,route(before),"Requested WhatsApp audio device unavailable; no route change made.");return;}
            long attempt=++generation;
            if(!manager.setCommunicationDevice(desired)){SpeakerHook.respond(context,reply,command,request,false,true,route(before),"Android rejected the WhatsApp route request.");return;}
            requestedByPack=true;
            verify(context,manager,desired.getId(),attempt,command,reply,request,0);
        }catch(Throwable error){SpeakerHook.respond(context,reply,command,request,false,false,-1,"WhatsApp audio error: "+error);}
    }
    private static void verify(Context context,AudioManager manager,int desiredId,long attempt,String command,String reply,String request,int retry) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if(attempt!=generation || !active(manager)){SpeakerHook.respond(context,reply,command,request,false,false,-1,"WhatsApp call audio ended or another route command superseded this request.");return;}
                AudioDeviceInfo observed=manager.getCommunicationDevice();
                boolean ok=observed!=null && observed.getId()==desiredId;
                if(!ok && retry<3){verify(context,manager,desiredId,attempt,command,reply,request,retry+1);return;}
                SpeakerHook.respond(context,reply,command,request,ok,true,route(observed),ok?"WhatsApp audio route verified.":"WhatsApp/Android did not retain the requested route.");
            }catch(Throwable error){SpeakerHook.respond(context,reply,command,request,false,false,-1,"WhatsApp verification error: "+error);}
        },300);
    }
}
