package com.mohamedelnaggar.hpp1005;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.core.view.ViewCompat;
import java.lang.reflect.Array;
import java.util.Locale;

/* loaded from: classes.dex */
final class SheetComposer {
    static final float DEFAULT_POSTER_SCALE_PERCENT = 150.0f;
    static final float HARDWARE_MARGIN_MM = 4.0f;

    enum Binding {
        LEFT,
        RIGHT
    }

    enum Mode {
        SIZE,
        POSTER,
        MULTIPLE,
        BOOKLET
    }

    interface PageSource {
        Bitmap page(int i) throws Exception;

        float[] pagePhysicalMm(int i);

        void release(Bitmap bitmap);
    }

    enum ScaleMode {
        FIT,
        ACTUAL,
        SHRINK,
        CUSTOM
    }

    static float actualScale(float f, float f2, int i, int i2) {
        if (f <= 0.0f || f2 <= 0.0f || i2 <= 0) {
            return 1.0f;
        }
        return ((f / f2) * i) / i2;
    }

    private static int pad(int i, int i2) {
        if (i < i2) {
            return i;
        }
        return -1;
    }

    static final class Spec {
        boolean cutMarks;
        int marginPx;
        int sheetHeightPx;
        int sheetWidthPx;
        Mode mode = Mode.SIZE;
        ScaleMode scaleMode = ScaleMode.FIT;
        float customScalePercent = 100.0f;
        int pagesPerSheet = 2;
        float paperWidthMm = 210.0f;
        boolean rtl = true;
        Binding binding = Binding.RIGHT;
        int overlapPx = 0;
        float posterScalePercent = SheetComposer.DEFAULT_POSTER_SCALE_PERCENT;

        Spec() {
        }
    }

    static float[] paperMm(String str) {
        String upperCase = str == null ? "A4" : str.toUpperCase(Locale.ROOT);
        switch (upperCase) {
            case "LETTER":
                return new float[]{215.9f, 279.4f};
            case "LEGAL":
                return new float[]{215.9f, 355.6f};
            case "A5":
                return new float[]{148.0f, 210.0f};
            case "B5":
                return new float[]{176.0f, 250.0f};
            default:
                return new float[]{210.0f, 297.0f};
        }
    }

    static int[] sheetDims(String str, boolean z, int i) {
        float[] paperMm = paperMm(str);
        return new int[]{i, Math.max(1, Math.round((i * (z ? paperMm[0] : paperMm[1])) / (z ? paperMm[1] : paperMm[0])))};
    }

    static int marginPx(String str, boolean z, int i) {
        float[] paperMm = paperMm(str);
        return Math.max(1, Math.round((i * HARDWARE_MARGIN_MM) / (z ? paperMm[1] : paperMm[0])));
    }

    /* renamed from: com.mohamedelnaggar.hpp1005.SheetComposer$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$mohamedelnaggar$hpp1005$SheetComposer$ScaleMode;

        static {
            int[] iArr = new int[ScaleMode.values().length];
            $SwitchMap$com$mohamedelnaggar$hpp1005$SheetComposer$ScaleMode = iArr;
            try {
                iArr[ScaleMode.ACTUAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$mohamedelnaggar$hpp1005$SheetComposer$ScaleMode[ScaleMode.CUSTOM.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$mohamedelnaggar$hpp1005$SheetComposer$ScaleMode[ScaleMode.SHRINK.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$mohamedelnaggar$hpp1005$SheetComposer$ScaleMode[ScaleMode.FIT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    static float scaleFor(ScaleMode scaleMode, float f, float f2, int i, int i2, int i3, int i4) {
        float min = Math.min(i3 / (float) i, i4 / (float) i2);
        int i5 = AnonymousClass1.$SwitchMap$com$mohamedelnaggar$hpp1005$SheetComposer$ScaleMode[scaleMode.ordinal()];
        if (i5 == 1) {
            return f2;
        }
        if (i5 != 2) {
            return i5 != 3 ? min : Math.min(f2, min);
        }
        return (f2 * Math.max(1.0f, f)) / 100.0f;
    }

    static int[] gridFor(int i) {
        if (i == 2) {
            return new int[]{1, 2};
        }
        if (i == 4) {
            return new int[]{2, 2};
        }
        if (i == 6) {
            return new int[]{2, 3};
        }
        if (i == 9) {
            return new int[]{3, 3};
        }
        if (i == 16) {
            return new int[]{4, 4};
        }
        return new int[]{1, 2};
    }

    static int[] multipleCell(Spec spec, int i) {
        int i2 = spec.sheetWidthPx - (spec.marginPx * 2);
        int i3 = spec.sheetHeightPx - (spec.marginPx * 2);
        int[] gridFor = gridFor(spec.pagesPerSheet);
        int i4 = gridFor[0];
        int i5 = i2 / i4;
        int i6 = i3 / gridFor[1];
        int i7 = i % i4;
        int i8 = i / i4;
        if (spec.rtl) {
            i7 = (i4 - 1) - i7;
        }
        return new int[]{spec.marginPx + (i7 * i5), spec.marginPx + (i8 * i6), i5, i6};
    }

    static int[] posterCell(int i, boolean z, int i2) {
        int i3 = i % i2;
        int i4 = i / i2;
        if (z) {
            i3 = (i2 - 1) - i3;
        }
        return new int[]{i3, i4};
    }

    static int[] posterGridMm(float f, float f2, float f3, float f4, float f5, float f6) {
        float max = Math.max(25.0f, f6) / 100.0f;
        float f7 = f4 * max;
        float f8 = f5 * max;
        int max2 = Math.max(1, (int) Math.ceil((f7 / f) - 1.0E-4d));
        while ((max2 * f) - ((max2 - 1) * f3) < f7 - 1.0E-4d) {
            max2++;
        }
        int max3 = Math.max(1, (int) Math.ceil((f8 / f2) - 1.0E-4d));
        while ((max3 * f2) - ((max3 - 1) * f3) < f8 - 1.0E-4d) {
            max3++;
        }
        return new int[]{max2, max3};
    }

    static float[] posterGeometry(Spec spec, int i, int i2, float f, int i3, int i4) {
        int i5 = spec.sheetWidthPx - (spec.marginPx * 2);
        int i6 = spec.sheetHeightPx - (spec.marginPx * 2);
        float f2 = (i5 * i) - ((float) (i - 1) * spec.overlapPx);
        float f3 = (i6 * i2) - ((float) (i2 - 1) * spec.overlapPx);
        return new float[]{f, (f2 - (i3 * f)) / 2.0f, (f3 - (i4 * f)) / 2.0f, f2, f3};
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x00d1  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x00db  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    static int[] posterTileMap(Spec spec, int i, int i2, int i3, float f, int i4, int i5) {
        int i9 = spec.sheetWidthPx - (spec.marginPx * 2);
        int i10 = spec.sheetHeightPx - (spec.marginPx * 2);
        float[] posterGeometry = posterGeometry(spec, i2, i3, f, i4, i5);
        float f2 = posterGeometry[0];
        float f3 = posterGeometry[1];
        float f4 = posterGeometry[2];
        int[] posterCell = posterCell(i, spec.rtl, i2);
        int i11 = posterCell[0];
        int i12 = posterCell[1];
        float f5 = (float) i11 * (i9 - spec.overlapPx);
        float f6 = (float) i12 * (i10 - spec.overlapPx);
        float max = Math.max(f5, f3);
        float min = Math.min(i9 + f5, (i4 * f2) + f3);
        float max2 = Math.max(f6, f4);
        float min2 = Math.min(i10 + f6, (i5 * f2) + f4);
        int round = Math.round((max - f3) / f2);
        int round2 = Math.round((min - f3) / f2);
        int round3 = Math.round((max2 - f4) / f2);
        int round4 = Math.round((min2 - f4) / f2);
        int round5 = spec.marginPx + Math.round(max - f5);
        int round6 = spec.marginPx + Math.round(max2 - f6);
        int round7 = spec.marginPx + Math.round(min - f5);
        int round8 = spec.marginPx + Math.round(min2 - f6);
        int i6 = -1;
        if (spec.overlapPx > 0 && i2 > 1) {
            if (i11 == 0) {
                i6 = (spec.marginPx + i9) - spec.overlapPx;
            } else if (i11 == i2 - 1) {
                i6 = spec.marginPx + spec.overlapPx;
            }
        }
        int i7 = -1;
        if (spec.overlapPx > 0 && i3 > 1) {
            if (i12 == 0) {
                i7 = (spec.marginPx + i10) - spec.overlapPx;
            } else if (i12 == i3 - 1) {
                i7 = spec.marginPx + spec.overlapPx;
            }
        }
        return new int[]{round, round3, round2, round4, round5, round6, round7, round8, i6, i7};
    }

    static Bitmap composeSheet(Spec spec, int[] iArr, PageSource pageSource) throws Exception {
        int i;
        Rect rect;
        Bitmap createBitmap = Bitmap.createBitmap(spec.sheetWidthPx, spec.sheetHeightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawColor(-1);
        Paint paint = new Paint(2);
        int i2 = spec.sheetWidthPx - (spec.marginPx * 2);
        int i3 = spec.sheetHeightPx - (spec.marginPx * 2);
        Rect rect2 = null;
        char c = 0;
        if (spec.mode == Mode.BOOKLET) {
            int i4 = i2 / 2;
            int i5 = 0;
            for (int i6 = 2; i5 < i6 && i5 < iArr.length; i6 = 2) {
                int i7 = iArr[i5];
                if (i7 < 0) {
                    rect = rect2;
                } else {
                    Bitmap page = pageSource.page(i7);
                    rect = rect2;
                    try {
                        float scaleFor = scaleFor(ScaleMode.FIT, 100.0f, 1.0f, page.getWidth(), page.getHeight(), i4, i3);
                        float width = page.getWidth() * scaleFor;
                        float height = page.getHeight() * scaleFor;
                        float f = spec.marginPx + (i5 * i4) + ((i4 - width) / 2.0f);
                        float f2 = spec.marginPx + ((i3 - height) / 2.0f);
                        RectF rectF = new RectF(f, f2, width + f, height + f2);
                        canvas.drawBitmap(page, rect, rectF, paint);
                    } finally {
                        pageSource.release(page);
                    }
                }
                i5++;
                rect2 = rect;
            }
        } else if (spec.mode == Mode.MULTIPLE) {
            int[] gridFor = gridFor(spec.pagesPerSheet);
            int min = Math.min(iArr.length, gridFor[0] * gridFor[1]);
            int i8 = 0;
            while (i8 < min) {
                int i9 = iArr[i8];
                if (i9 >= 0) {
                    Bitmap page2 = pageSource.page(i9);
                    try {
                        int[] multipleCell = multipleCell(spec, i8);
                        float scaleFor2 = scaleFor(ScaleMode.FIT, 100.0f, 1.0f, page2.getWidth(), page2.getHeight(), multipleCell[2], multipleCell[3]);
                        float width2 = page2.getWidth() * scaleFor2;
                        float height2 = page2.getHeight() * scaleFor2;
                        float f3 = multipleCell[c] + ((multipleCell[2] - width2) / 2.0f);
                        float f4 = multipleCell[1] + ((multipleCell[3] - height2) / 2.0f);
                        canvas.drawBitmap(page2, (Rect) null, new RectF(f3, f4, width2 + f3, height2 + f4), paint);
                    } finally {
                        pageSource.release(page2);
                    }
                }
                i8++;
                c = 0;
            }
        } else if (iArr.length > 0 && (i = iArr[0]) >= 0) {
            Bitmap page3 = pageSource.page(i);
            try {
                float[] pagePhysicalMm = pageSource.pagePhysicalMm(iArr[0]);
                float actualScale = actualScale(pagePhysicalMm == null ? 0.0f : pagePhysicalMm[0], spec.paperWidthMm, spec.sheetWidthPx, page3.getWidth());
                float scaleFor3 = scaleFor(spec.scaleMode, spec.customScalePercent, actualScale, page3.getWidth(), page3.getHeight(), i2, i3);
                float width3 = page3.getWidth() * scaleFor3;
                float height3 = page3.getHeight() * scaleFor3;
                float f5 = spec.marginPx + ((i2 - width3) / 2.0f);
                float f6 = spec.marginPx + ((i3 - height3) / 2.0f);
                canvas.drawBitmap(page3, (Rect) null, new RectF(f5, f6, width3 + f5, height3 + f6), paint);
            } finally {
                pageSource.release(page3);
            }
        }
        return createBitmap;
    }

    static Bitmap composePosterTile(Spec spec, Bitmap bitmap, int i, int i2, int i3, float f) {
        Bitmap createBitmap = Bitmap.createBitmap(spec.sheetWidthPx, spec.sheetHeightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawColor(-1);
        Paint paint = new Paint(2);
        int[] posterTileMap = posterTileMap(spec, i, i2, i3, (Math.max(25.0f, spec.posterScalePercent) * f) / 100.0f, bitmap.getWidth(), bitmap.getHeight());
        if (posterTileMap[2] > posterTileMap[0] && posterTileMap[3] > posterTileMap[1]) {
            canvas.drawBitmap(bitmap, new Rect(posterTileMap[0], posterTileMap[1], posterTileMap[2], posterTileMap[3]), new RectF(posterTileMap[4], posterTileMap[5], posterTileMap[6], posterTileMap[7]), paint);
        }
        if (spec.cutMarks) {
            Paint paint2 = new Paint();
            paint2.setColor(ViewCompat.MEASURED_STATE_MASK);
            paint2.setStrokeWidth(Math.max(2.0f, spec.sheetWidthPx / 600.0f));
            int max = Math.max(spec.marginPx, spec.overlapPx);
            int i4 = posterTileMap[8];
            if (i4 >= 0) {
                canvas.drawLine(i4, spec.marginPx, posterTileMap[8], spec.marginPx + max, paint2);
                canvas.drawLine(posterTileMap[8], (spec.sheetHeightPx - spec.marginPx) - max, posterTileMap[8], spec.sheetHeightPx - spec.marginPx, paint2);
            }
            if (posterTileMap[9] >= 0) {
                canvas.drawLine(spec.marginPx, posterTileMap[9], spec.marginPx + max, posterTileMap[9], paint2);
                canvas.drawLine((spec.sheetWidthPx - spec.marginPx) - max, posterTileMap[9], spec.sheetWidthPx - spec.marginPx, posterTileMap[9], paint2);
            }
        }
        return createBitmap;
    }

    static int[][] bookletOrder(int i, Binding binding) {
        int i2 = (i + 3) & (-4);
        int[][] iArr = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, i2 / 2, 2);
        for (int i3 = 0; i3 < i2 / 4; i3++) {
            int i4 = i3 * 2;
            int i5 = (i2 - 1) - i4;
            int i6 = i4 + 1;
            int i7 = (i2 - 2) - i4;
            if (binding == Binding.RIGHT) {
                iArr[i4][0] = pad(i4, i);
                iArr[i4][1] = pad(i5, i);
                iArr[i6][0] = pad(i7, i);
                iArr[i6][1] = pad(i6, i);
            } else {
                iArr[i4][0] = pad(i5, i);
                iArr[i4][1] = pad(i4, i);
                iArr[i6][0] = pad(i6, i);
                iArr[i6][1] = pad(i7, i);
            }
        }
        return iArr;
    }

    static boolean verifyFold(int[][] iArr, int i, Binding binding) {
        int length = iArr.length / 2;
        int[] iArr2 = new int[i];
        boolean z = binding == Binding.LEFT;
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            int i4 = i3 * 2;
            int i5 = z ? iArr[i4][1] : iArr[i4][0];
            int i6 = (i3 * 2) + 1;
            int i7 = z ? iArr[i6][0] : iArr[i6][1];
            if (i5 >= 0) {
                if (i2 >= i || i5 != i2) {
                    return false;
                }
                iArr2[i2] = i5;
                i2++;
            }
            if (i7 >= 0) {
                if (i2 >= i || i7 != i2) {
                    return false;
                }
                iArr2[i2] = i7;
                i2++;
            }
        }
        for (int i8 = length - 1; i8 >= 0; i8--) {
            int i9 = (i8 * 2) + 1;
            int i10 = z ? iArr[i9][1] : iArr[i9][0];
            int i11 = i8 * 2;
            int i12 = z ? iArr[i11][0] : iArr[i11][1];
            if (i10 >= 0) {
                if (i2 >= i || i10 != i2) {
                    return false;
                }
                iArr2[i2] = i10;
                i2++;
            }
            if (i12 >= 0) {
                if (i2 >= i || i12 != i2) {
                    return false;
                }
                iArr2[i2] = i12;
                i2++;
            }
        }
        return i2 == i;
    }

    private SheetComposer() {
    }
}
