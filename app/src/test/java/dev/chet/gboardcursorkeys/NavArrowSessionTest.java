package dev.chet.gboardcursorkeys;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.*;
import java.time.Duration;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class NavArrowSessionTest {
    ActivityController<Activity> controller;
    Activity activity;
    FrameLayout root,host;
    TextView left,right;
    NavArrowSession session;
    List<String> logs=new ArrayList<>();
    @Before public void setup() {
        controller=Robolectric.buildActivity(Activity.class).setup().visible();
        activity=controller.get();root=new FrameLayout(activity);activity.setContentView(root);
        host=new FrameLayout(activity);root.addView(host);
        left=new TextView(activity);right=new TextView(activity);
        session=new NavArrowSession(host,left,right,logs::add);session.start();
        assertTrue(host.isAttachedToWindow());
    }
    @After public void cleanup() { controller.pause().stop().destroy(); }
    void send(boolean shown) {
        activity.sendBroadcast(new Intent(NavArrowSession.VISIBILITY).putExtra("visible",shown));
        shadowOf(android.os.Looper.getMainLooper()).idle();
    }
    long queries() {
        return shadowOf(RuntimeEnvironment.getApplication()).getBroadcastIntents().stream()
                .filter(i->NavArrowSession.QUERY.equals(i.getAction())).count();
    }
    @Test public void detachedHostReconnectsWithoutControllerRecreation() {
        send(true);assertEquals(View.VISIBLE,left.getVisibility());
        root.removeView(host);assertEquals(View.GONE,left.getVisibility());
        root.addView(host);assertEquals(View.GONE,left.getVisibility());
        send(true);assertEquals(View.VISIBLE,left.getVisibility());
        send(false);assertEquals(View.GONE,right.getVisibility());
        assertEquals(2,host.getChildCount());
    }
    @Test public void removedArrowChildrenAreRepairedOnLayout() {
        send(true);host.removeAllViews();
        host.getViewTreeObserver().dispatchOnGlobalLayout();
        assertEquals(2,host.getChildCount());assertSame(host,left.getParent());
        assertEquals(View.VISIBLE,right.getVisibility());
    }
    @Test public void queriesAreBoundedAndStopAfterDetach() {
        shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(10));
        assertEquals(4,queries());
        root.removeView(host);
        shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(30));
        assertEquals(4,queries());
        root.addView(host);shadowOf(android.os.Looper.getMainLooper()).idle();
        assertEquals(5,queries());root.removeView(host);
        shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(10));
        assertEquals(5,queries());
    }
    @Test public void repeatedRecoveryDoesNotDuplicateViewsOrReceivers() {
        session.recover();session.recover();session.recover();send(true);
        assertEquals(2,host.getChildCount());
        assertEquals(1,logs.stream().filter(s->s.equals("Launcher arrows visible=true")).count());
        shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(10));
        assertEquals(4,queries());
    }
}
