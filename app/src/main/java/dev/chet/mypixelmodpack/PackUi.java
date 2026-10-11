package dev.chet.mypixelmodpack;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** UI-only palette and layouts; never loaded by feature hooks. */
public final class PackUi {
    private PackUi() {}
    public static int dp(Context c, int v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }
    public static boolean dark(Context c) { return (c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES; }
    public static int background(Context c) { return dark(c) ? 0xff111318 : 0xfff7f9fc; }
    public static int surface(Context c) { return dark(c) ? 0xff20242b : 0xffffffff; }
    public static int text(Context c) { return dark(c) ? 0xffe3e7ee : 0xff19212c; }
    public static int muted(Context c) { return dark(c) ? 0xffb7c0cd : 0xff546170; }
    public static int accent(Context c) {
        return c.getColor(dark(c) ? android.R.color.system_accent1_200 : android.R.color.system_accent1_600);
    }
    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    public static TextView label(Context c, String value, int size, boolean bold) {
        TextView t = new TextView(c); t.setText(value); t.setTextSize(size); t.setTextColor(text(c));
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    public static LinearLayout card(Context c) {
        LinearLayout l = column(c); int pad = dp(c, 20); l.setPadding(pad,pad,pad,pad);
        GradientDrawable b = new GradientDrawable(); b.setColor(surface(c)); b.setCornerRadius(dp(c,24)); l.setBackground(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(c,12); l.setLayoutParams(lp); return l;
    }
    public static void clickable(View v) {
        TypedValue value = new TypedValue(); v.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
        if (value.resourceId != 0) v.setForeground(v.getContext().getDrawable(value.resourceId));
        v.setFocusable(true); v.setClickable(true);
    }
    public static View header(Activity a, String title) {
        LinearLayout row = new LinearLayout(a); row.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=label(a,"‹",32,false); back.setGravity(Gravity.CENTER); back.setContentDescription("Back");
        row.addView(back,new LinearLayout.LayoutParams(dp(a,56),dp(a,56))); clickable(back); back.setOnClickListener(v -> a.finish());
        row.addView(label(a,title,20,true),new LinearLayout.LayoutParams(0,-2,1)); return row;
    }
}
