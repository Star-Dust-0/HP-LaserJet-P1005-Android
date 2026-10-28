package com.mohamedelnaggar.hpp1005;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import java.lang.reflect.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LayoutPreviewTest {
    private final Instrumentation ins = InstrumentationRegistry.getInstrumentation();
    private MainActivity activity;
    private Object field(String name) throws Exception {
        Field f=MainActivity.class.getDeclaredField(name); f.setAccessible(true); return f.get(activity);
    }
    private void set(String name,Object value) throws Exception {
        Field f=MainActivity.class.getDeclaredField(name); f.setAccessible(true); f.set(activity,value);
    }
    private void call(String name) throws Exception {
        Method m=MainActivity.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(activity);
    }
    private void ui(Runnable r) { ins.runOnMainSync(r); }
    private void update(String name,Object value) {
        ui(()->{try{set(name,value);call("renderApp");}catch(Exception e){throw new RuntimeException(e);}});
    }
    private void ready() throws Exception {
        long deadline=System.currentTimeMillis()+20000;
        while(System.currentTimeMillis()<deadline){
            ins.waitForIdleSync();
            final boolean[] done={false};
            ui(()->{try{done[0]=((TextView)field("layoutPreviewStatus")).getVisibility()==View.GONE;}catch(Exception e){throw new RuntimeException(e);}});
            if(done[0]) return;
            Thread.sleep(80);
        }
        fail("preview did not become ready");
    }
    private void capture(String name) throws Exception {
        ins.waitForIdleSync();
        Thread.sleep(250); // Allow the compositor to present the last UI-thread update.
        Bitmap b=ins.getUiAutomation().takeScreenshot();
        File dir=new File(ins.getTargetContext().getExternalFilesDir(null),"layout-shots");dir.mkdirs();
        try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();
    }
    private TextView find(View v,String text){
        if(v instanceof TextView && text.contentEquals(((TextView)v).getText()))return (TextView)v;
        if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){TextView r=find(((ViewGroup)v).getChildAt(i),text);if(r!=null)return r;}
        return null;
    }
    private void tapMode(String mode) {
        final String title="booklet".equals(mode)?"كتيّب":"multiple".equals(mode)?"متعدد":"poster".equals(mode)?"ملصق":"الحجم";
        ui(()->{TextView t=find(activity.getWindow().getDecorView(),title);assertNotNull(t);t.performClick();});
    }
    @Test public void realDocumentModesAndRapidEdits() throws Exception {
        Intent launch=ins.getTargetContext().getPackageManager().getLaunchIntentForPackage("com.hpp1005.printer");
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity=(MainActivity)ins.startActivitySync(launch);
        ins.waitForIdleSync();
        File pdf=new File(ins.getTargetContext().getCacheDir(),"numbered.pdf");
        try(InputStream in=ins.getContext().getAssets().open("numbered.pdf");OutputStream out=new FileOutputStream(pdf)){
            byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);
        }
        ui(()->{try{
            Method load=MainActivity.class.getDeclaredMethod("loadSelectedDocument",Uri.class);load.setAccessible(true);load.invoke(activity,Uri.fromFile(pdf));
            Method open=MainActivity.class.getDeclaredMethod("openPage",String.class);open.setAccessible(true);open.invoke(activity,"layout");
        }catch(Exception e){throw new RuntimeException(e);}});
        ready();capture("01-size");
        Object initialTrack=field("layoutModePill");
        for(String mode:new String[]{"multiple","booklet","poster","size"}){
            tapMode(mode);ready();capture("02-"+mode);
            assertSame("selector must survive updates",initialTrack,field("layoutModePill"));
        }
        // Actual taps, not just preference mutation, exercise persistent indicator animation.
        ui(()->{TextView t=find(activity.getWindow().getDecorView(),"متعدد");assertNotNull(t);t.performClick();});
        ready();Thread.sleep(350);capture("03-mode-tap");
        for(int n:new int[]{4,6,9,16,2}) update("pagesPerSheet",n);
        ready();assertEquals(2,field("pagesPerSheet"));capture("04-latest-two-up");
        update("layoutOrientation","landscape");ready();capture("05-landscape");
        update("readingOrder","ltr");ready();capture("06-ltr");
        ui(()->{try{((View)field("layoutNext")).performClick();}catch(Exception e){throw new RuntimeException(e);}});
        ready();capture("07-next-face");
        tapMode("booklet");update("bookletBinding","left");ready();capture("08-left-binding");
        tapMode("poster");update("posterScalePercent",200f);update("posterOverlapMm",10);update("posterCutMarks",false);ready();capture("09-poster-settings");
        tapMode("size");update("scaleModePref","custom");update("customScalePercent",75);ready();capture("10-custom-scale");
        update("pageRange","bad-range");
        ins.waitForIdleSync();
        assertNotEquals(View.GONE,((TextView)field("layoutPreviewStatus")).getVisibility());
        assertFalse(((View)field("layoutNext")).isEnabled());
        assertEquals("",((TextView)field("layoutPreviewCount")).getText().toString());capture("11-invalid-range");
        update("pageRange","3,3");ready();capture("12-dedup-range");
        tapMode("booklet");ready();capture("13-single-booklet-face");
        // Leave during in-flight work and return. No detached-image result may be installed.
        ui(()->{try{Method m=MainActivity.class.getDeclaredMethod("openPage",String.class);m.setAccessible(true);m.invoke(activity,"preview");m.invoke(activity,"layout");}catch(Exception e){throw new RuntimeException(e);}});
        ready();capture("14-return");
        update("themeMode","dark");
        ui(()->{try{set("darkMode",true);set("layoutSettingsHost",null);call("renderApp");}catch(Exception e){throw new RuntimeException(e);}});
        ready();capture("15-dark-booklet");
        tapMode("multiple");ready();capture("16-dark-multiple");
        update("selectedDocumentUri",null);capture("15-no-document");
        ui(()->activity.finish());
    }
}
