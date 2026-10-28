package com.mohamedelnaggar.hpp1005;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Debug builds only: CI fixture for the print service. The emulator has no
 * USB hardware, so a broadcast toggles a synthetic attached P1005 and the
 * converted XQX stream is written to a file sink instead of the printer.
 * Never claims physical output.
 */
final class ServiceDebugHooks {
    private static final String TAG = "HpPrintService";
    static final String ACTION_FAKE_PRINTER =
            "com.mohamedelnaggar.hpp1005.DEBUG_FAKE_PRINTER";
    static final String EXTRA_ENABLED = "enabled";

    private static volatile boolean fakeEnabled;

    private ServiceDebugHooks() {}

    static void register(final HpPrintService service, final Runnable refresher) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (!ACTION_FAKE_PRINTER.equals(intent.getAction())) return;
                fakeEnabled = intent.getBooleanExtra(EXTRA_ENABLED, false);
                Log.i(TAG, "debug fixture printer " + (fakeEnabled ? "enabled" : "disabled"));
                refresher.run();
            }
        };
        // Exported so `adb shell am broadcast` reaches it in CI; debug builds only.
        if (Build.VERSION.SDK_INT >= 33) {
            service.registerReceiver(receiver, new IntentFilter(ACTION_FAKE_PRINTER),
                    Context.RECEIVER_EXPORTED);
        } else {
            service.registerReceiver(receiver, new IntentFilter(ACTION_FAKE_PRINTER));
        }
    }

    static boolean fakePrinterEnabled() { return fakeEnabled; }

    static void writeSink(Context context, File xqxFile) throws Exception {
        File sink = new File(context.getExternalFilesDir(null), "debug-fixture-usb-stream.xqx");
        try (FileInputStream in = new FileInputStream(xqxFile);
             FileOutputStream out = new FileOutputStream(sink, true)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
        Log.i(TAG, "debug fixture: appended XQX page to " + sink.getAbsolutePath());
    }
}
