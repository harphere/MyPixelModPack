package dev.chet.mypixelmodpack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35, qualifiers="w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class UiPreviewTest {
    @Test public void homeAndNavigationRenderAtPhoneSize() throws Exception {
        render(null,"home-light.png"); render("Navigation","navigation-light.png");
    }
    @Test @Config(qualifiers="w393dp-h852dp-night-xhdpi") public void homeRendersInDarkTheme() throws Exception {
        render(null,"home-dark.png");
    }
    private void render(String section,String file) throws Exception {
        var app=RuntimeEnvironment.getApplication();
        Intent intent=new Intent(app,HomeActivity.class); if(section!=null) intent.putExtra("section",section);
        try(var controller=Robolectric.buildActivity(HomeActivity.class,intent).setup()) {
            View view=controller.get().findViewById(android.R.id.content);
            int w=786,h=1704;
            view.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));
            view.layout(0,0,w,h);
            Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(bitmap));
            assertEquals(w,view.getWidth()); assertEquals(h,view.getHeight());
            String dir=System.getProperty("pack.preview.dir");
            if(dir!=null) {
                File out=new File(dir,file); out.getParentFile().mkdirs();
                try(FileOutputStream stream=new FileOutputStream(out)) { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)); }
            }
            bitmap.recycle();
        }
    }
}
