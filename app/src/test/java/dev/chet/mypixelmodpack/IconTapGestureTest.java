package dev.chet.mypixelmodpack;
import org.junit.Test;
import static org.junit.Assert.*;
public class IconTapGestureTest {
 @Test public void shortTapWithSmallMovementQualifies(){IconTapGesture g=new IconTapGesture();g.begin(10,10,100,8,500);assertTrue(g.update(12,11,200,1));}
 @Test public void dragCannotRecoverByReturningToStart(){IconTapGesture g=new IconTapGesture();g.begin(10,10,100,8,500);assertFalse(g.update(10,30,150,1));assertFalse(g.update(10,10,200,1));}
 @Test public void extraFingerCancelsEntireGesture(){IconTapGesture g=new IconTapGesture();g.begin(10,10,100,8,500);assertFalse(g.update(10,10,150,2));assertFalse(g.update(10,10,200,1));}
 @Test public void longPressAndCancellationDoNotQualify(){IconTapGesture g=new IconTapGesture();g.begin(10,10,100,8,500);assertFalse(g.update(10,10,601,1));g.begin(10,10,700,8,500);g.cancel();assertFalse(g.update(10,10,800,1));}
 @Test public void freshTapAfterCancelledGestureCanQualify(){IconTapGesture g=new IconTapGesture();g.begin(0,0,0,8,500);g.cancel();g.begin(1,1,1000,8,500);assertTrue(g.update(1,1,1100,1));}
}
