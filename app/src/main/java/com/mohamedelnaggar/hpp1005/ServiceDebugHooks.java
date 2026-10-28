package com.mohamedelnaggar.hpp1005;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.File;

/* loaded from: classes.dex */
final class ServiceDebugHooks {
    static boolean fakePrinterEnabled() {
        return false;
    }

    static void register(HpPrintService hpPrintService, Runnable runnable) {
    }

    static void savePageBitmap(Context context, Bitmap bitmap, int i) {
    }

    static void unregister(HpPrintService hpPrintService) {
    }

    private ServiceDebugHooks() {
    }

    static void writeSink(Context context, File file) {
        throw new IllegalStateException("debug fixture unavailable in release builds");
    }
}
