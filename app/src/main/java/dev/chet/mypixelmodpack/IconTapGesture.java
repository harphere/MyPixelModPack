package dev.chet.mypixelmodpack;

/** Gesture eligibility only; no Android dependencies or event consumption. */
final class IconTapGesture {
    private boolean active;
    private float x,y,slop;
    private long time,timeout;
    void begin(float x,float y,long time,float slop,long timeout) {
        this.x=x; this.y=y; this.time=time; this.slop=slop; this.timeout=timeout; active=true;
    }
    boolean update(float x,float y,long time,int pointers) {
        if(pointers!=1 || time<this.time || time-this.time>timeout || Math.hypot(x-this.x,y-this.y)>slop) active=false;
        return active;
    }
    void cancel() { active=false; }
}
