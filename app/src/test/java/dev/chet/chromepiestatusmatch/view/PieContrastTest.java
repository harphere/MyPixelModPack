package dev.chet.chromepiestatusmatch.view;
import android.graphics.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class PieContrastTest {
    private PieMenu menu(String mode) {
        var context=RuntimeEnvironment.getApplication();
        var prefs=context.getSharedPreferences("chromepie_v1",0);
        prefs.edit().putString("pie_icon_color_mode",mode).commit();
        return new PieMenu(context,context.getResources(),prefs);
    }
    private Paint paint(PieMenu menu,String name) throws Exception {
        var f=PieMenu.class.getDeclaredField(name);f.setAccessible(true);return (Paint)f.get(menu);
    }
    private int foreground(PieMenu menu,int color) throws Exception {
        var m=PieMenu.class.getDeclaredMethod("contrastForeground",int.class);m.setAccessible(true);
        return (int)m.invoke(menu,color);
    }
    @Test public void wikipediaGreyIsOpaqueWithBlackIconsDespiteWhitePreference() throws Exception {
        PieMenu menu=menu("white");menu.setThemeColors(Color.rgb(234,236,240));
        assertEquals(Color.rgb(232,232,232),paint(menu,"mNormalPaint").getColor());
        for(String name:new String[]{"mNormalPaint","mSelectedPaint","mSubPaint"}) {
            int color=paint(menu,name).getColor();assertEquals(255,Color.alpha(color));assertEquals(Color.BLACK,foreground(menu,color));
        }
    }
    @Test public void whitePageKeepsOpaqueGreyAndDistinctSelectedSlice() throws Exception {
        PieMenu menu=menu("auto");menu.setThemeColors(Color.WHITE);
        assertEquals(Color.rgb(232,232,232),paint(menu,"mNormalPaint").getColor());
        assertNotEquals(paint(menu,"mNormalPaint").getColor(),paint(menu,"mSelectedPaint").getColor());
    }
    @Test public void orangeAndBlackRetainWhiteIconPreference() throws Exception {
        PieMenu menu=menu("white");
        for(int color:new int[]{Color.rgb(255,152,0),Color.BLACK}) {
            menu.setThemeColors(color);assertEquals(Color.WHITE,foreground(menu,paint(menu,"mNormalPaint").getColor()));
            assertEquals(224,Color.alpha(paint(menu,"mNormalPaint").getColor()));
        }
    }
}
