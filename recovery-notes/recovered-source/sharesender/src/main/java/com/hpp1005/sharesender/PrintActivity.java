package com.hpp1005.sharesender;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * CI driver for the system print path. Generates its own 4-page A4 PDF with
 * a unique solid background per page plus a 100mm calibration square, then
 * hands it to the Android print framework so the SYSTEM print dialog opens.
 * onWrite honors the requested page ranges per the PrintDocumentAdapter
 * contract (writes only the requested pages), letting CI verify that the
 * print service does not double-filter ranges and applies copies correctly.
 * Stays alive (translucent) behind the dialog while the job runs.
 */
public class PrintActivity extends Activity {
    private static final String TAG = "HP1005-SysPrint";
    private static final int PAGE_COUNT = 4;
    private static final int[] PAGE_COLORS = { 0xD32F2F, 0x1976D2, 0x388E3C, 0xF9A825 };
    private static final int PAGE_W_PT = 595;  // A4
    private static final int PAGE_H_PT = 842;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        for (int i = 0; i < PAGE_COUNT; i++) {
            Log.i(TAG, "generated page " + (i + 1) + " sig #"
                    + Integer.toHexString(PAGE_COLORS[i] & 0xFFFFFF));
        }
        PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        pm.print("hpp1005-smoke", new PrintDocumentAdapter() {
            @Override public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                           CancellationSignal cancellationSignal,
                                           LayoutResultCallback callback, Bundle extras) {
                PrintDocumentInfo info = new PrintDocumentInfo.Builder("generated-4page.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PAGE_COUNT)
                        .build();
                callback.onLayoutFinished(info, false);
            }

            @Override public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                          CancellationSignal cancellationSignal,
                                          WriteResultCallback callback) {
                try {
                    ArrayList<Integer> wanted = new ArrayList<>();
                    for (PageRange range : pages) {
                        if (PageRange.ALL_PAGES.equals(range)) {
                            wanted.clear();
                            for (int i = 0; i < PAGE_COUNT; i++) wanted.add(i);
                            break;
                        }
                        for (int p = range.getStart(); p <= range.getEnd(); p++) wanted.add(p);
                    }
                    StringBuilder names = new StringBuilder();
                    for (int p : wanted) names.append(p + 1).append(',');
                    Log.i(TAG, "onWrite requested pages: " + names);
                    PdfDocument subset = new PdfDocument();
                    for (int p : wanted) drawPage(subset, p);
                    subset.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                    subset.close();
                    callback.onWriteFinished(pages);
                    Log.i(TAG, "wrote " + wanted.size() + " page(s) to framework");
                } catch (Exception e) {
                    Log.e(TAG, "write failed", e);
                    callback.onWriteFailed(e.getMessage());
                }
            }
        }, null);
        // Keep the process alive behind the dialog while the job runs.
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 180000);
    }

    private static void drawPage(PdfDocument doc, int pageIndex) {
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(PAGE_W_PT, PAGE_H_PT, pageIndex + 1).create();
        PdfDocument.Page page = doc.startPage(info);
        Canvas canvas = page.getCanvas();
        canvas.drawColor(0xFF000000 | PAGE_COLORS[pageIndex]);
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(Color.WHITE);
        text.setTextSize(160f);
        text.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Page " + (pageIndex + 1), PAGE_W_PT / 2f, PAGE_H_PT / 2f, text);
        // Calibration square: exactly 100mm x 100mm at 72pt/in.
        Paint box = new Paint(Paint.ANTI_ALIAS_FLAG);
        box.setColor(Color.WHITE);
        box.setStyle(Paint.Style.STROKE);
        box.setStrokeWidth(2f);
        float side = 100f / 25.4f * 72f;
        canvas.drawRect(72f, 72f, 72f + side, 72f + side, box);
        doc.finishPage(page);
    }
}
