package com.chet.navbarmatch;
import org.junit.Test;
import static org.junit.Assert.*;
public class NavIconContrastTest {
    @Test public void titaniumNearWhiteAndLightGreyUseBlackIcons() {
        for (int color : new int[]{0xfff9f9fc, 0xfff8f8fc, 0xfff4f4f4, 0xffeeeeee, 0xffe8e8e8, 0xffffffff})
            assertTrue(Integer.toHexString(color), NavIconContrast.useDarkIcons(color));
    }
    @Test public void colouredAndDarkBarsKeepWhiteIcons() {
        for (int color : new int[]{0xff000000, 0xff303030, 0xffff9800, 0xffff0000, 0xff2196f3, 0xffffeb3b})
            assertFalse(Integer.toHexString(color), NavIconContrast.useDarkIcons(color));
    }
    @Test public void brightnessAndNeutralityBothRequired() {
        assertTrue(NavIconContrast.useDarkIcons(0xffe8e8e8));
        assertFalse(NavIconContrast.useDarkIcons(0xffe7e7e7));
        assertTrue(NavIconContrast.useDarkIcons(0xffe8e8f8)); // Neutrality boundary
        assertFalse(NavIconContrast.useDarkIcons(0xffe8e8f9));
        assertFalse(NavIconContrast.useDarkIcons(0xffe6f0ff));
    }
}
