package com.chet.navbarmatch;

/** Black glyphs on white/near-white neutral bars; retain white on coloured/dark bars. */
final class NavIconContrast {
    private NavIconContrast() { }
    static boolean useDarkIcons(int color) {
        int r = (color >>> 16) & 255, g = (color >>> 8) & 255, b = color & 255;
        int min = Math.min(r, Math.min(g, b));
        int max = Math.max(r, Math.max(g, b));
        return min >= 232 && max - min <= 16;
    }
}
