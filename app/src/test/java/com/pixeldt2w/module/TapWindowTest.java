package com.pixeldt2w.module;
import org.junit.Test;
import static org.junit.Assert.*;
public class TapWindowTest {
 @Test public void acceptsMeasuredPairs(){for(long gap:new long[]{167,185,400,500}){var w=new TapWindow();assertEquals(TapWindow.Result.FIRST,w.tap(1000));assertEquals(TapWindow.Result.SECOND,w.tap(1000+gap));}}
 @Test public void sleepingPastWindowCannotPairStaleTap(){var w=new TapWindow();w.tap(1000);assertEquals(TapWindow.Result.FIRST,w.tap(9000));assertEquals(TapWindow.Result.SECOND,w.tap(9185));}
 @Test public void duplicateDoesNotReplaceFirstTimestamp(){var w=new TapWindow();w.tap(1000);assertEquals(TapWindow.Result.DUPLICATE,w.tap(1010));assertEquals(TapWindow.Result.SECOND,w.tap(1185));}
 @Test public void resetAndCompletedPairStartFresh(){var w=new TapWindow();w.tap(1000);w.reset();assertEquals(TapWindow.Result.FIRST,w.tap(1100));assertEquals(TapWindow.Result.SECOND,w.tap(1270));assertEquals(TapWindow.Result.FIRST,w.tap(1450));}
}
