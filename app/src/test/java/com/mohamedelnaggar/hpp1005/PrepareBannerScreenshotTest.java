package com.mohamedelnaggar.hpp1005;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/* Renders the real MainActivity home screen with forced auto-prepare states
   and captures PNGs, so the preparing / ready / failed banners can be checked
   visually without a physical printer. Output: app/build/outputs/prepare_screenshots/. */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 34)
public class PrepareBannerScreenshotTest {

    private static final String FAILURE_AR = "ما زالت الطابعة لا ترسل إصدار البرنامج الثابت بعد التحميل.";
    private static final String FAILURE_EN = "The printer still does not report a firmware version after loading.";

    @Test
    public void captureAllPrepareStates() throws Exception {
        File outDir = new File("build/outputs/prepare_screenshots");
        outDir.mkdirs();
        for (String lang : new String[]{"ar", "en"}) {
            for (String theme : new String[]{"light", "dark"}) {
                capture(lang, theme, 1, 1, "", "preparing-checking", outDir);
                capture(lang, theme, 1, 2, "", "preparing-loading", outDir);
                capture(lang, theme, 2, 0, "", "ready", outDir);
                capture(lang, theme, 3, 0, "ar".equals(lang) ? FAILURE_AR : FAILURE_EN, "failed", outDir);
            }
        }
        System.out.println("screenshots written to " + outDir.getAbsolutePath());
    }

    private void capture(String lang, String theme, int state, int detail, String reason, String name, File outDir) throws Exception {
        RuntimeEnvironment.getApplication().getSharedPreferences("hp_p1005_preferences", 0)
                .edit()
                .putString("app_language", lang)
                .putString("theme_mode", theme)
                .commit();
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        setField(activity, "prepareState", state);
        setField(activity, "prepareDetail", detail);
        setField(activity, "prepareFailureReason", reason);
        setField(activity, "prepareReadyBannerDismissed", false);
        Method renderApp = MainActivity.class.getDeclaredMethod("renderApp");
        renderApp.setAccessible(true);
        renderApp.invoke(activity);
        View root = activity.getWindow().getDecorView();
        int width = 1080;
        int height = 2340;
        root.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bitmap));
        File out = new File(outDir, name + "-" + lang + "-" + theme + ".png");
        FileOutputStream stream = new FileOutputStream(out);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();
        activity.finish();
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = MainActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
