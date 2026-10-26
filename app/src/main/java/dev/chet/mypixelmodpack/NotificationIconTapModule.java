package dev.chet.mypixelmodpack;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.telecom.TelecomManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.lang.reflect.Method;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Select one visible notification icon, preserving the shade's drag stream. */
public final class NotificationIconTapModule {
    private static final class Icon {
        final View view; final StatusBarNotification notification; final float left,right;
        Icon(View view,StatusBarNotification notification,float left,float right) {
            this.view=view;this.notification=notification;this.left=left;this.right=right;
        }
    }
    private Icon target;
    private final IconTapGesture gesture=new IconTapGesture();
    private boolean tracking;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam load) throws Throwable {
        if (!"com.android.systemui".equals(load.processName)) return;
        Class<?> statusBar=XposedHelpers.findClass("com.android.systemui.statusbar.phone.PhoneStatusBarView",load.classLoader);
        // Resolve the ROM's actual override (or inherited implementation), not always ViewGroup's method.
        Method dispatch=statusBar.getMethod("dispatchTouchEvent",MotionEvent.class);
        XposedBridge.hookMethod(dispatch,new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam p) {
                if(!statusBar.isInstance(p.thisObject)) return;
                View root=(View)p.thisObject;MotionEvent event=(MotionEvent)p.args[0];
                try {
                    int action=event.getActionMasked();
                    if(action==MotionEvent.ACTION_DOWN) {
                        reset();
                        if(!unlocked(root.getContext())) { report("tap ignored: screen locked");return; }
                        float x=event.getRawX(),y=event.getRawY();
                        ArrayList<Icon> icons=new ArrayList<>();collect(root,icons);
                        float[] left=new float[icons.size()],right=new float[icons.size()];
                        for(int i=0;i<icons.size();i++) {left[i]=icons.get(i).left;right[i]=icons.get(i).right;}
                        int[] at=new int[2];root.getLocationOnScreen(at);
                        int hit=IconTapTargeting.pick(x,y,at[0],at[1],at[0]+root.getWidth(),at[1]+root.getHeight(),left,right,6f*root.getResources().getDisplayMetrics().density);
                        if(hit<0) {report("touch bridge reached; no icon hit; visible notification icons="+icons.size());return;}
                        target=icons.get(hit);
                        String pkg=target.notification.getPackageName();
                        if(!supported(root.getContext(),pkg)) {report("tap ignored: unsupported app "+pkg);reset();return;}
                        if(!target.notification.getUser().equals(android.os.Process.myUserHandle())) {report("tap ignored: different user/profile");reset();return;}
                        gesture.begin(x,y,event.getEventTime(),ViewConfiguration.get(root.getContext()).getScaledTouchSlop(),ViewConfiguration.getLongPressTimeout());
                        tracking=true;
                        report("icon selected: "+pkg+"; waiting for tap release");
                    } else if(tracking) {
                        if(action==MotionEvent.ACTION_CANCEL || action==MotionEvent.ACTION_POINTER_DOWN
                            || !gesture.update(event.getRawX(),event.getRawY(),event.getEventTime(),event.getPointerCount())) {
                            reset();report("shortcut cancelled: drag, long press, multi-touch or SystemUI cancellation");return;
                        }
                        if(action==MotionEvent.ACTION_UP) {
                            if(target==null || !target.view.isAttachedToWindow() || !target.view.isShown() || !unlocked(root.getContext())) {
                                reset();report("shortcut cancelled: icon removed or screen locked");return;
                            }
                            StatusBarNotification current=notification(target.view);
                            if(current==null || !current.getKey().equals(target.notification.getKey())) {reset();report("shortcut cancelled: notification changed");return;}
                            Intent launch=root.getContext().getPackageManager().getLaunchIntentForPackage(current.getPackageName());
                            PendingIntent pending=current.getNotification().contentIntent;
                            if(launch==null && (pending==null || !pending.isActivity())) {reset();report("no launcher activity or activity notification action for "+current.getPackageName());return;}
                            p.setObjectExtra("pack_target",current);
                            p.setObjectExtra("pack_launch",launch);
                            p.setObjectExtra("pack_event_action",event.getAction());
                            event.setAction(MotionEvent.ACTION_CANCEL);
                            reset();
                        }
                    }
                } catch(Throwable error) {reset();report("gesture failed: "+error);}
            }
            @Override protected void afterHookedMethod(MethodHookParam p) {
                Object old=p.getObjectExtra("pack_event_action");if(old==null)return;
                ((MotionEvent)p.args[0]).setAction((Integer)old);
                View root=(View)p.thisObject;
                if(p.hasThrowable()) {report("SystemUI rejected gesture cancellation");return;}
                StatusBarNotification sbn=(StatusBarNotification)p.getObjectExtra("pack_target");
                Intent launch=(Intent)p.getObjectExtra("pack_launch");
                if(!root.post(()->open(root.getContext(),launch,sbn))) report("launch could not be queued");
            }
        });
        PackRuntime.report("notification_tap","touch hook installed on "+dispatch.getDeclaringClass().getName()+"; full-height icon targets; restart applied");
    }
    private void reset(){gesture.cancel();tracking=false;target=null;}
    private static boolean unlocked(Context context) {
        KeyguardManager keyguard=(KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguard!=null && !keyguard.isKeyguardLocked();
    }
    private static StatusBarNotification notification(View view) {
        try {return (StatusBarNotification)XposedHelpers.callMethod(view,"getNotification");}
        catch(Throwable noGetter){try{return (StatusBarNotification)XposedHelpers.getObjectField(view,"mNotification");}catch(Throwable absent){return null;}}
    }
    private static void collect(View view,ArrayList<Icon> icons) {
        if(!view.isShown() || view.getAlpha()<=.1f || view.getWidth()<=0)return;
        if(view.getClass().getName().endsWith(".StatusBarIconView")) {
            StatusBarNotification sbn=notification(view);
            boolean visible=true;
            try {visible=((Integer)XposedHelpers.callMethod(view,"getVisibleState"))==0;}
            catch(Throwable unavailable){/* Older ROMs expose visibility through View only. */}
            if(sbn!=null && visible) {int[] at=new int[2];view.getLocationOnScreen(at);icons.add(new Icon(view,sbn,at[0],at[0]+view.getWidth()));}
        }
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)collect(group.getChildAt(i),icons);}
    }
    static boolean supported(Context context,String pkg) {
        if("org.kman.AquaMail".equals(pkg)||"rpkandrodev.yaata".equals(pkg)||"com.android.dialer".equals(pkg)
            ||"com.google.android.dialer".equals(pkg)||"com.android.incallui".equals(pkg))return true;
        TelecomManager telecom=(TelecomManager)context.getSystemService(Context.TELECOM_SERVICE);
        return telecom!=null && pkg.equals(telecom.getDefaultDialerPackage());
    }
    private static void open(Context context,Intent intent,StatusBarNotification sbn) {
        if(!unlocked(context)){report("launch cancelled: screen locked");return;}
        if(intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                ClassLoader loader=context.getClassLoader();
                Object starter=XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.systemui.Dependency",loader),"get",
                    XposedHelpers.findClass("com.android.systemui.plugins.ActivityStarter",loader));
                XposedHelpers.callMethod(starter,"postStartActivityDismissingKeyguard",intent,0);
                report("app launch requested through ActivityStarter: "+sbn.getPackageName());return;
            }catch(Throwable unavailable){XposedBridge.log("PackIconTap: trying direct app launch; ActivityStarter unavailable: "+unavailable);}
            try {context.startActivity(intent);report("app launch requested directly: "+sbn.getPackageName());return;}
            catch(RuntimeException error){XposedBridge.log("PackIconTap: launcher activity failed: "+error);}
        }
        // An activity-only notification action is a last resort when the app launcher cannot be opened.
        PendingIntent pending=sbn.getNotification().contentIntent;
        if(pending!=null && pending.isActivity() && unlocked(context))try {
            pending.send();report("notification activity requested: "+sbn.getPackageName());return;
        }catch(PendingIntent.CanceledException error){report("notification action expired: "+sbn.getPackageName());return;}
        catch(RuntimeException error){report("notification activity failed: "+error);return;}
        report("could not open app: "+sbn.getPackageName());
    }
    private static void report(String message){PackRuntime.report("notification_tap_last",message);}
}
