package dev.chet.mypixelmodpack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public final class HomeActivity extends Activity {
    // Keys and appearance activities match the working 2.0.2 implementation.
    private static final String[][] FEATURES = {
        {"battery", "Battery gradient", "dev.chet.batterygradient.SettingsActivity"},
        {"cursor", "Gboard cursor arrows", ""},
        {"mobile", "4G/5G icon styles", "dev.chet.mobiletypeframe.SettingsActivity"},
        {"vo", "VoLTE / VoWiFi icons", "com.chet.voserviceframe.MainActivity"},
        {"opa", "OPA Home animation", ""},
        {"dots", "Navigation icons", "com.chet.navdotstyle.MainActivity"},
        {"match", "Nav Bar Status Match", "com.chet.navbarmatch.MainActivity"},
        {"network", "Network activity indicator", "com.chet.networkactivity.SettingsActivity"},
        {"wake", "Double Tap 2 Wake", ""}
    };
    private SharedPreferences prefs;
    private LinearLayout page;
    private String section;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs=getSharedPreferences("features_v2",MODE_PRIVATE);
        section=state == null ? getIntent().getStringExtra("section") : state.getString("section");
        render();
        if(section==null) AccessRefresh.start(getApplicationContext(),null);
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putString("section",section); }
    @Override protected void onResume() { super.onResume(); if (page != null) render(); }
    private void render() {
        LinearLayout root=PackUi.column(this); root.setBackgroundColor(PackUi.background(this));
        if(section != null) root.addView(PackUi.header(this,section));
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        page=PackUi.column(this); int pad=PackUi.dp(this,20); page.setPadding(pad,pad,pad,pad);
        scroll.addView(page); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
        if(section==null) {
            TextView title=PackUi.label(this,"My Pixel\nMod Pack",34,true); title.setPadding(0,pad,0,pad); page.addView(title);
            note(page,"Version "+BuildConfig.VERSION_NAME+" • Your Pixel, your style");
            navigation("Status bar","Battery, mobile icons and network activity",new int[]{0,2,3,7});
            navigation("Navigation","Icons, OPA animation, cursor arrows and colours",new int[]{1,4,5,6});
            navigation("Screen & wake","Double Tap to Wake",new int[]{8});
            navigation("App & diagnostics","Settings shortcut, process reports and scope help",null);
            note(page,"Feature changes require a restart. Keep matching standalone modules disabled.");
        } else if("App & diagnostics".equals(section)) {
            LinearLayout c=PackUi.card(this);
            toggle(c,"settings_entry","Show in Android Settings","Add My Pixel Mod Pack to the Settings home page.");
            note(c,"Add Settings (com.android.settings) to the pack scope, then restart Settings or reboot. The shortcut is optional; placement depends on the ROM."); page.addView(c);
            action("Refresh app access",AccessRefresh.isRunning() ? "Refresh in progress…" : "Keep scoped apps able to read the pack’s saved settings",this::refreshAccess);
            action("Startup diagnostics","View process reports and copy the full log",this::diagnostics);
            action("Scope & restart help","Which processes need to restart",this::help);
        } else {
            int[] indices="Status bar".equals(section)?new int[]{0,2,3,7}:"Navigation".equals(section)?new int[]{1,4,5,6}:new int[]{8};
            note(page,"Switch changes take effect after the relevant process restarts or the phone reboots.");
            for(int i:indices) feature(i);
        }
    }
    private void navigation(String name,String summary,int[] indices) {
        String status="";
        if(indices != null) { int on=0; for(int i:indices) if(prefs.getBoolean(FEATURES[i][0],false)) on++; status=" • "+on+" enabled"; }
        action(name,summary+status,() -> startActivity(new Intent(this,HomeActivity.class).putExtra("section",name)));
    }
    private void feature(int i) {
        String[] f=FEATURES[i]; LinearLayout c=PackUi.card(this);
        toggle(c,f[0],f[1],description(f[0]));
        if(!f[2].isEmpty()) {
            TextView settings=PackUi.label(this,"Customize  ›",16,true); settings.setTextColor(PackUi.accent(this));
            settings.setPadding(0,PackUi.dp(this,18),0,PackUi.dp(this,8)); PackUi.clickable(settings);
            settings.setOnClickListener(v -> startActivity(new Intent().setClassName(this,f[2]))); c.addView(settings);
        }
        page.addView(c);
    }
    private void toggle(LinearLayout c,String key,String title,String summary) {
        Switch s=new Switch(this); s.setText(title); s.setTextSize(18); s.setTextColor(PackUi.text(this));
        s.setMinHeight(PackUi.dp(this,56)); s.setChecked(prefs.getBoolean(key,false));
        final boolean[] reverting={false};
        s.setOnCheckedChangeListener((v,on)-> {
            if(reverting[0]) return;
            if(!prefs.edit().putBoolean(key,on).commit()) { reverting[0]=true; s.setChecked(!on); reverting[0]=false; Toast.makeText(this,"Could not save switch",Toast.LENGTH_LONG).show(); }
        }); c.addView(s); note(c,summary);
    }
    private void action(String title,String summary,Runnable run) {
        LinearLayout c=PackUi.card(this); TextView t=PackUi.label(this,title+"  ›",21,true); c.addView(t); note(c,summary);
        PackUi.clickable(c); c.setOnClickListener(v->run.run()); page.addView(c);
    }
    private void note(LinearLayout target,String value) {
        TextView t=PackUi.label(this,value,14,false); t.setTextColor(PackUi.muted(this)); t.setPadding(0,PackUi.dp(this,8),0,PackUi.dp(this,12)); target.addView(t);
    }
    private String description(String key) {
        switch(key) {
            case "battery": return "Gradient battery styles and placement. Scope: System UI.";
            case "cursor": return "Tap arrows to move one character; hold to repeat. Scope: Gboard and Launcher3. After enabling, force stop Gboard and reopen a text field; restart Launcher3 or reboot.";
            case "mobile": return "Choose the look of 4G and 5G labels. Scope: System UI.";
            case "vo": return "Style the ROM’s VoLTE and VoWiFi indicators. Scope: System UI.";
            case "opa": return "Animate the Home button in the launcher navigation row. Scope: Launcher3.";
            case "dots": return "Choose dots or hashmarks for three-button navigation. Scope: Launcher3.";
            case "match": return "Match navigation bar colour to the status bar. Scope each app you want to match.";
            case "network": return "Show upload and download activity. Scope: System UI.";
            default: return "Wake the display with a double tap. Scope: System UI.";
        }
    }
    private void help() {
        new AlertDialog.Builder(this).setTitle("Scope & restart help")
            .setMessage("System UI: battery, mobile and service icons, network activity, Double Tap to Wake.\n\nLauncher3: navigation icons, OPA and Gboard arrows. Nova is your home app; Launcher3 hosts the navigation row.\n\nGboard: cursor arrows. Force stop Gboard and reopen it after enabling or updating the pack if arrows are missing.\n\nNav Bar Status Match: each target app.\n\nSettings: optional pack shortcut.\n\nSystem Framework is unnecessary. Reboot applies all feature changes. Keep matching standalone modules disabled.")
            .setPositiveButton("Got it",null).show();
    }
    private void refreshAccess() {
        boolean started=AccessRefresh.start(getApplicationContext(),() -> {
            if(isFinishing() || isDestroyed()) return;
            String result=getSharedPreferences("access_diagnostics_v2",0).getString("result","No result recorded");
            new AlertDialog.Builder(this).setTitle("App access refreshed")
                .setMessage(result+"\n\nForce stop and reopen affected apps if matching was missing.")
                .setPositiveButton("Got it",null).show();
        });
        Toast.makeText(this,started ? "Refreshing app access…" : "App access refresh is already running",Toast.LENGTH_SHORT).show();
    }
    private void diagnostics() {
        StringBuilder text=new StringBuilder("My Pixel Mod Pack "+BuildConfig.VERSION_NAME+"\nSaved switches (restart required):\n");
        for(String[] f:FEATURES) text.append(f[0]).append('=').append(prefs.getBoolean(f[0],false)).append('\n');
        SharedPreferences matchPrefs=getSharedPreferences("nav_match_v2",MODE_PRIVATE);
        text.append("\nNav Bar Match settings:\nmodule_enabled=").append(matchPrefs.getBoolean("enabled",true));
        text.append("\nblacklist=").append(matchPrefs.getStringSet("blacklist",java.util.Collections.emptySet()));
        text.append("\n\nApp access refresh:\n").append(getSharedPreferences("access_diagnostics_v2",MODE_PRIVATE).getString("result","Not run yet")).append('\n');
        text.append("settings_entry=").append(prefs.getBoolean("settings_entry",false)).append("\n\nLatest process reports (last reported, not live):\n");
        Map<String,?> reports=new TreeMap<>(getSharedPreferences("diagnostics_v2",0).getAll());
        if(reports.isEmpty()) text.append("No process reports yet. Confirm scope and restart the relevant processes.\n");
        for(Map.Entry<String,?> r:reports.entrySet()) {
            String raw=String.valueOf(r.getValue()); String formatted=raw;
            try { int split=raw.indexOf(" | "); formatted=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM).format(new Date(Long.parseLong(raw.substring(0,split))))+raw.substring(split); } catch(RuntimeException ignored) {}
            text.append(r.getKey()).append(": ").append(formatted).append('\n');
        }
        TextView output=PackUi.label(this,text.toString(),14,false); output.setTextIsSelectable(true); int p=PackUi.dp(this,20); output.setPadding(p,p,p,p);
        ScrollView scroll=new ScrollView(this); scroll.addView(output);
        new AlertDialog.Builder(this).setTitle("Startup diagnostics").setView(scroll)
            .setPositiveButton("Copy all",(d,w)-> { ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Pack diagnostics",text.toString())); Toast.makeText(this,"Full report copied",Toast.LENGTH_SHORT).show(); })
            .setNegativeButton("Close",null).show();
    }
}
