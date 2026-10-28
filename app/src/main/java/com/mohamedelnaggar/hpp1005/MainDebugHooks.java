package com.mohamedelnaggar.hpp1005;

import android.graphics.Bitmap;
import java.io.File;

/* loaded from: classes.dex */
final class MainDebugHooks {
    static boolean enabled() {
        return false;
    }

    static void register(MainActivity mainActivity) {
    }

    static void unregister(MainActivity mainActivity) {
    }

    static void validatePrinter() {
    }

    MainDebugHooks() {
    }

    static void saveAndSend(MainActivity mainActivity, Bitmap bitmap, File file, int i) {
        throw new IllegalStateException("debug fixture unavailable in release builds");
    }
}
