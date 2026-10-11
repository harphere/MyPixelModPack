package dev.chet.batterygradient;

import android.content.*;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;

/** Stops frames when detached, hidden, or the display is non-interactive. */
public final class AnimatedBatteryView extends ImageView {
    private boolean listening;
    private final BroadcastReceiver screen = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { updateAnimationVisibility(); }
    };
    public AnimatedBatteryView(Context c) { super(c); }
    @Override public void setImageDrawable(android.graphics.drawable.Drawable drawable) {
        super.setImageDrawable(drawable);
        updateAnimationVisibility();
    }
    private void updateAnimationVisibility() {
        if (!(getDrawable() instanceof GradientBatteryDrawable)) return;
        PowerManager power = (PowerManager)getContext().getSystemService(Context.POWER_SERVICE);
        boolean visible = isAttachedToWindow() && isShown()
                && getWindowVisibility() == View.VISIBLE && power != null && power.isInteractive();
        // ImageView can unschedule a drawable while moving/replacing it. Restart
        // the ticker after the callback and visible host have been restored.
        getDrawable().setVisible(visible, visible);
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON); filter.addAction(Intent.ACTION_SCREEN_OFF);
        getContext().registerReceiver(screen, filter); listening = true;
        updateAnimationVisibility();
    }
    @Override protected void onDetachedFromWindow() {
        if (getDrawable() instanceof GradientBatteryDrawable) {
            getDrawable().setVisible(false, false);
            ((GradientBatteryDrawable)getDrawable()).stopAnimation();
        }
        if (listening) { getContext().unregisterReceiver(screen); listening = false; }
        super.onDetachedFromWindow();
    }
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility); updateAnimationVisibility();
    }
    @Override public void onVisibilityAggregated(boolean visible) {
        super.onVisibilityAggregated(visible); updateAnimationVisibility();
    }
}
