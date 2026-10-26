package dev.chet.mypixelmodpack;

/** One horizontal icon slot, using the full status-bar height as its touch target. */
final class IconTapTargeting {
    static int pick(float x,float y,float barLeft,float barTop,float barRight,float barBottom,
                    float[] left,float[] right,float gap) {
        if(x<barLeft || x>=barRight || y<barTop || y>=barBottom) return -1;
        int selected=-1; float best=Float.MAX_VALUE;
        for(int i=0;i<left.length;i++) {
            if(right[i]<=left[i] || x<left[i]-gap || x>=right[i]+gap) continue;
            float distance=Math.abs(x-(left[i]+right[i])/2f);
            if(distance<best) { best=distance; selected=i; }
        }
        return selected;
    }
}
