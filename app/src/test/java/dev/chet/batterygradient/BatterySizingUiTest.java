package dev.chet.batterygradient;
import android.view.*;
import android.widget.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class BatterySizingUiTest {
    private RadioButton find(View view,String title) {
        if(view instanceof RadioButton && title.contentEquals(((RadioButton)view).getText())) return (RadioButton)view;
        if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) { var result=find(((ViewGroup)view).getChildAt(i),title);if(result!=null)return result; }
        return null;
    }
    @Test public void orientationControlsOnlyApplyToRectangleAndSaveSelection() {
        var controller=Robolectric.buildActivity(SettingsActivity.class).setup();var activity=controller.get();
        View root=activity.findViewById(android.R.id.content);
        RadioButton landscape=find(root,"Landscape"),rectangle=find(root,"Rectangular (portrait / landscape)");
        assertNotNull(landscape);assertFalse(landscape.isEnabled());rectangle.performClick();assertTrue(landscape.isEnabled());
        landscape.performClick();assertEquals("landscape",activity.getSharedPreferences("battery",0).getString("portrait_orientation",""));
        find(root,"Circle").performClick();assertFalse(landscape.isEnabled());
        controller.pause().stop().destroy();
    }
}
