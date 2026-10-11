package dev.chet.gboardcursorkeys;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.function.Consumer;

/** Owns one nav host across detach/reattach; no process-wide strong view references. */
final class NavArrowSession implements View.OnAttachStateChangeListener {
    static final String VISIBILITY = "dev.chet.gboardcursorkeys.IME_VISIBILITY";
    static final String QUERY = "dev.chet.gboardcursorkeys.QUERY_IME";
    private final ViewGroup host;
    private final TextView left, right;
    private final Consumer<String> log;
    private boolean registered, visible;
    private int attempt;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!VISIBILITY.equals(intent.getAction()) || !registered) return;
            visible = intent.getBooleanExtra("visible", false);
            repairChildren();
            show();
            log.accept("Launcher arrows visible=" + visible);
        }
    };
    private final Runnable query = new Runnable() {
        @Override public void run() {
            if (!registered || !host.isAttachedToWindow()) return;
            try {
                host.getContext().sendBroadcast(new Intent(QUERY)
                        .setPackage("com.google.android.inputmethod.latin"));
            } catch (Throwable error) { log.accept("Launcher cursor query failed: " + error); }
            if (++attempt < 4) host.postDelayed(this, new long[]{250,750,2000}[attempt-1]);
        }
    };
    private final ViewTreeObserver.OnGlobalLayoutListener layout = this::repairChildren;

    NavArrowSession(ViewGroup host, TextView left, TextView right, Consumer<String> log) {
        this.host=host; this.left=left; this.right=right; this.log=log;
    }
    void start() {
        host.addOnAttachStateChangeListener(this);
        repairChildren();
        if (host.isAttachedToWindow()) connect();
    }
    void recover() {
        repairChildren();
        if (host.isAttachedToWindow()) {
            connect();
            requestState();
        }
    }
    private void connect() {
        if (registered) return;
        try {
            host.getContext().registerReceiver(receiver, new IntentFilter(VISIBILITY), Context.RECEIVER_EXPORTED);
            registered=true;
            host.getViewTreeObserver().addOnGlobalLayoutListener(layout);
            requestState();
            log.accept("Launcher cursor receiver connected");
        } catch (Throwable error) { log.accept("Launcher cursor reconnect failed: " + error); }
    }
    private void requestState() {
        host.removeCallbacks(query);
        attempt=0;
        host.post(query);
    }
    private void repairChildren() {
        try { add(left,true); add(right,false); show(); }
        catch (Throwable error) { log.accept("Launcher cursor repair failed: " + error); }
    }
    private void add(TextView button, boolean first) {
        if (button.getParent()==host) return;
        if (button.getParent() instanceof ViewGroup) ((ViewGroup)button.getParent()).removeView(button);
        int width=Math.round(40*host.getResources().getDisplayMetrics().density);
        if (host instanceof FrameLayout) {
            host.addView(button,new FrameLayout.LayoutParams(width,-1,first?Gravity.LEFT:Gravity.RIGHT));
        } else if (host instanceof LinearLayout) {
            host.addView(button,first?0:host.getChildCount(),new LinearLayout.LayoutParams(width,-1));
        }
    }
    private void show() {
        int state=registered && visible ? View.VISIBLE : View.GONE;
        if(left.getVisibility()!=state)left.setVisibility(state);
        if(right.getVisibility()!=state)right.setVisibility(state);
    }
    @Override public void onViewAttachedToWindow(View view) { recover(); }
    @Override public void onViewDetachedFromWindow(View view) {
        host.removeCallbacks(query);
        if (host.getViewTreeObserver().isAlive()) host.getViewTreeObserver().removeOnGlobalLayoutListener(layout);
        if (registered) {
            try { host.getContext().unregisterReceiver(receiver); } catch (IllegalArgumentException ignored) { }
        }
        registered=false;visible=false;show();
        // Retain the attach listener: this same host can return without controller init.
    }
}
