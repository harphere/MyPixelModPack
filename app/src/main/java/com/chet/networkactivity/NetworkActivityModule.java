package com.chet.networkactivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NetworkActivityModule implements IXposedHookLoadPackage {
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final Set<View> SEEN = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!SYSTEM_UI.equals(lpparam.packageName)) return;
        hookCollapsedStatusBarFragment(lpparam.classLoader);
        hookPhoneStatusBarView(lpparam.classLoader);
    }

    private void hookCollapsedStatusBarFragment(ClassLoader cl) {
        try {
            Class<?> cls = XposedHelpers.findClass("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment", cl);
            XposedHelpers.findAndHookMethod(cls, "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    View root = (View) param.args[0];
                    if (root != null) root.post(() -> inject(root));
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("PXNetworkActivity: fragment hook unavailable: " + t);
        }
    }

    private void hookPhoneStatusBarView(ClassLoader cl) {
        try {
            Class<?> cls = XposedHelpers.findClass("com.android.systemui.statusbar.phone.PhoneStatusBarView", cl);
            XposedBridge.hookAllMethods(cls, "onFinishInflate", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    View root = (View) param.thisObject;
                    root.post(() -> inject(root));
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("PXNetworkActivity: PhoneStatusBarView hook unavailable: " + t);
        }
    }

    private static synchronized void inject(View root) {
        if (SEEN.contains(root)) return;
        ViewGroup host = findBestHost(root);
        if (host == null) {
            XposedBridge.log("PXNetworkActivity: no compatible status-bar host found");
            return;
        }

        for (int i = 0; i < host.getChildCount(); i++) {
            View child = host.getChildAt(i);
            if (child instanceof NetworkActivityView) {
                SEEN.add(root);
                return;
            }
        }

        NetworkActivityView monitor = new NetworkActivityView(root.getContext());
        monitor.setTag("px_network_activity_monitor");
        monitor.setSystemTint(resolveTint(host));

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        host.addView(monitor, 0, lp);
        SEEN.add(root);
        XposedBridge.log("PXNetworkActivity: monitor injected into " + host.getClass().getName());
    }

    private static ViewGroup findBestHost(View root) {
        String[] ids = {
                "status_bar_end_side_content",
                "system_icon_area",
                "statusIcons",
                "status_bar_system_icons",
                "status_bar_end_side_except_heads_up"
        };
        for (String name : ids) {
            int id = root.getResources().getIdentifier(name, "id", SYSTEM_UI);
            if (id != 0) {
                View v = root.findViewById(id);
                if (v instanceof ViewGroup) return (ViewGroup) v;
            }
        }

        if (root instanceof ViewGroup) {
            return findLikelyContainer((ViewGroup) root);
        }
        return null;
    }

    private static ViewGroup findLikelyContainer(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (!(child instanceof ViewGroup)) continue;
            ViewGroup vg = (ViewGroup) child;
            CharSequence desc = vg.getContentDescription();
            String n = vg.getClass().getName();
            if (n.contains("StatusIcon") || (desc != null && desc.toString().toLowerCase().contains("status"))) {
                return vg;
            }
            ViewGroup nested = findLikelyContainer(vg);
            if (nested != null) return nested;
        }
        return null;
    }

    private static int resolveTint(ViewGroup host) {
        for (int i = 0; i < host.getChildCount(); i++) {
            View child = host.getChildAt(i);
            if (child instanceof TextView) {
                try { return ((TextView) child).getCurrentTextColor(); } catch (Throwable ignored) {}
            }
        }
        return Color.WHITE;
    }
}
