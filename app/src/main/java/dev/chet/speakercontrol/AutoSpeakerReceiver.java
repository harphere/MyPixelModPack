package dev.chet.speakercontrol;

import android.content.*;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Short-lived status discovery, followed by exactly one targeted command. */
public final class AutoSpeakerReceiver extends BroadcastReceiver {
    private static final AtomicBoolean BUSY=new AtomicBoolean();
    @Override public void onReceive(Context context,Intent intent) {
        if(intent==null || !Protocol.TOKEN.equals(intent.getStringExtra(Protocol.KEY)))return;
        String action=intent.getAction();
        if(action==null || !action.startsWith(Protocol.PREFIX))return;
        String command=action.substring(Protocol.PREFIX.length());
        if(!List.of("ENABLE","DISABLE","TOGGLE","STATUS").contains(command))return;
        String reply=Protocol.TASKER.equals(intent.getStringExtra("reply_package"))?Protocol.TASKER:Protocol.APP;
        String request=intent.getStringExtra("request_id");
        if(!context.getSharedPreferences("features_v2",0).getBoolean("speaker",false)) {
            failure(context,reply,request,command,"Enable SpeakerControl in the Pack first.");return;
        }
        if(!BUSY.compareAndSet(false,true)){failure(context,reply,request,command,"Another Auto speaker request is in progress; retry shortly.");return;}
        PendingResult pending=goAsync();
        new Session(context.getApplicationContext(),pending,reply,request,command).start();
    }
    private static void failure(Context c,String reply,String request,String command,String message) {
        c.sendBroadcast(new Intent(Protocol.PREFIX+"RESULT").setPackage(reply).putExtra(Protocol.KEY,Protocol.TOKEN)
            .putExtra("request_id",request).putExtra("command",command).putExtra("success",false)
            .putExtra("call_active",false).putExtra("route",-1).putExtra("speaker_on",false)
            .putExtra("backend_package",Protocol.APP).putExtra("status",message));
    }
    static final class Session extends BroadcastReceiver {
        final Context c;final PendingResult pending;final String reply,request,command;
        final Handler handler=new Handler(Looper.getMainLooper());
        final String probe=UUID.randomUUID().toString(),execution=UUID.randomUUID().toString();
        final Set<String> targets=new LinkedHashSet<>();final Map<String,Intent> active=new LinkedHashMap<>();
        String selected;boolean registered,finished,dispatched;
        Session(Context c,PendingResult p,String reply,String request,String command){this.c=c;pending=p;this.reply=reply;this.request=request;this.command=command;}
        void start(){
            try {
                TelecomManager tm=(TelecomManager)c.getSystemService(Context.TELECOM_SERVICE);
                String dialer=tm==null?null:tm.getDefaultDialerPackage();
                if(dialer!=null && !dialer.equals(Protocol.APP))targets.add(dialer);
                targets.add("com.android.incallui");targets.add("com.whatsapp");targets.add("com.whatsapp.w4b");
                c.registerReceiver(this,new IntentFilter(Protocol.PREFIX+"RESULT"),Context.RECEIVER_EXPORTED);registered=true;
                for(String pkg:targets)send(pkg,"STATUS",probe);
                handler.postDelayed(this::choose,1000);
            }catch(RuntimeException error){fail("Auto discovery failed: "+error);}
        }
        void send(String pkg,String action,String id){c.sendBroadcast(new Intent(Protocol.PREFIX+action).setPackage(pkg)
            .putExtra(Protocol.KEY,Protocol.TOKEN).putExtra("reply_package",Protocol.APP).putExtra("request_id",id));}
        @Override public void onReceive(Context context,Intent intent){
            if(finished || !Protocol.TOKEN.equals(intent.getStringExtra(Protocol.KEY)))return;
            String id=intent.getStringExtra("request_id"),pkg=intent.getStringExtra("backend_package");
            if(!targets.contains(pkg))return;
            if(!dispatched && probe.equals(id) && "STATUS".equals(intent.getStringExtra("command"))
                    && intent.getBooleanExtra("success",false) && intent.getBooleanExtra("call_active",false))active.put(pkg,new Intent(intent));
            if(dispatched && execution.equals(id) && Objects.equals(selected,pkg))complete(intent);
        }
        void choose(){
            if(finished)return;
            if(active.isEmpty()){fail("No active call backend replied. Start/answer a call and check Phone/WhatsApp scope and hooks.");return;}
            if(active.size()!=1){fail("Multiple call backends report active: "+String.join(", ",active.keySet())+". Select Phone or WhatsApp explicitly.");return;}
            selected=active.keySet().iterator().next();
            if("STATUS".equals(command)){complete(active.get(selected));return;}
            dispatched=true;
            try{send(selected,command,execution);handler.postDelayed(() -> fail("Selected call backend did not confirm the command; it was not resent."),2500);}
            catch(RuntimeException error){fail("Auto command failed: "+error);}
        }
        void complete(Intent result){
            if(finished)return;
            Intent output=new Intent(result).setPackage(reply).setComponent(null);
            output.putExtra("request_id",request).putExtra("command",command);
            try{c.sendBroadcast(output);}finally{finish();}
        }
        void fail(String message){if(finished)return;try{failure(c,reply,request,command,message);}finally{finish();}}
        void finish(){finished=true;handler.removeCallbacksAndMessages(null);if(registered){c.unregisterReceiver(this);registered=false;}BUSY.set(false);if(pending!=null)pending.finish();}
    }
}
