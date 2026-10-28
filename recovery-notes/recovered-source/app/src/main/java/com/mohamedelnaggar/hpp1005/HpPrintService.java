package com.mohamedelnaggar.hpp1005;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.print.PrintJobInfo;
import android.printservice.PrintDocument;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Android system print service for the HP LaserJet P1005. Once the user
 * enables it in Settings &gt; Printing, any app's system print dialog lists
 * the P1005 and jobs are converted through the same Foo2XqxConverter +
 * USB stream the in-app flow uses.
 *
 * Truthful capability rules: the P1005 is monochrome with no duplex
 * hardware and no paper/size sensing, so the service advertises only
 * monochrome, DUPLEX_MODE_NONE and the fixed paper list. Density and
 * draft are not standard Android print attributes; they stay app-owned
 * settings (read from the same SharedPreferences the app writes).
 * Job completion means the bytes were accepted by USB, never that paper
 * physically exited the printer.
 */
public class HpPrintService extends PrintService {
    private static final String TAG = "HpPrintService";
    private static final String PRINTER_LOCAL_ID = "hp-laserjet-p1005-usb";
    private static final String PRINTER_NAME = "HP LaserJet P1005";
    private static final String ACTION_USB_PERMISSION =
            "com.mohamedelnaggar.hpp1005.USB_PERMISSION";
    private static final String PRINT_PREFS = "hp_p1005_print_preferences";

    // Debug-only fixture (debug builds): CI on an emulator has no USB
    // hardware, so this flag reports a synthetic attached P1005 and sinks
    // the converted XQX bytes to a file instead of USB. It never ships in
    // release builds and never claims physical output.
    static final String ACTION_DEBUG_FAKE_PRINTER =
            "com.mohamedelnaggar.hpp1005.DEBUG_FAKE_PRINTER";
    static final String EXTRA_DEBUG_FAKE_ENABLED = "enabled";
    private volatile boolean debugFakePrinter;

    private UsbManager usbManager;
    private ExecutorService executor;
    private P1005DiscoverySession session;
    private final Map<PrintJob, Boolean> cancelFlags = new ConcurrentHashMap<>();

    private final BroadcastReceiver usbEvents = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            if (ACTION_USB_PERMISSION.equals(action)
                    || UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)
                    || UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                refreshSession();
            } else if (BuildConfig.DEBUG && ACTION_DEBUG_FAKE_PRINTER.equals(action)) {
                debugFakePrinter = intent.getBooleanExtra(EXTRA_DEBUG_FAKE_ENABLED, false);
                Log.i(TAG, "debug fixture printer " + (debugFakePrinter ? "enabled" : "disabled"));
                refreshSession();
            }
        }
    };

    private void refreshSession() {
        P1005DiscoverySession s = session;
        if (s != null) s.refresh();
    }

    @Override public void onCreate() {
        super.onCreate();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "P1005-PrintService"));
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        if (BuildConfig.DEBUG) filter.addAction(ACTION_DEBUG_FAKE_PRINTER);
        if (Build.VERSION.SDK_INT >= 33) {
            // The debug action must stay reachable from `adb shell am broadcast`;
            // debug builds register exported, release keeps NOT_EXPORTED.
            registerReceiver(usbEvents, filter,
                    BuildConfig.DEBUG ? Context.RECEIVER_EXPORTED : Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(usbEvents, filter);
        }
    }

    @Override public void onDestroy() {
        try { unregisterReceiver(usbEvents); } catch (Exception ignored) {}
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }

    @Override protected void onConnected() {
        Log.i(TAG, "print service connected");
    }

    @Override protected void onDisconnected() {
        Log.i(TAG, "print service disconnected");
    }

    @Override protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        session = new P1005DiscoverySession();
        return session;
    }

    @Override protected void onRequestCancelPrintJob(PrintJob printJob) {
        cancelFlags.put(printJob, Boolean.TRUE);
        if (!printJob.isStarted()) {
            printJob.cancel();
        }
    }

    @Override protected void onPrintJobQueued(PrintJob printJob) {
        if (!printJob.isQueued()) return;
        // Death/rebind safety: the spooler can re-deliver a job whose
        // completion was not persisted before the service died. Never resend
        // a job already completed once - the printer cannot unprint pages.
        if (isJobCompleted(printJob)) {
            Log.i(TAG, "job already completed before rebind; settling without resend");
            printJob.complete();
            return;
        }
        if (!printJob.start()) return;
        printJob.setStatus("تُجهَّز مهمة الطباعة…");
        executor.execute(() -> processJob(printJob));
    }

    private void processJob(PrintJob job) {
        File tempPdf = null;
        String failure = null;
        boolean cancelled = false;
        try {
            PrintDocument document = job.getDocument();
            if (document == null || document.getData() == null) {
                throw new IllegalStateException("وصلت مهمة النظام دون بيانات مستند.");
            }
            tempPdf = File.createTempFile("p1005-job-", ".pdf", getCacheDir());
            try (ParcelFileDescriptor source = document.getData();
                 InputStream in = new FileInputStream(source.getFileDescriptor());
                 FileOutputStream out = new FileOutputStream(tempPdf)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            }

            UsbDevice printer = debugFakePrinter ? null : UsbPrinterIO.findPrinter(usbManager);
            if (!debugFakePrinter) {
                if (printer == null) {
                    throw new IllegalStateException("طابعة HP P1005 غير متصلة عبر USB OTG.");
                }
                if (!usbManager.hasPermission(printer)) {
                    throw new IllegalStateException("إذن USB غير ممنوح. افتح تطبيق HP P1005 لمنح الإذن ثم أعد المحاولة.");
                }
            }

            PrintJobInfo info = job.getInfo();
            PrintAttributes attrs = info != null ? info.getAttributes() : null;
            String paper = paperFor(attrs != null ? attrs.getMediaSize() : null);
            int copies = Math.max(1, info != null ? info.getCopies() : 1);
            String label = info != null && info.getLabel() != null ? info.getLabel() : "مستند النظام";

            // Density/draft/fit are app-owned settings (not standard Android
            // print attributes); the service honors the same preferences the
            // in-app flow uses.
            SharedPreferences prefs = getSharedPreferences(PRINT_PREFS, MODE_PRIVATE);
            int density = prefs.getInt("density", 3);
            boolean draft = prefs.getBoolean("draft_mode", false);
            boolean fit = prefs.getBoolean("fit_to_page", true);

            try (DocumentRenderer renderer = new DocumentRenderer(getApplicationContext(),
                    Uri.fromFile(tempPdf), label, "application/pdf")) {
                if (!renderer.isSupported() || !renderer.isPdf()) {
                    throw new IllegalStateException("بيانات مهمة النظام ليست PDF قابلاً للمعالجة.");
                }
                int pageCount = renderer.getPageCount();
                ArrayList<Integer> selection = selectionFor(info != null ? info.getPages() : null, pageCount);
                if (selection.isEmpty()) {
                    throw new IllegalStateException("نطاق الصفحات المطلوب فارغ لهذا المستند.");
                }
                int total = selection.size() * copies;
                int sent = 0;
                for (int copy = 1; copy <= copies && !cancelled; copy++) {
                    for (int pageIndex : selection) {
                        if (cancelFlags.containsKey(job)) {
                            cancelled = true;
                            break;
                        }
                        Bitmap bitmap = renderer.renderPage(pageIndex, 1800, 2540);
                        boolean landscape = bitmap.getWidth() > bitmap.getHeight();
                        Bitmap printBitmap = landscape ? Foo2XqxConverter.rotate(bitmap, 90f) : bitmap;
                        Foo2XqxConverter.Result converted = null;
                        try {
                            converted = Foo2XqxConverter.convert(getApplicationContext(), printBitmap,
                                    paper, landscape, 1, density, draft, fit, false);
                            if (debugFakePrinter) {
                                writeDebugSink(converted.xqxFile);
                            } else {
                                UsbPrinterIO.sendXqx(usbManager, converted.xqxFile, printer);
                            }
                        } finally {
                            if (converted != null && converted.xqxFile.exists()) converted.xqxFile.delete();
                            if (printBitmap != null && !printBitmap.isRecycled()) printBitmap.recycle();
                            if (bitmap != printBitmap && bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
                        }
                        sent++;
                        final int doneNow = sent;
                        job.setProgress(total > 0 ? (float) doneNow / total : 0f);
                        job.setStatus("أُرسلت الصفحة " + doneNow + " من " + total
                                + (debugFakePrinter ? " (جهاز اختبار)" : ""));
                    }
                }
            }
        } catch (Exception e) {
            failure = e.getMessage() != null ? e.getMessage() : "فشلت مهمة الطباعة.";
            Log.w(TAG, "print job failed", e);
        } finally {
            cancelFlags.remove(job);
            if (tempPdf != null && tempPdf.exists()) tempPdf.delete();
        }
        try {
            if (cancelled) {
                job.cancel();
            } else if (failure != null) {
                job.fail(failure);
            } else {
                // Means: the converted stream was accepted (by USB, or by the
                // debug file sink). Never claims paper physically exited.
                markJobCompleted(job);
                job.complete();
            }
        } catch (Exception e) {
            Log.w(TAG, "could not settle print job", e);
        }
    }

    private void writeDebugSink(File xqxFile) throws Exception {
        File sink = new File(getExternalFilesDir(null), "debug-fixture-usb-stream.xqx");
        try (FileInputStream in = new FileInputStream(xqxFile);
             FileOutputStream out = new FileOutputStream(sink, true)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
        Log.i(TAG, "debug fixture: appended XQX page to " + sink.getAbsolutePath());
    }

    private static String paperFor(PrintAttributes.MediaSize mediaSize) {
        if (mediaSize == null) return "A4";
        if (mediaSize.equals(PrintAttributes.MediaSize.ISO_A5)) return "A5";
        if (mediaSize.equals(PrintAttributes.MediaSize.ISO_B5)) return "B5";
        if (mediaSize.equals(PrintAttributes.MediaSize.NA_LETTER)) return "LETTER";
        if (mediaSize.equals(PrintAttributes.MediaSize.NA_LEGAL)) return "LEGAL";
        return "A4";
    }

    private static ArrayList<Integer> selectionFor(PageRange[] ranges, int pageCount) {
        ArrayList<Integer> selection = new ArrayList<>();
        if (ranges == null || ranges.length == 0
                || (ranges.length == 1 && PageRange.ALL_PAGES.equals(ranges[0]))) {
            for (int i = 0; i < pageCount; i++) selection.add(i);
            return selection;
        }
        for (PageRange range : ranges) {
            int start = Math.max(1, range.getStart());
            int end = Math.min(pageCount, range.getEnd());
            for (int page = start; page <= end; page++) {
                int index = page - 1;
                if (!selection.contains(index)) selection.add(index);
            }
        }
        return selection;
    }

    private static final String COMPLETED_JOBS_KEY = "completed_system_jobs";

    private static String jobKey(PrintJob job) {
        android.os.Parcel parcel = android.os.Parcel.obtain();
        try {
            job.getId().writeToParcel(parcel, 0);
            return android.util.Base64.encodeToString(parcel.marshall(), android.util.Base64.NO_WRAP);
        } finally {
            parcel.recycle();
        }
    }

    private boolean isJobCompleted(PrintJob job) {
        return getSharedPreferences(PRINT_PREFS, MODE_PRIVATE)
                .getStringSet(COMPLETED_JOBS_KEY, Collections.<String>emptySet())
                .contains(jobKey(job));
    }

    private void markJobCompleted(PrintJob job) {
        SharedPreferences prefs = getSharedPreferences(PRINT_PREFS, MODE_PRIVATE);
        java.util.Set<String> kept = new java.util.LinkedHashSet<>(
                prefs.getStringSet(COMPLETED_JOBS_KEY, Collections.<String>emptySet()));
        kept.add(jobKey(job));
        while (kept.size() > 50) {
            java.util.Iterator<String> it = kept.iterator();
            it.next();
            it.remove();
        }
        prefs.edit().putStringSet(COMPLETED_JOBS_KEY, kept).apply();
    }

    private void requestUsbPermission(UsbDevice printer) {
        Intent permission = new Intent(ACTION_USB_PERMISSION).setPackage(getPackageName());
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 31) {
            pendingFlags |= PendingIntent.FLAG_MUTABLE;
        } else if (Build.VERSION.SDK_INT >= 23) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pending = PendingIntent.getBroadcast(this, 1, permission, pendingFlags);
        usbManager.requestPermission(printer, pending);
    }

    private PrinterCapabilitiesInfo buildCapabilities(PrinterId id) {
        return new PrinterCapabilitiesInfo.Builder(id)
                .addMediaSize(PrintAttributes.MediaSize.ISO_A4, true)
                .addMediaSize(PrintAttributes.MediaSize.ISO_A5, false)
                .addMediaSize(PrintAttributes.MediaSize.ISO_B5, false)
                .addMediaSize(PrintAttributes.MediaSize.NA_LETTER, false)
                .addMediaSize(PrintAttributes.MediaSize.NA_LEGAL, false)
                .addResolution(new PrintAttributes.Resolution("res600", "600 × 600 dpi", 600, 600), true)
                .setColorModes(PrintAttributes.COLOR_MODE_MONOCHROME,
                        PrintAttributes.COLOR_MODE_MONOCHROME)
                .setDuplexModes(PrintAttributes.DUPLEX_MODE_NONE,
                        PrintAttributes.DUPLEX_MODE_NONE)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();
    }

    private final class P1005DiscoverySession extends PrinterDiscoverySession {
        @Override public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
            refresh();
        }

        @Override public void onStopPrinterDiscovery() {}

        @Override public void onValidatePrinters(List<PrinterId> printerIds) {
            refresh();
        }

        @Override public void onStartPrinterStateTracking(PrinterId printerId) {
            refresh();
        }

        @Override public void onStopPrinterStateTracking(PrinterId printerId) {}

        @Override public void onDestroy() {}

        void refresh() {
            if (isDestroyed()) return;
            PrinterId id = generatePrinterId(PRINTER_LOCAL_ID);
            UsbDevice printer = UsbPrinterIO.findPrinter(usbManager);
            if (debugFakePrinter || printer != null) {
                int status;
                String description;
                if (debugFakePrinter) {
                    status = PrinterInfo.STATUS_IDLE;
                    description = "جهاز اختبار CI (debug fixture) - لا يطبع فعليًا";
                } else if (usbManager.hasPermission(printer)) {
                    status = PrinterInfo.STATUS_IDLE;
                    description = "متصلة عبر USB OTG";
                } else {
                    status = PrinterInfo.STATUS_UNAVAILABLE;
                    description = "افتح تطبيق HP P1005 لمنح إذن USB";
                    requestUsbPermission(printer);
                }
                PrinterInfo info = new PrinterInfo.Builder(id, PRINTER_NAME, status)
                        .setCapabilities(buildCapabilities(id))
                        .setDescription(description)
                        .build();
                addPrinters(Collections.singletonList(info));
            } else {
                removePrinters(Collections.singletonList(id));
            }
        }
    }
}
