package dev.chet.mypixelmodpack;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.service.notification.StatusBarNotification;
import android.telecom.TelecomManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Select one visible notification icon, preserving the shade's drag stream. */
public final class NotificationIconTapModule {
    private View target;
    private StatusBarNotification notification;
    private float downX, downY;
    private final IconTapGesture gesture=new IconTapGesture();
    private boolean tracking;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam load) {
        if (!"com.android.systemui".equals(load.processName)) return;
        XposedHelpers.findAndHookMethod(ViewGroup.class, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam p) {
                if (!p.thisObject.getClass().getName().endsWith(".PhoneStatusBarView")) return;
                View root=(View)p.thisObject; MotionEvent event=(MotionEvent)p.args[0];
                try {
                    int action=event.getActionMasked();
                    if(action==MotionEvent.ACTION_DOWN) {
                        reset();
                        KeyguardManager keyguard=(KeyguardManager)root.getContext().getSystemService(Context.KEYGUARD_SERVICE);
                        if(keyguard==null || keyguard.isKeyguardLocked()) return;
                        downX=event.getRawX(); downY=event.getRawY(); gesture.begin(downX,downY,event.getEventTime(),ViewConfiguration.get(root.getContext()).getScaledTouchSlop(),ViewConfiguration.getLongPressTimeout());
                        // All icons participate in nearest selection; an unsupported icon never opens a neighbour.
                        nearest(root,downX,downY,new float[]{Float.MAX_VALUE});
                        tracking=notification!=null && supported(root.getContext(),notification.getPackageName());
                    } else if(tracking) {
                        if(!gesture.update(event.getRawX(),event.getRawY(),event.getEventTime(),event.getPointerCount())) { reset(); return; }
                        if(action==MotionEvent.ACTION_CANCEL || action==MotionEvent.ACTION_POINTER_DOWN) { reset(); return; }
                        if(action==MotionEvent.ACTION_UP) {
                            Rect rect=new Rect();
                            if(target==null || !target.isAttachedToWindow() || !target.getGlobalVisibleRect(rect)
                                || !rect.contains((int)downX,(int)downY)) { reset(); return; }
                            Intent launch=root.getContext().getPackageManager().getLaunchIntentForPackage(notification.getPackageName());
                            KeyguardManager keyguard=(KeyguardManager)root.getContext().getSystemService(Context.KEYGUARD_SERVICE);
                            if(launch==null || keyguard==null || keyguard.isKeyguardLocked()
                                || !notification.getUser().equals(android.os.Process.myUserHandle())) { reset(); return; }
                            p.setObjectExtra("pack_launch",launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            p.setObjectExtra("pack_event_action",event.getAction());
                            // Let SystemUI receive CANCEL to clean up its DOWN; never swallow an unfinished stream.
                            event.setAction(MotionEvent.ACTION_CANCEL);
                            reset();
                        }
                    }
                } catch(Throwable error) { reset(); XposedBridge.log("PackIconTap: gesture unavailable: "+error); }
            }
            @Override protected void afterHookedMethod(MethodHookParam p) {
                Object old=p.getObjectExtra("pack_event_action");
                if(old==null) return;
                ((MotionEvent)p.args[0]).setAction((Integer)old);
                Intent intent=(Intent)p.getObjectExtra("pack_launch");
                View root=(View)p.thisObject;
                if(!p.hasThrowable()) root.post(()->open(root.getContext(),intent));
            }
        });
        PackRuntime.report("notification_tap","PhoneStatusBarView touch bridge installed; AquaMail, YAATA and Phone; unlocked only");
    }
    private void reset() { gesture.cancel(); tracking=false; target=null; notification=null; }
    private void nearest(View view,float x,float y,float[] best) {
        if(!view.isShown() || view.getAlpha()<=.1f) return;
        if(view.getClass().getName().endsWith(".StatusBarIconView")) {
            try {
                StatusBarNotification sbn;
                try { sbn=(StatusBarNotification)XposedHelpers.callMethod(view,"getNotification"); }
                catch(Throwable missingGetter) { sbn=(StatusBarNotification)XposedHelpers.getObjectField(view,"mNotification"); }
                Rect r=new Rect();
                if(sbn!=null && view.getGlobalVisibleRect(r) && r.contains((int)x,(int)y)) {
                    float distance=Math.abs(r.exactCenterX()-x);
                    if(distance<best[0]) { best[0]=distance; target=view; notification=sbn; }
                }
            } catch(Throwable ignored) { /* Non-notification status icons have no SBN. */ }
        }
        if(view instanceof ViewGroup) { ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++) nearest(group.getChildAt(i),x,y,best);
        }
    }
    static boolean supported(Context context,String pkg) {
        if("org.kman.AquaMail".equals(pkg) || "rpkandrodev.yaata".equals(pkg)
            || "com.android.dialer".equals(pkg) || "com.google.android.dialer".equals(pkg)
            || "com.android.incallui".equals(pkg)) return true;
        TelecomManager telecom=(TelecomManager)context.getSystemService(Context.TELECOM_SERVICE);
        return telecom!=null && pkg.equals(telecom.getDefaultDialerPackage());
    }
    private static void open(Context context,Intent intent) {
        try {
            ClassLoader loader=context.getClassLoader();
            Object starter=XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.systemui.Dependency",loader),"get",
                XposedHelpers.findClass("com.android.systemui.plugins.ActivityStarter",loader));
            XposedHelpers.callMethod(starter,"postStartActivityDismissingKeyguard",intent,0);
            XposedBridge.log("PackIconTap: opened "+intent.getPackage());
        } catch(Throwable error) {
            // Fail closed if the ROM's ActivityStarter API differs; do not bypass keyguard/shade handling.
            XposedBridge.log("PackIconTap: ActivityStarter unavailable; no app opened: "+error);
        }
    }
}
