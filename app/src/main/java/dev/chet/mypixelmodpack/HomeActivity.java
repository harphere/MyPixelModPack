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
        {"wake", "Double Tap 2 Wake", ""},
        {"speaker", "SpeakerControl", "dev.chet.speakercontrol.MainActivity"},
        {"pie", "ChromePie Status Match", "dev.chet.chromepiestatusmatch.settings.PieSettings"}
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
            LinearLayout toolbar=new LinearLayout(this); toolbar.setGravity(Gravity.CENTER_VERTICAL);
            TextView title=PackUi.label(this,"My Pixel Mod Pack",26,false);
            toolbar.addView(title,new LinearLayout.LayoutParams(0,PackUi.dp(this,72),1)); title.setGravity(Gravity.CENTER_VERTICAL);
            TextView refresh=PackUi.label(this,"↻",30,false);refresh.setGravity(Gravity.CENTER);refresh.setContentDescription("Refresh app access");
            toolbar.addView(refresh,new LinearLayout.LayoutParams(PackUi.dp(this,48),PackUi.dp(this,56)));PackUi.clickable(refresh);refresh.setOnClickListener(v->refreshAccess());
            TextView menu=PackUi.label(this,"⋮",28,false);menu.setGravity(Gravity.CENTER);menu.setContentDescription("More options");
            toolbar.addView(menu,new LinearLayout.LayoutParams(PackUi.dp(this,40),PackUi.dp(this,56)));PackUi.clickable(menu);
            menu.setOnClickListener(v->{ android.widget.PopupMenu popup=new android.widget.PopupMenu(this,menu);
                popup.getMenu().add("Startup diagnostics").setOnMenuItemClickListener(item->{diagnostics();return true;});
                popup.getMenu().add("Scope & restart help").setOnMenuItemClickListener(item->{help();return true;});popup.show(); });
            page.addView(toolbar);
            note(page,"Version "+BuildConfig.VERSION_NAME);
            android.widget.EditText search=new android.widget.EditText(this);search.setSingleLine(true);search.setHint("Search features…");search.setTextSize(17);
            search.setTextColor(PackUi.text(this));search.setHintTextColor(PackUi.muted(this));search.setContentDescription("Search Pack features");
            android.graphics.drawable.GradientDrawable searchBackground=new android.graphics.drawable.GradientDrawable();
            searchBackground.setColor(PackUi.background(this));searchBackground.setCornerRadius(PackUi.dp(this,32));searchBackground.setStroke(PackUi.dp(this,1),PackUi.muted(this));
            search.setBackground(searchBackground);search.setPadding(PackUi.dp(this,22),0,PackUi.dp(this,22),0);
            LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(-1,PackUi.dp(this,56));searchParams.bottomMargin=PackUi.dp(this,22);page.addView(search,searchParams);
            search.addTextChangedListener(new android.text.TextWatcher(){
                public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                public void onTextChanged(CharSequence s,int start,int before,int count){String q=s.toString().trim().toLowerCase(java.util.Locale.ROOT);
                    for(int j=0;j<page.getChildCount();j++){View child=page.getChildAt(j);Object tag=child.getTag();
                        if(tag instanceof String && ((String)tag).startsWith("category:"))child.setVisibility(((String)tag).contains(q)?View.VISIBLE:View.GONE);}}
                public void afterTextChanged(android.text.Editable text){}
            });
            navigation("Status bar","Battery, mobile icons and network activity",new int[]{0,2,3,7});
            navigation("Navigation","Icons, OPA animation, cursor arrows and colours",new int[]{1,4,5,6});
            navigation("Calls & browser","Speakerphone commands and ChromePie",new int[]{9,10});
            navigation("Screen & wake","Double Tap to Wake",new int[]{8});
            navigation("App & diagnostics","Settings shortcut, process reports and scope help",null);
            note(page,"Feature changes require a restart. Keep matching standalone modules disabled.");
            LinearLayout tabs=new LinearLayout(this); tabs.setPadding(pad,0,pad,0);tabs.setBackgroundColor(PackUi.surface(this));
            String[] tabNames={"Home","Diagnostics","Settings"};
            for(int i=0;i<3;i++){final int tab=i;TextView item=PackUi.label(this,tabNames[i],14,i==0);item.setGravity(Gravity.CENTER);
                tabs.addView(item,new LinearLayout.LayoutParams(0,PackUi.dp(this,64),1));PackUi.clickable(item);
                item.setOnClickListener(v->{if(tab==0)scroll.smoothScrollTo(0,0);else if(tab==1)diagnostics();else startActivity(new Intent(this,HomeActivity.class).putExtra("section","App & diagnostics"));});}
            root.addView(tabs);

        } else if("App & diagnostics".equals(section)) {
            LinearLayout c=PackUi.card(this);
            toggle(c,"settings_entry","Show in Android Settings","Add My Pixel Mod Pack to the Settings home page.");
            note(c,"Add Settings (com.android.settings) to the pack scope, then restart Settings or reboot. The shortcut is optional; placement depends on the ROM."); page.addView(c);
            action("Refresh app access",AccessRefresh.isRunning() ? "Refresh in progress…" : "Keep scoped apps able to read the pack’s saved settings",this::refreshAccess);
            action("Startup diagnostics","View process reports and copy the full log",this::diagnostics);
            action("Scope & restart help","Which processes need to restart",this::help);
        } else {
            int[] indices="Status bar".equals(section)?new int[]{0,2,3,7}:"Navigation".equals(section)?new int[]{1,4,5,6}:"Calls & browser".equals(section)?new int[]{9,10}:new int[]{8};
            note(page,"Switch changes take effect after the relevant process restarts or the phone reboots.");
            for(int i:indices) feature(i);
        }
    }
    private void navigation(String name,String summary,int[] indices) {
        String status="";
        if(indices != null) { int on=0; for(int i:indices) if(prefs.getBoolean(FEATURES[i][0],false)) on++; status=" • "+on+" enabled"; }
        LinearLayout card=PackUi.card(this);card.setOrientation(LinearLayout.HORIZONTAL);card.setGravity(Gravity.CENTER_VERTICAL);
        int category="Status bar".equals(name)?0:"Navigation".equals(name)?1:"Calls & browser".equals(name)?2:"Screen & wake".equals(name)?3:4;
        CategoryIconView icon=new CategoryIconView(this,category);icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams iconParams=new LinearLayout.LayoutParams(PackUi.dp(this,44),PackUi.dp(this,44));iconParams.rightMargin=PackUi.dp(this,16);card.addView(icon,iconParams);
        LinearLayout labels=PackUi.column(this);labels.addView(PackUi.label(this,name,19,true));
        TextView detail=PackUi.label(this,summary+status,14,false);detail.setTextColor(PackUi.muted(this));detail.setPadding(0,PackUi.dp(this,6),0,0);labels.addView(detail);
        card.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        String keywords=name+" "+summary;
        if(indices!=null)for(int i:indices)keywords+=" "+FEATURES[i][1]+" "+description(FEATURES[i][0]);
        card.setTag("category:"+keywords.toLowerCase(java.util.Locale.ROOT));PackUi.clickable(card);
        card.setOnClickListener(v->startActivity(new Intent(this,HomeActivity.class).putExtra("section",name)));page.addView(card);
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
            case "speaker": return "Phone and WhatsApp calls. Scope: default Phone app/InCall UI plus WhatsApp. Choose the target in SpeakerControl; existing Tasker actions and TOKEN remain supported.";
            case "pie": return "ChromePie menu with status-bar colour matching. Scope: Titanium or supported Chromium browser. Configure the menu here; restart the browser after changes.";
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
            .setMessage("System UI: battery, mobile and service icons, network activity, Double Tap to Wake.\n\nLauncher3: navigation icons, OPA and Gboard arrows. Nova is your home app; Launcher3 hosts the navigation row.\n\nGboard: cursor arrows. Force stop Gboard and reopen it after enabling or updating the pack if arrows are missing.\n\nNav Bar Status Match: each target app.\n\nSpeakerControl: default Phone app/InCall UI, plus WhatsApp or WhatsApp Business if used.\n\nChromePie: Titanium or supported Chromium browser. Disable the standalone ChromePie Status Match module.\n\nSettings: optional pack shortcut.\n\nSystem Framework is unnecessary. Reboot applies all feature changes. Keep matching standalone modules disabled.")
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
        text.append("versionCode=").append(BuildConfig.VERSION_CODE).append("; build_id=pack-3.6.0-r1\n");
        for(String[] f:FEATURES) text.append(f[0]).append('=').append(prefs.getBoolean(f[0],false)).append('\n');
        android.os.Bundle batteryOptions=dev.chet.batterygradient.SettingsProvider.getDisplayOptions(this);
        text.append("\nBattery settings: style=").append(batteryOptions.getString("style"))
            .append("; percentage=").append(batteryOptions.getBoolean("show_percentage"))
            .append("; charging_animation=").append(batteryOptions.getBoolean("charging_animation",true))
            .append("; size=").append(batteryOptions.getInt("size_percent",100)).append("%")
            .append("; orientation=").append(batteryOptions.getString("orientation","portrait")).append("\n");
        text.append("System UI battery report: ").append(dev.chet.batterygradient.SettingsProvider.getStatus(this)).append("\n");
        SharedPreferences matchPrefs=getSharedPreferences("nav_match_v2",MODE_PRIVATE);
        text.append("\nNav Bar Match settings:\nmodule_enabled=").append(matchPrefs.getBoolean("enabled",true));
        text.append("\nblacklist=").append(matchPrefs.getStringSet("blacklist",java.util.Collections.emptySet()));
        text.append("\n\nApp access refresh:\n").append(getSharedPreferences("access_diagnostics_v2",MODE_PRIVATE).getString("result","Not run yet")).append('\n');
        text.append("Last app install/update refresh: ").append(getSharedPreferences("access_diagnostics_v2",MODE_PRIVATE).getString("package_result","None recorded")).append('\n');
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
