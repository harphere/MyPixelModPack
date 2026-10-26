package dev.chet.mypixelmodpack;
import org.junit.Test;
import static org.junit.Assert.*;
public class IconTapTargetingTest {
 private int hit(float x,float y){return IconTapTargeting.pick(x,y,0,0,200,40,new float[]{20,44,68},new float[]{40,64,88},6);}
 @Test public void iconCanBeTappedAboveOrBelowItsArtwork(){assertEquals(0,hit(30,1));assertEquals(0,hit(30,39));}
 @Test public void adjacentIconsSelectExactlyOne(){assertEquals(0,hit(30,20));assertEquals(1,hit(54,20));assertEquals(2,hit(78,20));assertEquals(0,hit(42,20));assertEquals(1,hit(43,20));}
 @Test public void unrelatedSlotIsNotSkipped(){assertEquals(1,hit(54,20));/* Caller filters support AFTER selecting this slot. */}
 @Test public void clockAndOutsideBarDoNotSelectIcon(){assertEquals(-1,hit(0,20));assertEquals(-1,hit(120,20));assertEquals(-1,hit(30,40));assertEquals(-1,hit(30,-1));}
 @Test public void emptyOrZeroWidthIconsDoNotMatch(){assertEquals(-1,IconTapTargeting.pick(20,20,0,0,100,40,new float[0],new float[0],6));assertEquals(-1,IconTapTargeting.pick(20,20,0,0,100,40,new float[]{20},new float[]{20},6));}
 @Test public void screenOffsetIsRespected(){assertEquals(0,IconTapTargeting.pick(130,60,100,50,300,90,new float[]{120},new float[]{140},6));}
}
