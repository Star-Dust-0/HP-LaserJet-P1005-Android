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
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintJobInfo;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintDocument;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Base64;
import android.util.Log;
import androidx.core.view.ViewCompat;
import com.mohamedelnaggar.hpp1005.Foo2XqxConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class HpPrintService extends PrintService {
    private static final String ACTION_USB_PERMISSION = "com.mohamedelnaggar.hpp1005.USB_PERMISSION";
    private static final String COMPLETED_JOBS_KEY = "completed_system_jobs";
    private static final String PRINTER_LOCAL_ID = "hp-laserjet-p1005-usb";
    private static final String PRINTER_NAME = "HP LaserJet P1005";
    private static final String PRINT_PREFS = "hp_p1005_print_preferences";
    private static final String STARTED_JOBS_KEY = "started_system_jobs";
    private static final String TAG = "HpPrintService";
    private ExecutorService executor;
    private Handler mainHandler;
    private P1005DiscoverySession session;
    private UsbManager usbManager;
    private final Map<PrintJob, Boolean> cancelFlags = new ConcurrentHashMap();
    private final BroadcastReceiver usbEvents = new BroadcastReceiver() { // from class: com.mohamedelnaggar.hpp1005.HpPrintService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (HpPrintService.ACTION_USB_PERMISSION.equals(action) || "android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action) || "android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                HpPrintService.this.refreshSession();
            }
        }
    };

    private static boolean isWhite(int i) {
        return ((i >> 16) & 255) > 200 && ((i >> 8) & 255) > 200 && (i & 255) > 200;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshSession() {
        P1005DiscoverySession p1005DiscoverySession = this.session;
        if (p1005DiscoverySession != null) {
            p1005DiscoverySession.refresh();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String localized(int i) {
        return AppLanguage.wrap(this, AppLanguage.selected(this)).getString(i);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.usbManager = (UsbManager) getSystemService("usb");
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactory() { // from class: com.mohamedelnaggar.hpp1005.HpPrintService$$ExternalSyntheticLambda4
            @Override // java.util.concurrent.ThreadFactory
            public final Thread newThread(Runnable runnable) {
                return HpPrintService.lambda$onCreate$0(runnable);
            }
        });
        this.mainHandler = new Handler(Looper.getMainLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(this.usbEvents, intentFilter, 4);
        } else {
            registerReceiver(this.usbEvents, intentFilter);
        }
        ServiceDebugHooks.register(this, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.HpPrintService$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                HpPrintService.this.refreshSession();
            }
        });
    }

    static /* synthetic */ Thread lambda$onCreate$0(Runnable runnable) {
        return new Thread(runnable, "P1005-PrintService");
    }

    @Override // android.app.Service
    public void onDestroy() {
        try {
            unregisterReceiver(this.usbEvents);
        } catch (Exception unused) {
        }
        ServiceDebugHooks.unregister(this);
        ExecutorService executorService = this.executor;
        if (executorService != null) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }

    @Override // android.printservice.PrintService
    protected void onConnected() {
        Log.i(TAG, "print service connected");
    }

    @Override // android.printservice.PrintService
    protected void onDisconnected() {
        Log.i(TAG, "print service disconnected");
    }

    @Override // android.printservice.PrintService
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        P1005DiscoverySession p1005DiscoverySession = new P1005DiscoverySession();
        this.session = p1005DiscoverySession;
        return p1005DiscoverySession;
    }

    @Override // android.printservice.PrintService
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        this.cancelFlags.put(printJob, Boolean.TRUE);
        if (printJob.isStarted()) {
            return;
        }
        printJob.cancel();
    }

    @Override // android.printservice.PrintService
    protected void onPrintJobQueued(final PrintJob printJob) {
        if (printJob.isQueued()) {
            if (isJobCompleted(printJob)) {
                Log.i(TAG, "job already completed before rebind; settling without resend");
                printJob.complete();
                return;
            }
            if (isJobStarted(printJob)) {
                Log.w(TAG, "interrupted job re-delivered; failing for explicit retry");
                unmarkJobStarted(jobKey(printJob));
                printJob.fail(localized(R.string.text_006));
                return;
            }
            if (printJob.start()) {
                markJobStarted(printJob);
                printJob.setStatus(localized(R.string.text_007));
                final String jobKey = jobKey(printJob);
                PrintDocument document = printJob.getDocument();
                final PrintJobInfo info = printJob.getInfo();
                final ParcelFileDescriptor data = document != null ? document.getData() : null;
                try {
                    this.executor.execute(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.HpPrintService$$ExternalSyntheticLambda3
                        @Override // java.lang.Runnable
                        public final void run() {
                            HpPrintService.this.lambda$onPrintJobQueued$1(printJob, jobKey, data, info);
                        }
                    });
                } catch (RuntimeException e) {
                    Log.w(TAG, "print worker unavailable; failing job", e);
                    closeQuietly(data);
                    unmarkJobStarted(jobKey);
                    printJob.fail(localized(R.string.text_008));
                }
            }
        }
    }

    /* Rebuilt by hand from bytecode (decompilers could not recover this method). */
    public void lambda$onPrintJobQueued$1(final PrintJob printJob, final String str, ParcelFileDescriptor parcelFileDescriptor, PrintJobInfo printJobInfo) {
        UsbPrinterIO.JOB_LOCK.lock();
        try {
            boolean cancelled = false;
            String error = null;
            File file = null;
            try {
                if (parcelFileDescriptor == null) {
                    throw new IllegalStateException(localized(R.string.text_009));
                }
                file = File.createTempFile("p1005-job-", ".pdf", getCacheDir());
                try {
                    FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] bArr = new byte[65536];
                    while (true) {
                        int read = fileInputStream.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        fileOutputStream.write(bArr, 0, read);
                    }
                    fileOutputStream.close();
                    fileInputStream.close();
                } finally {
                    parcelFileDescriptor.close();
                }
                final boolean fakePrinterEnabled = ServiceDebugHooks.fakePrinterEnabled();
                UsbDevice findPrinter = fakePrinterEnabled ? null : UsbPrinterIO.findPrinter(this.usbManager);
                if (!fakePrinterEnabled) {
                    if (findPrinter == null) {
                        throw new IllegalStateException(localized(R.string.text_010));
                    }
                    if (!this.usbManager.hasPermission(findPrinter)) {
                        throw new IllegalStateException(localized(R.string.text_011));
                    }
                }
                if (!fakePrinterEnabled) {
                    findPrinter = autoPrepareForJob(printJob, findPrinter);
                }
                PrintAttributes attributes = printJobInfo != null ? printJobInfo.getAttributes() : null;
                PrintAttributes.MediaSize mediaSize = attributes != null ? attributes.getMediaSize() : null;
                String paperFor = paperFor(mediaSize);
                int round = mediaSize != null ? Math.round((mediaSize.getWidthMils() * 72.0f) / 1000.0f) : 595;
                int round2 = mediaSize != null ? Math.round((mediaSize.getHeightMils() * 72.0f) / 1000.0f) : 842;
                int max = Math.max(1, printJobInfo != null ? printJobInfo.getCopies() : 1);
                String localized = (printJobInfo == null || printJobInfo.getLabel() == null) ? localized(R.string.text_012) : printJobInfo.getLabel().toString();
                SharedPreferences sharedPreferences = getSharedPreferences(PRINT_PREFS, 0);
                int density = sharedPreferences.getInt("density", 3);
                boolean draft = sharedPreferences.getBoolean("draft_mode", false);
                DocumentRenderer documentRenderer = new DocumentRenderer(getApplicationContext(), Uri.fromFile(file), localized, "application/pdf");
                try {
                    if (!documentRenderer.isSupported() || !documentRenderer.isPdf()) {
                        throw new IllegalStateException(localized(R.string.text_013));
                    }
                    int pageCount = documentRenderer.getPageCount();
                    ArrayList<Integer> pages = new ArrayList<>();
                    for (int i8 = 0; i8 < pageCount; i8++) {
                        pages.add(Integer.valueOf(i8));
                    }
                    if (pages.isEmpty()) {
                        throw new IllegalStateException(localized(R.string.text_014));
                    }
                    final int size = pages.size() * max;
                    int done = 0;
                    for (int pass = 1; pass <= max && !cancelled; pass++) {
                        for (Integer num : pages) {
                            int intValue = num.intValue();
                            if (this.cancelFlags.containsKey(printJob)) {
                                cancelled = true;
                                break;
                            }
                            Bitmap renderPage = documentRenderer.renderPage(intValue, 1800, 2540);
                            if (fakePrinterEnabled) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("system job page sig #");
                                sb.append(Integer.toHexString(renderPage.getPixel(8, 8) & ViewCompat.MEASURED_SIZE_MASK));
                                sb.append(" bitmap ");
                                sb.append(renderPage.getWidth());
                                sb.append("x");
                                sb.append(renderPage.getHeight());
                                Log.i(TAG, sb.toString());
                                ServiceDebugHooks.savePageBitmap(getApplicationContext(), renderPage, done + 1);
                                logCalibration(renderPage, round, round2);
                            }
                            boolean landscape = renderPage.getWidth() > renderPage.getHeight();
                            Bitmap rotate = landscape ? Foo2XqxConverter.rotate(renderPage, 90.0f) : renderPage;
                            Foo2XqxConverter.Result convert = null;
                            try {
                                convert = Foo2XqxConverter.convert(getApplicationContext(), rotate, paperFor, landscape, 1, density, draft, false, false);
                                if (fakePrinterEnabled) {
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("engine geometry ");
                                    sb2.append(convert.width);
                                    sb2.append("x");
                                    sb2.append(convert.height);
                                    sb2.append(" paper ");
                                    sb2.append(paperFor);
                                    Log.i(TAG, sb2.toString());
                                }
                                if (fakePrinterEnabled) {
                                    ServiceDebugHooks.writeSink(getApplicationContext(), convert.xqxFile);
                                } else {
                                    UsbPrinterIO.sendXqx(this.usbManager, convert.xqxFile, findPrinter);
                                }
                            } finally {
                                if (convert != null && convert.xqxFile.exists()) {
                                    convert.xqxFile.delete();
                                }
                                if (rotate != null && !rotate.isRecycled()) {
                                    rotate.recycle();
                                }
                                if (renderPage != rotate && renderPage != null && !renderPage.isRecycled()) {
                                    renderPage.recycle();
                                }
                            }
                            done++;
                            final int doneF = done;
                            this.mainHandler.post(new Runnable() {
                                @Override // java.lang.Runnable
                                public final void run() {
                                    HpPrintService.this.lambda$processJob$2(printJob, size, doneF, fakePrinterEnabled);
                                }
                            });
                        }
                    }
                } finally {
                    documentRenderer.close();
                }
            } catch (PrepareAbortedException e) {
                cancelled = true;
                Log.i(TAG, "print job cancelled while preparing the printer");
            } catch (Exception e) {
                error = e.getMessage() != null ? e.getMessage() : localized(R.string.text_018);
                Log.w(TAG, "print job failed", e);
            } finally {
                this.cancelFlags.remove(printJob);
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
            final boolean cancelledF = cancelled;
            final String errorF = error;
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            this.mainHandler.post(new Runnable() {
                @Override // java.lang.Runnable
                public final void run() {
                    HpPrintService.this.lambda$processJob$3(cancelledF, str, printJob, errorF, countDownLatch);
                }
            });
            try {
                countDownLatch.await(15L, TimeUnit.SECONDS);
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        } finally {
            UsbPrinterIO.JOB_LOCK.unlock();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$processJob$2(PrintJob printJob, int i, int i2, boolean z) {
        printJob.setProgress(i > 0 ? i2 / (float) i : 0.0f);
        StringBuilder sb = new StringBuilder();
        sb.append(localized(R.string.text_015));
        sb.append(i2);
        sb.append(localized(R.string.text_016));
        sb.append(i);
        sb.append(z ? localized(R.string.text_017) : "");
        printJob.setStatus(sb.toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$processJob$3(boolean z, String str, PrintJob printJob, String str2, CountDownLatch countDownLatch) {
        try {
            if (z) {
                unmarkJobStarted(str);
                printJob.cancel();
            } else if (str2 != null) {
                unmarkJobStarted(str);
                printJob.fail(str2);
            } else {
                markJobCompleted(str);
                printJob.complete();
            }
        } catch (Exception e) {
            Log.w(TAG, "could not settle print job", e);
        }
        countDownLatch.countDown();
    }

    private static void closeQuietly(ParcelFileDescriptor parcelFileDescriptor) {
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (Exception unused) {
            }
        }
    }

    private static String paperFor(PrintAttributes.MediaSize mediaSize) {
        if (mediaSize == null) {
            return "A4";
        }
        return mediaSize.equals(PrintAttributes.MediaSize.ISO_A5) ? "A5" : mediaSize.equals(PrintAttributes.MediaSize.ISO_B5) ? "B5" : mediaSize.equals(PrintAttributes.MediaSize.NA_LETTER) ? "LETTER" : mediaSize.equals(PrintAttributes.MediaSize.NA_LEGAL) ? "LEGAL" : "A4";
    }

    private static String jobKey(PrintJob printJob) {
        Parcel obtain = Parcel.obtain();
        try {
            printJob.getId().writeToParcel(obtain, 0);
            return Base64.encodeToString(obtain.marshall(), 2);
        } finally {
            obtain.recycle();
        }
    }

    private boolean isJobCompleted(PrintJob printJob) {
        return isJobCompleted(jobKey(printJob));
    }

    private boolean isJobCompleted(String str) {
        return getSharedPreferences(PRINT_PREFS, 0).getStringSet(COMPLETED_JOBS_KEY, Collections.emptySet()).contains(str);
    }

    private void markJobCompleted(String str) {
        SharedPreferences sharedPreferences = getSharedPreferences(PRINT_PREFS, 0);
        LinkedHashSet linkedHashSet = new LinkedHashSet(sharedPreferences.getStringSet(COMPLETED_JOBS_KEY, Collections.emptySet()));
        linkedHashSet.add(str);
        while (linkedHashSet.size() > 50) {
            Iterator<String> it = linkedHashSet.iterator();
            it.next();
            it.remove();
        }
        sharedPreferences.edit().putStringSet(COMPLETED_JOBS_KEY, linkedHashSet).apply();
        unmarkJobStarted(str);
    }

    private boolean isJobStarted(PrintJob printJob) {
        return isJobStarted(jobKey(printJob));
    }

    private boolean isJobStarted(String str) {
        return getSharedPreferences(PRINT_PREFS, 0).getStringSet(STARTED_JOBS_KEY, Collections.emptySet()).contains(str);
    }

    private void markJobStarted(PrintJob printJob) {
        SharedPreferences sharedPreferences = getSharedPreferences(PRINT_PREFS, 0);
        LinkedHashSet linkedHashSet = new LinkedHashSet(sharedPreferences.getStringSet(STARTED_JOBS_KEY, Collections.emptySet()));
        linkedHashSet.add(jobKey(printJob));
        while (linkedHashSet.size() > 50) {
            Iterator<String> it = linkedHashSet.iterator();
            it.next();
            it.remove();
        }
        sharedPreferences.edit().putStringSet(STARTED_JOBS_KEY, linkedHashSet).apply();
    }

    private void unmarkJobStarted(String str) {
        SharedPreferences sharedPreferences = getSharedPreferences(PRINT_PREFS, 0);
        LinkedHashSet linkedHashSet = new LinkedHashSet(sharedPreferences.getStringSet(STARTED_JOBS_KEY, Collections.emptySet()));
        linkedHashSet.remove(str);
        sharedPreferences.edit().putStringSet(STARTED_JOBS_KEY, linkedHashSet).apply();
    }

    /* Sentinel used to unwind the worker when the job is cancelled while the
       printer is being prepared, so the job settles as cancelled, not failed. */
    private static final class PrepareAbortedException extends Exception {
        private PrepareAbortedException() {
        }
    }

    /* Auto-prepare for a system print job: verifies the printer firmware and
       loads it only when missing, then returns the device handle to print to
       (the printer can re-enumerate when the firmware loads). Runs on the job
       worker thread with UsbPrinterIO.JOB_LOCK already held, so it cannot
       collide with the app's own prepare or another print job. */
    private UsbDevice autoPrepareForJob(final PrintJob printJob, UsbDevice usbDevice) throws Exception {
        final Context localizedContext = AppLanguage.wrap(this, AppLanguage.selected(this));
        final String[] lastPermissionRequestName = new String[1];
        final long[] lastPermissionRequestAt = new long[1];
        try {
            return PrinterFirmware.ensureReady(localizedContext, this.usbManager, usbDevice, new PrinterFirmware.Listener() {
                @Override // com.mohamedelnaggar.hpp1005.PrinterFirmware.Listener
                public void onChecking() {
                    postJobStatus(printJob, localizedContext.getString(R.string.text_463));
                }

                @Override // com.mohamedelnaggar.hpp1005.PrinterFirmware.Listener
                public void onSending() {
                    postJobStatus(printJob, localizedContext.getString(R.string.text_453));
                }

                @Override // com.mohamedelnaggar.hpp1005.PrinterFirmware.Listener
                public void onVerifying() {
                    postJobStatus(printJob, localizedContext.getString(R.string.text_464));
                }

                @Override // com.mohamedelnaggar.hpp1005.PrinterFirmware.Listener
                public void onPermissionNeeded(UsbDevice device) {
                    postJobStatus(printJob, localizedContext.getString(R.string.text_461));
                    String deviceName = device.getDeviceName();
                    long now = System.currentTimeMillis();
                    if (!deviceName.equals(lastPermissionRequestName[0]) || now - lastPermissionRequestAt[0] > 15000L) {
                        lastPermissionRequestName[0] = deviceName;
                        lastPermissionRequestAt[0] = now;
                        requestUsbPermission(device);
                    }
                }
            }, new PrinterFirmware.AbortCheck() {
                @Override // com.mohamedelnaggar.hpp1005.PrinterFirmware.AbortCheck
                public boolean aborted() {
                    return HpPrintService.this.cancelFlags.containsKey(printJob);
                }
            });
        } catch (PrinterFirmware.CancelledException e) {
            throw new PrepareAbortedException();
        } catch (Exception e2) {
            String detail = e2.getMessage();
            throw new IllegalStateException(localizedContext.getString(R.string.text_449) + (detail != null ? detail : localizedContext.getString(R.string.text_018)));
        }
    }

    private void postJobStatus(final PrintJob printJob, final String str) {
        this.mainHandler.post(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                try {
                    printJob.setStatus((CharSequence) str);
                } catch (Exception e) {
                    Log.w(HpPrintService.TAG, "could not update job status", e);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestUsbPermission(UsbDevice usbDevice) {
        this.usbManager.requestPermission(usbDevice, PendingIntent.getBroadcast(this, 1, new Intent(ACTION_USB_PERMISSION).setPackage(getPackageName()), Build.VERSION.SDK_INT >= 31 ? 167772160 : 201326592));
    }

    private static void logCalibration(Bitmap bitmap, int i, int i2) {
        int i3;
        int i4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int round = Math.round((72.0f / i2) * height);
        float f = i;
        float f2 = width;
        int round2 = Math.round((72.0f / f) * f2);
        float f3 = 283.46f / f;
        int round3 = Math.round(f2 * f3);
        int i5 = 0;
        int i6 = -1;
        int i7 = -1;
        for (int max = Math.max(0, round - 16); max <= Math.min(height - 1, round + 16); max++) {
            int max2 = Math.max(0, round2 - 24);
            while (max2 < Math.min(width, round2 + 24)) {
                if (isWhite(bitmap.getPixel(max2, max))) {
                    int i8 = 0;
                    while (true) {
                        i4 = max2 + i8;
                        if (i4 >= width || !isWhite(bitmap.getPixel(i4, max))) {
                            break;
                        } else {
                            i8++;
                        }
                    }
                    if (i8 > i5) {
                        i7 = max;
                        i6 = max2;
                        i5 = i8;
                    }
                    max2 = i4;
                } else {
                    max2++;
                }
            }
        }
        if (i5 < (round3 * 3) / 4 || i6 < 0) {
            i3 = 0;
        } else {
            int i9 = 0;
            for (int i10 = i7; i10 < height && isWhite(bitmap.getPixel(i6, i10)); i10++) {
                i9++;
            }
            boolean z = i9 >= (i5 * 3) / 4 && Math.abs(i9 - i5) <= i5 / 8;
            i3 = z ? i5 : 0;
            StringBuilder sb = new StringBuilder("calibration edges: top=(");
            sb.append(i6);
            sb.append(",");
            sb.append(i7);
            sb.append(") len ");
            sb.append(i5);
            sb.append("px, left len ");
            sb.append(i9);
            sb.append("px, expected side ");
            sb.append(round3);
            sb.append("px");
            sb.append(z ? " [square OK]" : " [NOT square]");
            Log.i(TAG, sb.toString());
        }
        if (i3 == 0) {
            int i11 = -1;
            int i12 = 0;
            for (int i13 = 0; i13 < height; i13 += 2) {
                int longestWhiteRun = longestWhiteRun(bitmap, i13, 0, width);
                if (longestWhiteRun > i12) {
                    i11 = i13;
                    i12 = longestWhiteRun;
                }
            }
            int i14 = 0;
            for (int i15 = 0; i15 < height; i15 += 4) {
                for (int i16 = 0; i16 < width; i16 += 4) {
                    if (isWhite(bitmap.getPixel(i16, i15))) {
                        i14++;
                    }
                }
            }
            Log.i(TAG, "calibration diagnostic: square structure absent; best run anywhere " + i12 + "px at row " + i11 + " (expected edge row " + round + ", col " + round2 + "), white samples " + i14);
        }
        Log.i(TAG, "calibration square side " + i3 + "px on " + width + "x" + height + " bitmap (expected fraction " + String.format(Locale.US, "%.3f", Float.valueOf(f3)) + " of width)");
    }

    private static int longestWhiteRun(Bitmap bitmap, int i, int i2, int i3) {
        int i4 = 0;
        int i5 = 0;
        while (i2 < i3) {
            if (isWhite(bitmap.getPixel(i2, i))) {
                i5++;
                if (i5 > i4) {
                    i4 = i5;
                }
            } else {
                i5 = 0;
            }
            i2++;
        }
        return i4;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public PrinterCapabilitiesInfo buildCapabilities(PrinterId printerId) {
        return new PrinterCapabilitiesInfo.Builder(printerId).addMediaSize(PrintAttributes.MediaSize.ISO_A4, true).addMediaSize(PrintAttributes.MediaSize.ISO_A5, false).addMediaSize(PrintAttributes.MediaSize.ISO_B5, false).addMediaSize(PrintAttributes.MediaSize.NA_LETTER, false).addMediaSize(PrintAttributes.MediaSize.NA_LEGAL, false).addResolution(new PrintAttributes.Resolution("res600", "600 × 600 dpi", 600, 600), true).setColorModes(1, 1).setDuplexModes(1, 1).setMinMargins(new PrintAttributes.Margins(158, 158, 158, 158)).build();
    }

    private final class P1005DiscoverySession extends PrinterDiscoverySession {
        @Override // android.printservice.PrinterDiscoverySession
        public void onDestroy() {
        }

        @Override // android.printservice.PrinterDiscoverySession
        public void onStopPrinterDiscovery() {
        }

        @Override // android.printservice.PrinterDiscoverySession
        public void onStopPrinterStateTracking(PrinterId printerId) {
        }

        private P1005DiscoverySession() {
        }

        @Override // android.printservice.PrinterDiscoverySession
        public void onStartPrinterDiscovery(List<PrinterId> list) {
            refresh();
        }

        @Override // android.printservice.PrinterDiscoverySession
        public void onValidatePrinters(List<PrinterId> list) {
            refresh();
        }

        @Override // android.printservice.PrinterDiscoverySession
        public void onStartPrinterStateTracking(PrinterId printerId) {
            refresh();
        }

        void refresh() {
            String str;
            if (isDestroyed()) {
                return;
            }
            PrinterId generatePrinterId = HpPrintService.this.generatePrinterId(HpPrintService.PRINTER_LOCAL_ID);
            UsbDevice findPrinter = UsbPrinterIO.findPrinter(HpPrintService.this.usbManager);
            boolean fakePrinterEnabled = ServiceDebugHooks.fakePrinterEnabled();
            if (fakePrinterEnabled || findPrinter != null) {
                int i = 1;
                if (fakePrinterEnabled) {
                    str = HpPrintService.this.localized(R.string.text_019);
                } else if (HpPrintService.this.usbManager.hasPermission(findPrinter)) {
                    str = HpPrintService.this.localized(R.string.text_020);
                } else {
                    String localized = HpPrintService.this.localized(R.string.text_021);
                    HpPrintService.this.requestUsbPermission(findPrinter);
                    i = 3;
                    str = localized;
                }
                addPrinters(Collections.singletonList(new PrinterInfo.Builder(generatePrinterId, HpPrintService.PRINTER_NAME, i).setCapabilities(HpPrintService.this.buildCapabilities(generatePrinterId)).setDescription(str).build()));
                return;
            }
            removePrinters(Collections.singletonList(generatePrinterId));
        }
    }
}
