package com.mohamedelnaggar.hpp1005;

import android.content.Context;

import java.io.File;

/**
 * Release build: the CI debug fixture does not exist. No broadcast action,
 * no receiver, no synthetic printer, no file sink - the release bytecode
 * contains none of it.
 */
final class ServiceDebugHooks {
    private ServiceDebugHooks() {}

    static void register(HpPrintService service, Runnable refresher) {}

    static void unregister(HpPrintService service) {}

    static void savePageBitmap(Context context, android.graphics.Bitmap bitmap, int seq) {}

    static boolean fakePrinterEnabled() { return false; }

    static void writeSink(Context context, File xqxFile) {
        throw new IllegalStateException("debug fixture unavailable in release builds");
    }
}
