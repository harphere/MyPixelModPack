/* Battery J coordinates adapted from Mahmud0808/Iconify battery_configs.xml.
 * Source commit: 5515543241d906af78ad44751ea96933a7ad7cc7.
 * Distributed under GPL-3.0; see LICENSE and THIRD_PARTY_NOTICES.md.
 * Modified for MyPixelModPack: Java Path commands, translation and Pack colour/level logic.
 */
package dev.chet.batterygradient;
import android.graphics.Path;
/** Iconify Battery J geometry on its 20x12 grid, centred in the Pack's icon lane. */
final class BatteryJPaths {
    private BatteryJPaths() { }
    static Path perimeter() {
        Path p = new Path();
        p.moveTo(2.641f, 1.352f);
        p.cubicTo(2.246f, 1.391f, 1.84f, 1.555f, 1.531f, 1.801f);
        p.cubicTo(1.121f, 2.125f, 0.871f, 2.551f, 0.77f, 3.09f);
        p.lineTo(0.746f, 3.203f);
        p.lineTo(0.742f, 5.922f);
        p.cubicTo(0.738f, 7.816f, 0.742f, 8.672f, 0.75f, 8.75f);
        p.cubicTo(0.82f, 9.488f, 1.262f, 10.125f, 1.922f, 10.445f);
        p.cubicTo(2.152f, 10.559f, 2.402f, 10.625f, 2.66f, 10.648f);
        p.cubicTo(2.82f, 10.664f, 14.949f, 10.664f, 15.109f, 10.648f);
        p.cubicTo(15.953f, 10.57f, 16.656f, 10.016f, 16.922f, 9.219f);
        p.cubicTo(17f, 8.98f, 17.031f, 8.781f, 17.031f, 8.5f);
        p.lineTo(17.031f, 8.34f);
        p.lineTo(17.672f, 8.336f);
        p.cubicTo(18.246f, 8.336f, 18.324f, 8.332f, 18.391f, 8.316f);
        p.cubicTo(18.621f, 8.262f, 18.813f, 8.156f, 18.969f, 8f);
        p.cubicTo(19.133f, 7.836f, 19.234f, 7.645f, 19.285f, 7.41f);
        p.cubicTo(19.305f, 7.328f, 19.305f, 7.242f, 19.305f, 6.004f);
        p.cubicTo(19.305f, 4.766f, 19.305f, 4.68f, 19.285f, 4.602f);
        p.cubicTo(19.195f, 4.176f, 18.906f, 3.855f, 18.5f, 3.727f);
        p.cubicTo(18.332f, 3.672f, 18.293f, 3.672f, 17.641f, 3.672f);
        p.lineTo(17.031f, 3.672f);
        p.lineTo(17.027f, 3.441f);
        p.cubicTo(17.02f, 3.102f, 16.973f, 2.875f, 16.855f, 2.605f);
        p.cubicTo(16.551f, 1.902f, 15.887f, 1.422f, 15.109f, 1.352f);
        p.cubicTo(14.965f, 1.336f, 2.781f, 1.34f, 2.641f, 1.352f);
        p.close();
        p.moveTo(14.883f, 2.488f);
        p.cubicTo(15.395f, 2.594f, 15.77f, 2.965f, 15.879f, 3.469f);
        p.cubicTo(15.895f, 3.551f, 15.895f, 3.672f, 15.895f, 6f);
        p.cubicTo(15.895f, 8.328f, 15.895f, 8.449f, 15.879f, 8.531f);
        p.cubicTo(15.773f, 9.012f, 15.426f, 9.375f, 14.953f, 9.492f);
        p.cubicTo(14.922f, 9.504f, 14.848f, 9.516f, 14.789f, 9.52f);
        p.cubicTo(14.645f, 9.535f, 3.125f, 9.535f, 2.98f, 9.52f);
        p.cubicTo(2.438f, 9.465f, 2.008f, 9.074f, 1.895f, 8.531f);
        p.cubicTo(1.875f, 8.449f, 1.875f, 8.328f, 1.875f, 6f);
        p.cubicTo(1.875f, 3.672f, 1.875f, 3.551f, 1.895f, 3.469f);
        p.cubicTo(2.012f, 2.918f, 2.449f, 2.527f, 3.004f, 2.477f);
        p.cubicTo(3.027f, 2.477f, 5.695f, 2.473f, 8.934f, 2.477f);
        p.cubicTo(13.504f, 2.477f, 14.84f, 2.48f, 14.883f, 2.488f);
        p.close();
        p.moveTo(17.906f, 4.82f);
        p.cubicTo(18.023f, 4.867f, 18.117f, 4.961f, 18.16f, 5.066f);
        p.lineTo(18.184f, 5.125f);
        p.lineTo(18.184f, 6f);
        p.cubicTo(18.184f, 6.82f, 18.184f, 6.879f, 18.168f, 6.926f);
        p.cubicTo(18.141f, 7.004f, 18.07f, 7.09f, 18f, 7.137f);
        p.cubicTo(17.898f, 7.203f, 17.863f, 7.211f, 17.414f, 7.211f);
        p.lineTo(17.031f, 7.211f);
        p.lineTo(17.031f, 4.801f);
        p.lineTo(17.449f, 4.801f);
        p.cubicTo(17.813f, 4.805f, 17.871f, 4.809f, 17.906f, 4.82f);
        p.close();
        p.moveTo(17.906f, 4.82f);
        p.offset(2f, 6f);
        return p;
    }
    static Path fillMask() {
        Path p = new Path();
        p.moveTo(12.391f, 6f);
        p.lineTo(12.391f, 8.398f);
        p.lineTo(13.359f, 8.398f);
        p.cubicTo(14.438f, 8.398f, 14.414f, 8.402f, 14.527f, 8.336f);
        p.cubicTo(14.648f, 8.262f, 14.734f, 8.141f, 14.762f, 8.012f);
        p.cubicTo(14.766f, 7.977f, 14.77f, 7.258f, 14.77f, 5.992f);
        p.cubicTo(14.77f, 3.848f, 14.773f, 3.977f, 14.715f, 3.859f);
        p.cubicTo(14.68f, 3.789f, 14.578f, 3.691f, 14.512f, 3.656f);
        p.cubicTo(14.402f, 3.602f, 14.406f, 3.602f, 13.359f, 3.602f);
        p.lineTo(12.391f, 3.602f);
        p.close();
        p.moveTo(12.391f, 6f);
        p.moveTo(3.371f, 3.613f);
        p.cubicTo(3.203f, 3.652f, 3.063f, 3.789f, 3.016f, 3.969f);
        p.cubicTo(3.004f, 4.016f, 3f, 4.281f, 3f, 5.992f);
        p.cubicTo(3f, 7.258f, 3.004f, 7.977f, 3.012f, 8.012f);
        p.cubicTo(3.035f, 8.141f, 3.121f, 8.262f, 3.242f, 8.336f);
        p.cubicTo(3.355f, 8.402f, 3.332f, 8.398f, 4.41f, 8.398f);
        p.lineTo(5.379f, 8.398f);
        p.lineTo(5.379f, 3.602f);
        p.lineTo(4.406f, 3.602f);
        p.cubicTo(3.629f, 3.602f, 3.422f, 3.605f, 3.371f, 3.613f);
        p.close();
        p.moveTo(3.371f, 3.613f);
        p.moveTo(6.129f, 6f);
        p.lineTo(6.129f, 8.398f);
        p.lineTo(8.512f, 8.398f);
        p.lineTo(8.512f, 3.602f);
        p.lineTo(6.129f, 3.602f);
        p.close();
        p.moveTo(6.129f, 6f);
        p.moveTo(9.262f, 6f);
        p.lineTo(9.262f, 8.398f);
        p.lineTo(11.641f, 8.398f);
        p.lineTo(11.641f, 3.602f);
        p.lineTo(9.262f, 3.602f);
        p.close();
        p.moveTo(9.262f, 6f);
        p.offset(2f, 6f);
        return p;
    }
}
