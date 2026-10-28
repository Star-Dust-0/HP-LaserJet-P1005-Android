package com.mohamedelnaggar.hpp1005;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public final class Foo2XqxConverter {
    private Foo2XqxConverter() {
    }

    public static final class Result {
        public final int height;
        public final int width;
        public final File xqxFile;

        Result(File file, int i, int i2) {
            this.xqxFile = file;
            this.width = i;
            this.height = i2;
        }
    }

    public static Result convert(Context context, Bitmap bitmap, String str, boolean z, int i, int i2, boolean z2, boolean z3, boolean z4) throws Exception {
        return convert(context, bitmap, str, z, i, i2, z2, z3, z4, 7);
    }

    public static Result convert(Context context, Bitmap bitmap, String str, boolean z, int i, int i2, boolean z2, boolean z3, boolean z4, int i3) throws Exception {
        ProcessBuilder redirectInput;
        ProcessBuilder redirectOutput;
        ProcessBuilder redirectError;
        boolean waitFor;
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException("No rendered page is available.");
        }
        int[] geometry = geometry(str, z);
        int i4 = geometry[0];
        int i5 = geometry[1];
        int i6 = geometry[2];
        File file = new File(context.getCacheDir(), "p1005-print");
        if (!file.exists() && !file.mkdirs()) {
            throw new IllegalStateException("Could not create the print workspace.");
        }
        File createTempFile = File.createTempFile("page-", ".pbm", file);
        File createTempFile2 = File.createTempFile("page-", ".xqx", file);
        File createTempFile3 = File.createTempFile("foo2xqx-", ".log", file);
        writePbm(bitmap, createTempFile, i4, i5, z3, z4);
        File file2 = new File(context.getApplicationInfo().nativeLibraryDir + File.separator + "libfoo2xqx.so");
        if (!file2.exists()) {
            createTempFile.delete();
            createTempFile2.delete();
            createTempFile3.delete();
            throw new IllegalStateException("The built-in HP P1005 XQX rasterizer is not present in this APK.");
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(file2.getAbsolutePath());
        arrayList.add("-r1200x600");
        arrayList.add("-g" + i4 + "x" + i5);
        StringBuilder sb = new StringBuilder("-p");
        sb.append(i6);
        arrayList.add(sb.toString());
        if (i3 > 0) {
            arrayList.add("-s" + i3);
        }
        arrayList.add("-n" + Math.max(1, Math.min(i, 99)));
        arrayList.add("-T" + Math.max(1, Math.min(i2, 5)));
        if (z2) {
            arrayList.add("-t");
        }
        redirectInput = new ProcessBuilder(arrayList).redirectInput(createTempFile);
        redirectOutput = redirectInput.redirectOutput(createTempFile2);
        redirectError = redirectOutput.redirectError(createTempFile3);
        Process start = redirectError.start();
        waitFor = start.waitFor(120L, TimeUnit.SECONDS);
        if (!waitFor) {
            start.destroyForcibly();
            createTempFile.delete();
            createTempFile2.delete();
            createTempFile3.delete();
            throw new IllegalStateException("Page conversion timed out after 120 seconds.");
        }
        int exitValue = start.exitValue();
        if (exitValue != 0 || !createTempFile2.isFile() || createTempFile2.length() < 32) {
            String readSmallText = readSmallText(createTempFile3);
            createTempFile.delete();
            createTempFile2.delete();
            createTempFile3.delete();
            throw new IllegalStateException("The HP P1005 page converter failed (exit " + exitValue + "). " + readSmallText);
        }
        createTempFile.delete();
        createTempFile3.delete();
        return new Result(createTempFile2, i4, i5);
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    private static int[] geometry(String str, boolean z) {
        int i;
        String upperCase = str == null ? "A4" : str.toUpperCase(Locale.ROOT);
        upperCase.hashCode();
        int i2 = 1;
        char c = 65535;
        switch (upperCase.hashCode()) {
            case -2052843482:
                if (upperCase.equals("LETTER")) {
                    c = 0;
                    break;
                }
                break;
            case 2068:
                if (upperCase.equals("A5")) {
                    c = 1;
                    break;
                }
                break;
            case 2099:
                if (upperCase.equals("B5")) {
                    c = 2;
                    break;
                }
                break;
            case 72313497:
                if (upperCase.equals("LEGAL")) {
                    c = 3;
                    break;
                }
                break;
        }
        int i3 = 10200;
        switch (c) {
            case 0:
                i = 6600;
                break;
            case 1:
                i3 = 6996;
                i = 4962;
                i2 = 11;
                break;
            case 2:
                i3 = 8604;
                i = 6072;
                i2 = 13;
                break;
            case 3:
                i = 8400;
                i2 = 5;
                break;
            default:
                i3 = 9924;
                i = 7014;
                i2 = 9;
                break;
        }
        if (z) {
            int round = Math.round(i * 2.0f);
            i = Math.round(i3 / 2.0f);
            i3 = round;
        }
        return new int[]{i3, i, i2};
    }

    public static Bitmap rotate(Bitmap bitmap, float f) {
        Matrix matrix = new Matrix();
        matrix.postRotate(f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static void writePbm(Bitmap bitmap, File file, int i, int i2, boolean z, boolean z2) throws Exception {
        int[][] iArr;
        int[] iArr2;
        byte[] bArr;
        int i3;
        int i4;
        int i5;
        byte[] bArr2;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int max = z ? Math.max(16, i / 40) : 0;
        int max2 = z ? Math.max(16, i2 / 40) : 0;
        int max3 = Math.max(1, i - (max * 2));
        int max4 = Math.max(1, i2 - (max2 * 2));
        byte[] bArr3 = new byte[(i + 7) / 8];
        int[] iArr3 = new int[width];
        int i6 = max3;
        int[][] iArr4 = {new int[]{0, 8, 2, 10}, new int[]{12, 4, 14, 6}, new int[]{3, 11, 1, 9}, new int[]{15, 7, 13, 5}};
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file), 65536);
        try {
            bufferedOutputStream.write(("P4\n" + i + " " + i2 + "\n").getBytes(StandardCharsets.US_ASCII));
            int i7 = -1;
            int i8 = 0;
            while (i8 < i2) {
                Arrays.fill(bArr3, (byte) 0);
                int i9 = i8 - max2;
                boolean z3 = i9 >= 0 && i9 < max4;
                if (z3) {
                    i4 = i8;
                    int[][] iArr5 = iArr4;
                    int i10 = (int) (((long) i9 * height) / max4);
                    if (z2) {
                        i10 = (height - 1) - i10;
                    }
                    int i11 = i10;
                    if (i11 != i7) {
                        iArr = iArr5;
                        iArr2 = iArr3;
                        i3 = max4;
                        bArr = bArr3;
                        bitmap.getPixels(iArr3, 0, width, 0, i11, width, 1);
                        i5 = i6;
                        i7 = i11;
                    } else {
                        iArr2 = iArr3;
                        bArr = bArr3;
                        iArr = iArr5;
                        i3 = max4;
                        i5 = i6;
                    }
                } else {
                    iArr = iArr4;
                    iArr2 = iArr3;
                    bArr = bArr3;
                    i3 = max4;
                    i4 = i8;
                    i5 = i6;
                }
                if (z3) {
                    int i12 = max;
                    while (i12 < i - max) {
                        int i13 = (int) ((((long) (i12 - max)) * width) / i5);
                        if (z2) {
                            i13 = (width - 1) - i13;
                        }
                        int i14 = iArr2[i13];
                        if ((((Color.red(i14) * 299) + (Color.green(i14) * 587)) + (Color.blue(i14) * 114)) / 1000 < (iArr[i4 & 3][i12 & 3] * 8) + 112) {
                            int i15 = i12 >> 3;
                            bArr2 = bArr;
                            bArr2[i15] = (byte) (bArr2[i15] | ((byte) (128 >> (i12 & 7))));
                        } else {
                            bArr2 = bArr;
                        }
                        i12++;
                        bArr = bArr2;
                    }
                }
                bArr3 = bArr;
                bufferedOutputStream.write(bArr3);
                i8 = i4 + 1;
                i6 = i5;
                iArr4 = iArr;
                iArr3 = iArr2;
                max4 = i3;
            }
            bufferedOutputStream.close();
        } finally {
        }
    }

    private static String readSmallText(File file) {
        if (file == null || !file.isFile()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null || sb.length() >= 1000) {
                        break;
                    }
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(readLine);
                } finally {
                }
            }
            bufferedReader.close();
        } catch (Exception unused) {
        }
        return sb.toString();
    }
}
