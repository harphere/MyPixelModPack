package dev.chet.gboardcursorkeys;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        TextView view = new TextView(this);
        view.setText("Gboard Cursor Keys\n\nEnable the module in LSPosed. Scope the pack to Gboard and Launcher3 (com.android.launcher3). Force stop Gboard and reopen a text field; restart Launcher3 or reboot.\n\nTap the arrows to move one character. Hold to repeat.");
        int p = (int)(24 * getResources().getDisplayMetrics().density);
        view.setPadding(p,p,p,p);
        setContentView(view);
    }
}
