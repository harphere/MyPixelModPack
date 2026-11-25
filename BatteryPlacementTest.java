package dev.chet.batterygradient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class BatteryPlacementTest {
    private final Context context=RuntimeEnvironment.getApplication();
    private final LinearLayout root=new LinearLayout(context),end=new LinearLayout(context),
        batteryRow=new LinearLayout(context),signalRow=new LinearLayout(context),systemIcons=new LinearLayout(context);
    private void setup() {
        end.setTag(BatteryPlacement.END_SIDE);root.addView(end);
        end.addView(batteryRow);end.addView(signalRow);signalRow.addView(systemIcons);
    }
    @Test public void nativeBatteryStillInSignalRowUsesSeparateBatteryRow() {
        setup();View nativeBattery=new View(context);systemIcons.addView(nativeBattery);
        assertSame(batteryRow,BatteryPlacement.destination(root,systemIcons,nativeBattery));
    }
    @Test public void swappedRowsKeepLogicalBatteryDestination() {
        setup();end.removeView(signalRow);end.addView(signalRow,0);
        assertSame(batteryRow,BatteryPlacement.destination(root,systemIcons,null));
    }
    @Test public void landscapeSingleRowStillUsesBatteryBranch() {
        setup();end.setOrientation(LinearLayout.HORIZONTAL);
        assertSame(batteryRow,BatteryPlacement.destination(root,systemIcons,null));
    }
    @Test public void iconifyCustomBatteryIsFoundAfterMovingOutOfSystemIcons() {
        setup();View tagged=new View(context);tagged.setTag(BatteryPlacement.ICONIFY_BATTERY);batteryRow.addView(tagged);
        assertSame(tagged,BatteryPlacement.anchor(root,systemIcons,null));
        assertSame(batteryRow,BatteryPlacement.destination(root,systemIcons,tagged));
    }
    @Test public void unknownTreeStaysWithNativeBatteryParent() {
        root.addView(systemIcons);View nativeBattery=new View(context);systemIcons.addView(nativeBattery);
        assertSame(systemIcons,BatteryPlacement.destination(root,systemIcons,nativeBattery));
    }
    @Test public void unrelatedTaggedContainerDoesNotStealBattery() {
        root.addView(systemIcons);LinearLayout unrelated=new LinearLayout(context);unrelated.setTag(BatteryPlacement.END_SIDE);root.addView(unrelated);
        unrelated.addView(new LinearLayout(context));unrelated.addView(new LinearLayout(context));
        assertSame(systemIcons,BatteryPlacement.destination(root,systemIcons,null));
    }
}
