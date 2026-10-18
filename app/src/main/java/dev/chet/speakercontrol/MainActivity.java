package dev.chet.speakercontrol;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.telecom.TelecomManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private TextView status;
    private String dialer;
    private boolean registered;
    private String pendingRequest;
    private android.widget.Spinner target;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeout = () -> status.setText("No reply from selected call app. Check scope, restart that app, and start/answer a call. WhatsApp must enter communication audio before its receiver responds.");
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!Protocol.TOKEN.equals(intent.getStringExtra(Protocol.KEY))) return;
            if (!java.util.Objects.equals(pendingRequest,intent.getStringExtra("request_id"))) return;
            handler.removeCallbacks(timeout);
            status.setText("App: " + intent.getStringExtra("backend_package") + "\nSuccess: " + intent.getBooleanExtra("success", false) + "\n" + intent.getStringExtra("status"));
        }
    };
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this); title.setText("SpeakerControl • My Pixel Mod Pack"); title.setTextSize(26); layout.addView(title);
        TextView help = new TextView(this);
        help.setText("Phone calls only. Scope to your default Phone app in LSPosed."); layout.addView(help);
        target = new android.widget.Spinner(this);
        target.setAdapter(new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Auto", "Phone", "WhatsApp", "WhatsApp Business"}));
        layout.addView(target);
        for (String command : new String[]{"ENABLE", "DISABLE", "TOGGLE", "STATUS"}) {
            Button button = new Button(this); button.setText(command); button.setOnClickListener(v -> send(command)); layout.addView(button);
        }
        TextView key = new TextView(this); key.setText("Tasker TOKEN (keep private):\n" + Protocol.TOKEN); key.setTextIsSelectable(true); layout.addView(key);
        status = new TextView(this); status.setText("Press STATUS to check the framework connection."); status.setTextIsSelectable(true); layout.addView(status);
        ScrollView scroll = new ScrollView(this); scroll.addView(layout); setContentView(scroll);
        TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        dialer = tm == null ? null : tm.getDefaultDialerPackage();
        help.setText("Default dialer: " + dialer + "\nScope the Pack to the Phone app and WhatsApp (or WhatsApp Business). Enable SpeakerControl and reboot. Choose Auto to detect the active call app, or select it below and start/answer a call before testing. ENABLE = speaker; DISABLE = earpiece. WhatsApp communication mode is used to detect call audio; video calls may not offer an earpiece route.");
    }
    @Override protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter(Protocol.PREFIX + "RESULT"), Context.RECEIVER_EXPORTED);
        registered = true;
    }
    @Override protected void onStop() {
        handler.removeCallbacks(timeout);
        if (registered) { unregisterReceiver(receiver); registered = false; }
        super.onStop();
    }
    private void send(String command) {
        String destination = target.getSelectedItemPosition()==0 ? Protocol.APP : target.getSelectedItemPosition()==2 ? "com.whatsapp" : target.getSelectedItemPosition()==3 ? "com.whatsapp.w4b" : dialer;
        if (destination == null || destination.isEmpty()) { status.setText("No default dialer detected."); return; }
        status.setText("Waiting for " + destination + "…");
        handler.removeCallbacks(timeout); handler.postDelayed(timeout, 5000);
        pendingRequest = java.util.UUID.randomUUID().toString();
        sendBroadcast(new Intent(Protocol.PREFIX + command).setPackage(destination)
                .putExtra(Protocol.KEY, Protocol.TOKEN).putExtra("request_id",pendingRequest));
    }
}
