package dev.chet.mypixelmodpack;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Presentation wrapper for existing feature settings. No feature behavior is changed. */
public final class UiApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            public void onActivityCreated(Activity a, Bundle b) {}
            public void onActivityResumed(Activity a) {
                if(a instanceof HomeActivity) return;
                ViewGroup content=a.findViewById(android.R.id.content);
                if(content==null || content.getChildCount()!=1) return;
                View original=content.getChildAt(0);
                if(!"pack-ui-wrapper".equals(original.getTag())) {
                    content.removeView(original);
                    LinearLayout root=PackUi.column(a); root.setTag("pack-ui-wrapper"); root.setBackgroundColor(PackUi.background(a));
                    root.addView(PackUi.header(a,title(a)));
                    if(hasScrollContainer(original)) root.addView(original,new LinearLayout.LayoutParams(-1,0,1));
                    else { ScrollView scroll=new ScrollView(a); scroll.addView(original,new ScrollView.LayoutParams(-1,-2)); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); }
                    a.setContentView(root);
                }
                recolor(content);
            }
            public void onActivityStarted(Activity a) {}
            public void onActivityPaused(Activity a) {}
            public void onActivityStopped(Activity a) {}
            public void onActivitySaveInstanceState(Activity a,Bundle b) {}
            public void onActivityDestroyed(Activity a) {}
        });
    }
    private static boolean hasScrollContainer(View v) {
        if(v instanceof ScrollView || v instanceof android.widget.AbsListView) return true;
        if(v instanceof ViewGroup) { ViewGroup g=(ViewGroup)v; for(int i=0;i<g.getChildCount();i++) if(hasScrollContainer(g.getChildAt(i))) return true; }
        return false;
    }
    private static String title(Activity a) {
        String name=a.getClass().getName();
        if(name.contains("batterygradient")) return "Battery gradient";
        if(name.contains("mobiletypeframe")) return "4G/5G icon styles";
        if(name.contains("voserviceframe")) return "VoLTE / VoWiFi icons";
        if(name.contains("navdotstyle")) return "Navigation icons";
        if(name.contains("navbarmatch")) return "Nav Bar Status Match";
        if(name.contains("networkactivity")) return "Network activity";
        if(name.contains("speakercontrol")) return "SpeakerControl";
        if(name.contains("chromepiestatusmatch")) return "ChromePie Status Match";
        return "Gboard arrows";
    }
    private static void recolor(View view) {
        if(view==null) return;
        if(view instanceof TextView) ((TextView)view).setTextColor(PackUi.text(view.getContext()));
        if(view instanceof ViewGroup) {
            if(view.getBackground() instanceof ColorDrawable) view.setBackgroundColor(PackUi.background(view.getContext()));
            ViewGroup group=(ViewGroup)view; for(int i=0;i<group.getChildCount();i++) recolor(group.getChildAt(i));
        }
    }
}
