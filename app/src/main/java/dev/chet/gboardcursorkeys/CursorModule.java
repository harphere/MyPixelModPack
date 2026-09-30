package dev.chet.gboardcursorkeys;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.ViewParent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.SurroundingText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.Collections;
import java.util.Set;

public class CursorModule implements IXposedHookLoadPackage {
    private static final WeakHashMap<InputMethodService, WeakReference<FrameLayout>> overlays = new WeakHashMap<>();
    private static final int TAG = 0x4732434b;
    private static final String ACTION = "dev.chet.gboardcursorkeys.MOVE_CURSOR";
    private static final String VISIBILITY = "dev.chet.gboardcursorkeys.IME_VISIBILITY";
    private static final String QUERY = "dev.chet.gboardcursorkeys.QUERY_IME";
    private static final Set<ViewGroup> navHosts = Collections.newSetFromMap(new WeakHashMap<>());
    private static final WeakHashMap<InputMethodService, BroadcastReceiver> receivers = new WeakHashMap<>();
    private static void trace(String message) { XposedBridge.log("GboardCursorKeys: " + message); }

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if ("com.google.android.apps.nexuslauncher".equals(p.packageName)
                || "com.android.launcher3".equals(p.packageName)) {
            hookLauncher(p);
            return;
        }
        if (!"com.google.android.inputmethod.latin".equals(p.packageName)) return;
        trace("loaded in Gboard");
        XposedHelpers.findAndHookMethod(InputMethodService.class, "onWindowShown", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                InputMethodService ime = (InputMethodService)param.thisObject;
                ime.getWindow().getWindow().getDecorView().post(() -> {
                    registerReceiver(ime);
                    sendVisibility(ime, true);
                });
            }
        });
        XposedHelpers.findAndHookMethod(InputMethodService.class, "onWindowHidden", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                InputMethodService ime = (InputMethodService)param.thisObject;
                sendVisibility(ime, false);
            }
        });
        XposedHelpers.findAndHookMethod(InputMethodService.class, "onDestroy", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) { unregisterReceiver((InputMethodService)param.thisObject); }
        });
    }

    private static void registerReceiver(InputMethodService ime) {
        if (receivers.containsKey(ime)) return;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (QUERY.equals(intent.getAction())) {
                    sendVisibility(ime, ime.isInputViewShown());
                    return;
                }
                if (!ACTION.equals(intent.getAction()) || !ime.isInputViewShown()) return;
                int code = intent.getIntExtra("code", 0);
                if (code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT) move(ime, code);
            }
        };
        try {
            IntentFilter filter = new IntentFilter(ACTION);
            filter.addAction(QUERY);
            ime.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
            receivers.put(ime, receiver);
            trace("Gboard cursor receiver ready");
        } catch (Throwable error) { trace("receiver registration failed: " + error); }
    }

    private static void unregisterReceiver(InputMethodService ime) {
        BroadcastReceiver receiver = receivers.remove(ime);
        if (receiver != null) try { ime.unregisterReceiver(receiver); } catch (Throwable ignored) { }
    }

    private static void sendVisibility(Context context, boolean visible) {
        for (String pkg : new String[]{"com.android.launcher3", "com.google.android.apps.nexuslauncher"}) {
            try {
                Intent message = new Intent(VISIBILITY).setPackage(pkg);
                message.putExtra("visible", visible);
                context.sendBroadcast(message);
            } catch (Throwable error) { trace("visibility broadcast failed: " + error); }
        }
        trace("Gboard reported IME visible=" + visible);
    }

    private static void hookLauncher(XC_LoadPackage.LoadPackageParam p) {
        try {
            Class<?> controller = XposedHelpers.findClass(
                    "com.android.launcher3.taskbar.NavbarButtonsViewController", p.classLoader);
            XC_MethodHook capture = new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object home = XposedHelpers.getObjectField(param.thisObject, "mHomeButton");
                        Object back = XposedHelpers.getObjectField(param.thisObject, "mBackButton");
                        if (home instanceof View && back instanceof View)
                            ((View) home).post(() -> installNavButtons((View) home, (View) back));
                    } catch (Throwable error) { trace("Launcher home capture: " + error); }
                }
            };
            XposedBridge.hookAllMethods(controller, "init", capture);
            XposedBridge.hookAllMethods(controller, "onConfigurationChanged", capture);
            trace("Launcher controller hooked in " + p.packageName);
        } catch (Throwable error) { trace("Launcher controller unavailable: " + error); }
    }

    private static void installNavButtons(View home, View back) {
        ViewGroup host = null;
        for (ViewParent parent = home.getParent(); parent instanceof ViewGroup; parent = parent.getParent()) {
            if (contains((ViewGroup) parent, back)) { host = (ViewGroup) parent; break; }
        }
        if (host == null) { trace("Launcher shared navigation parent missing"); return; }
        ViewParent outer = host.getParent();
        final ViewGroup navHost = outer instanceof FrameLayout ? (ViewGroup) outer : host;
        if (navHosts.contains(navHost)) return;
        try {
            TextView left = navButton(navHost.getContext(), "‹", KeyEvent.KEYCODE_DPAD_LEFT);
            TextView right = navButton(navHost.getContext(), "›", KeyEvent.KEYCODE_DPAD_RIGHT);
            if (home instanceof android.widget.ImageView) {
                android.content.res.ColorStateList tint = ((android.widget.ImageView) home).getImageTintList();
                if (tint != null) {
                    left.setTextColor(tint); right.setTextColor(tint);
                }
            }
            int width = dp(navHost.getContext(), 40);
            if (navHost instanceof FrameLayout) {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, -1, Gravity.LEFT);
                FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(width, -1, Gravity.RIGHT);
                navHost.addView(left, lp);
                navHost.addView(right, rp);
            } else if (navHost instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(width, -1);
                navHost.addView(left, 0, params);
                navHost.addView(right, new android.widget.LinearLayout.LayoutParams(width, -1));
            } else {
                trace("unsupported navigation parent: " + navHost.getClass().getName()); return;
            }
            navHosts.add(navHost);
            BroadcastReceiver visibility = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    boolean shown = intent.getBooleanExtra("visible", false);
                    left.setVisibility(shown ? View.VISIBLE : View.GONE);
                    right.setVisibility(shown ? View.VISIBLE : View.GONE);
                    trace("Launcher arrows visible=" + shown);
                    if (shown) navHost.postDelayed(() -> {
                        int[] hostXY = new int[2], leftXY = new int[2], rightXY = new int[2];
                        navHost.getLocationOnScreen(hostXY);
                        left.getLocationOnScreen(leftXY);
                        right.getLocationOnScreen(rightXY);
                        trace("nav bounds host=" + hostXY[0] + "," + hostXY[1] + " "
                                + navHost.getWidth() + "x" + navHost.getHeight()
                                + " parent=" + (navHost.getParent() == null ? "null" : navHost.getParent().getClass().getName())
                                + " left=" + leftXY[0] + "," + leftXY[1] + " " + left.getWidth() + "x" + left.getHeight()
                                + " right=" + rightXY[0] + "," + rightXY[1] + " " + right.getWidth() + "x" + right.getHeight()
                                + " color=" + Integer.toHexString(left.getCurrentTextColor())
                                + " attached=" + navHost.isAttachedToWindow());
                    }, 100);
                }
            };
            navHost.getContext().registerReceiver(visibility, new IntentFilter(VISIBILITY), Context.RECEIVER_EXPORTED);
            navHost.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override public void onViewAttachedToWindow(View view) { }
                @Override public void onViewDetachedFromWindow(View view) {
                    try { view.getContext().unregisterReceiver(visibility); } catch (Throwable ignored) { }
                    navHosts.remove(navHost);
                    view.removeOnAttachStateChangeListener(this);
                }
            });
            navHost.getContext().sendBroadcast(new Intent(QUERY).setPackage("com.google.android.inputmethod.latin"));
            trace("Launcher nav arrows attached: " + navHost.getClass().getName());
        } catch (Throwable error) { trace("Launcher nav attach failed: " + error); }
    }

    private static boolean contains(ViewGroup group, View child) {
        for (ViewParent parent = child.getParent(); parent != null; parent = parent.getParent())
            if (parent == group) return true;
        return false;
    }

    private static TextView navButton(Context context, String glyph, int code) {
        TextView button = new TextView(context);
        button.setText(glyph);
        button.setTextSize(27);
        button.setGravity(Gravity.CENTER);
        button.setVisibility(View.GONE);
        button.setOnTouchListener(new View.OnTouchListener() {
            final Handler handler = new Handler(Looper.getMainLooper());
            boolean pressed;
            final Runnable repeat = new Runnable() {
                @Override public void run() {
                    if (pressed) { dispatch(context, code); handler.postDelayed(this, 75); }
                }
            };
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        pressed = true; view.setPressed(true); dispatch(context, code);
                        handler.postDelayed(repeat, 350); return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        pressed = false; view.setPressed(false); handler.removeCallbacks(repeat); return true;
                    default: return true;
                }
            }
        });
        return button;
    }

    private static void dispatch(Context context, int code) {
        try {
            Intent intent = new Intent(ACTION).setPackage("com.google.android.inputmethod.latin");
            intent.putExtra("code", code);
            context.sendBroadcast(intent);
            trace("Launcher sent " + (code == KeyEvent.KEYCODE_DPAD_LEFT ? "left" : "right"));
        } catch (Throwable error) { trace("Launcher dispatch failed: " + error); }
    }

    private static void attach(InputMethodService ime) {
        try {
            Window window = ime.getWindow().getWindow();
            ViewGroup decor = (ViewGroup) window.getDecorView();
            View existing = decor.findViewWithTag(TAG);
            if (existing != null) return;
            FrameLayout layer = new FrameLayout(ime);
            layer.setTag(TAG);
            layer.setClickable(false);
            layer.setFocusable(false);
            // The decor's bottom edge includes the IME navigation area on this ROM.
            // Draw above it so the arrows receive touches inside Gboard's window.
            int size = dp(ime, 44), edge = dp(ime, 3), bottom = dp(ime, 64);
            TextView left = button(ime, "‹", KeyEvent.KEYCODE_DPAD_LEFT);
            TextView right = button(ime, "›", KeyEvent.KEYCODE_DPAD_RIGHT);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size, Gravity.BOTTOM | Gravity.LEFT);
            lp.leftMargin = edge; lp.bottomMargin = bottom;
            layer.addView(left, lp);
            FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(size, size, Gravity.BOTTOM | Gravity.RIGHT);
            rp.rightMargin = edge; rp.bottomMargin = bottom;
            layer.addView(right, rp);
            decor.addView(layer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlays.put(ime, new WeakReference<>(layer));
            trace("arrows attached; window=" + decor.getClass().getName());
            layer.post(() -> {
                int[] leftScreen = new int[2];
                left.getLocationOnScreen(leftScreen);
                trace("left button bounds: x=" + leftScreen[0] + " y=" + leftScreen[1]
                        + " width=" + left.getWidth() + " height=" + left.getHeight()
                        + " decorHeight=" + decor.getHeight());
            });
        } catch (Throwable error) { trace("attach failed: " + error); }
    }

    private static TextView button(InputMethodService ime, String glyph, int keycode) {
        TextView view = new TextView(ime);
        view.setText(glyph);
        view.setTextSize(29);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        boolean night = (ime.getResources().getConfiguration().uiMode & 0x30) == 0x20;
        view.setTextColor(night ? Color.WHITE : Color.rgb(35, 38, 45));
        GradientDrawable background = new GradientDrawable();
        background.setColor(night ? 0xCC383A40 : 0xDDE7E9EE);
        background.setCornerRadius(dp(ime, 13));
        view.setBackground(background);
        view.setOnTouchListener(new RepeatListener(ime, keycode));
        return view;
    }

    private static void detach(InputMethodService ime) {
        WeakReference<FrameLayout> ref = overlays.remove(ime);
        FrameLayout layer = ref == null ? null : ref.get();
        if (layer != null && layer.getParent() instanceof ViewGroup) ((ViewGroup)layer.getParent()).removeView(layer);
    }

    private static int dp(Context context, int n) {
        return (int)(n * context.getResources().getDisplayMetrics().density + .5f);
    }

    private static void move(InputMethodService ime, int code) {
        InputConnection connection = ime.getCurrentInputConnection();
        if (connection == null) { trace("move: current InputConnection is null"); return; }
        trace("move: direction=" + (code == KeyEvent.KEYCODE_DPAD_LEFT ? "left" : "right")
                + " connection=" + connection.getClass().getName());
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                SurroundingText surrounding = connection.getSurroundingText(64, 64, 0);
                trace("surrounding: " + (surrounding == null ? "null" :
                        "offset=" + surrounding.getOffset() + " selection="
                                + surrounding.getSelectionStart() + "," + surrounding.getSelectionEnd()
                                + " length=" + (surrounding.getText() == null ? -1 : surrounding.getText().length())));
                if (surrounding != null && surrounding.getOffset() >= 0) {
                    int start = surrounding.getSelectionStart();
                    int end = surrounding.getSelectionEnd();
                    CharSequence content = surrounding.getText();
                    if (start >= 0 && end >= 0 && content != null
                            && start <= content.length() && end <= content.length()) {
                        int position;
                        if (code == KeyEvent.KEYCODE_DPAD_LEFT) {
                            position = Math.min(start, end);
                            if (start == end && position > 0) {
                                position--;
                                if (position > 0 && Character.isLowSurrogate(content.charAt(position))
                                        && Character.isHighSurrogate(content.charAt(position - 1))) position--;
                            }
                        } else {
                            position = Math.max(start, end);
                            if (start == end && position < content.length()) {
                                if (Character.isHighSurrogate(content.charAt(position))
                                        && position + 1 < content.length()
                                        && Character.isLowSurrogate(content.charAt(position + 1))) position++;
                                position++;
                            }
                        }
                        int absolute = surrounding.getOffset() + position;
                        boolean applied = connection.setSelection(absolute, absolute);
                        trace("surrounding setSelection(" + absolute + ")=" + applied);
                        if (applied) return;
                    }
                }
            }
            ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
            trace("extracted: " + (extracted == null ? "null" :
                    "offset=" + extracted.startOffset + " selection=" + extracted.selectionStart
                            + "," + extracted.selectionEnd + " length="
                            + (extracted.text == null ? -1 : extracted.text.length())));
            if (extracted != null && extracted.text != null && extracted.selectionStart >= 0
                    && extracted.selectionEnd >= 0 && extracted.startOffset >= 0) {
                int position = code == KeyEvent.KEYCODE_DPAD_LEFT
                        ? Math.min(extracted.selectionStart, extracted.selectionEnd)
                        : Math.max(extracted.selectionStart, extracted.selectionEnd);
                if (extracted.selectionStart == extracted.selectionEnd) {
                    position = code == KeyEvent.KEYCODE_DPAD_LEFT
                            ? Math.max(0, position - 1) : Math.min(extracted.text.length(), position + 1);
                }
                int absolute = extracted.startOffset + position;
                boolean applied = connection.setSelection(absolute, absolute);
                trace("extracted setSelection(" + absolute + ")=" + applied);
                if (applied) return;
            }
        } catch (Throwable error) { trace("selection failed: " + error); }
        // Fallback for editors that implement key events but not selection APIs.
        long time = SystemClock.uptimeMillis();
        boolean down = connection.sendKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_DOWN, code, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD));
        boolean up = connection.sendKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_UP, code, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD));
        trace("key fallback: down=" + down + " up=" + up);
    }

    private static class RepeatListener implements View.OnTouchListener {
        private final InputMethodService ime;
        private final int code;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean pressed;
        private final Runnable repeat = new Runnable() {
            @Override public void run() {
                if (pressed) { move(ime, code); handler.postDelayed(this, 75); }
            }
        };
        RepeatListener(InputMethodService ime, int code) { this.ime = ime; this.code = code; }
        @Override public boolean onTouch(View view, MotionEvent event) {
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    trace("touch DOWN: " + (code == KeyEvent.KEYCODE_DPAD_LEFT ? "left" : "right"));
                    pressed = true; view.setPressed(true); move(ime, code);
                    handler.postDelayed(repeat, 350); return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    trace("touch " + (event.getActionMasked() == MotionEvent.ACTION_UP ? "UP" : "CANCEL"));
                    pressed = false; view.setPressed(false); handler.removeCallbacks(repeat); return true;
                default: return true;
            }
        }
    }
}
