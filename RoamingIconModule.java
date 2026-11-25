package dev.chet.mypixelmodpack;

import android.view.View;
import android.view.ViewGroup;
import java.util.HashSet;
import java.util.Set;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Presentation only: never changes telephony roaming state or data policy. */
public final class RoamingIconModule {
    private final Set<Integer> ids=new HashSet<>();
    private boolean reported;
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam load) {
        if(!"com.android.systemui".equals(load.processName))return;
        for(String name:new String[]{"mobile_roaming","mobile_roaming_space"}) {
            int id=PackRuntime.hostContext().getResources().getIdentifier(name,"id","com.android.systemui");
            if(id!=0)ids.add(id);
        }
        if(ids.isEmpty()) {PackRuntime.report("hide_roaming","No mobile roaming view IDs found; UI unchanged");return;}
        XposedHelpers.findAndHookMethod(View.class,"setVisibility",int.class,new XC_MethodHook(){
            @Override protected void beforeHookedMethod(MethodHookParam p) {
                View view=(View)p.thisObject;
                if(ids.contains(view.getId())) {p.args[0]=View.GONE;seen();}
            }
        });
        // Cover initially visible XML views as well as later binder updates.
        XposedHelpers.findAndHookMethod(ViewGroup.class,"addView",View.class,int.class,ViewGroup.LayoutParams.class,new XC_MethodHook(){
            @Override protected void afterHookedMethod(MethodHookParam p) {if(!p.hasThrowable() && p.args[0] instanceof View)hide((View)p.args[0]);}
        });
        for(String name:new String[]{"com.android.systemui.statusbar.pipeline.mobile.ui.binder.MobileIconBinder",
                "com.android.systemui.statusbar.StatusBarMobileView"}) {
            Class<?> type=XposedHelpers.findClassIfExists(name,load.classLoader);
            if(type==null)continue;
            for(String method:new String[]{"bind","onFinishInflate","updateState","applyMobileState"})
                XposedBridge.hookAllMethods(type,method,new XC_MethodHook(){
                    @Override protected void afterHookedMethod(MethodHookParam p) {
                        if(p.thisObject instanceof View)hide((View)p.thisObject);
                        for(Object arg:p.args)if(arg instanceof View)hide((View)arg);
                    }
                });
        }
        PackRuntime.report("hide_roaming","Roaming R and spacer visibility hooks installed; roaming service unchanged");
    }
    private void hide(View view) {
        if(ids.contains(view.getId())) {view.setVisibility(View.GONE);seen();}
        if(view instanceof ViewGroup) {ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)hide(group.getChildAt(i));}
    }
    private void seen(){if(!reported){reported=true;PackRuntime.report("hide_roaming_view","Roaming indicator/spacer matched and hidden");}}
}
