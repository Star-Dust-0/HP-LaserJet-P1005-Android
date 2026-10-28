package com.mohamedelnaggar.hpp1005;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Xml;
import androidx.core.view.ViewCompat;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: classes.dex */
public final class DocumentRenderer implements Closeable {
    private final Context context;
    private final String displayName;
    private String extractedText;
    private boolean image;
    private Bitmap imageBitmap;
    private final String mimeType;
    private boolean pdf;
    private ParcelFileDescriptor pdfDescriptor;
    private PdfRenderer pdfRenderer;
    private final ContentResolver resolver;
    private boolean supported;
    private List<String> textLines;
    private int textPageCount;
    private final Uri uri;

    public DocumentRenderer(Context context, Uri uri, String str, String str2) throws Exception {
        this.textPageCount = 1;
        Context applicationContext = context.getApplicationContext();
        this.context = applicationContext;
        ContentResolver contentResolver = applicationContext.getContentResolver();
        this.resolver = contentResolver;
        this.uri = uri;
        str = str == null ? "Selected document" : str;
        this.displayName = str;
        String lowerCase = str2 == null ? "" : str2.toLowerCase(Locale.ROOT);
        this.mimeType = lowerCase;
        String extensionOf = extensionOf(str);
        if ("pdf".equals(extensionOf) || "application/pdf".equals(lowerCase)) {
            ParcelFileDescriptor openFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
            this.pdfDescriptor = openFileDescriptor;
            if (openFileDescriptor == null) {
                throw new IllegalStateException("Android could not open this PDF.");
            }
            this.pdfRenderer = new PdfRenderer(this.pdfDescriptor);
            this.pdf = true;
            this.supported = true;
            return;
        }
        if (lowerCase.startsWith("image/") || isImageExtension(extensionOf)) {
            Bitmap decodeImage = decodeImage(2400, 3400);
            this.imageBitmap = decodeImage;
            if (decodeImage == null) {
                throw new IllegalArgumentException("The selected image format could not be decoded by Android.");
            }
            this.image = true;
            this.supported = true;
            return;
        }
        String extractTextDocument = extractTextDocument(extensionOf);
        this.extractedText = extractTextDocument;
        if (TextUtils.isEmpty(extractTextDocument)) {
            return;
        }
        List<String> wrapText = wrapText(this.extractedText, 1240, 1754);
        this.textLines = wrapText;
        this.textPageCount = Math.max(1, ((wrapText.size() + linesPerPage(1754)) - 1) / linesPerPage(1754));
        this.supported = true;
    }

    public boolean isSupported() {
        return this.supported;
    }

    public boolean isPdf() {
        return this.pdf;
    }

    public boolean isImage() {
        return this.image;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public String getFormatLabel() {
        if (this.pdf) {
            return "PDF document";
        }
        if (this.image) {
            return "Image";
        }
        String extensionOf = extensionOf(this.displayName);
        if (extensionOf.isEmpty()) {
            return "Text document";
        }
        return extensionOf.toUpperCase(Locale.ROOT) + " document";
    }

    public float[] pagePhysicalMm(int i) {
        PdfRenderer pdfRenderer;
        try {
            if (this.pdf && (pdfRenderer = this.pdfRenderer) != null && i >= 0 && i < pdfRenderer.getPageCount()) {
                PdfRenderer.Page openPage = this.pdfRenderer.openPage(i);
                try {
                    return new float[]{(openPage.getWidth() * 25.4f) / 72.0f, (openPage.getHeight() * 25.4f) / 72.0f};
                } finally {
                    openPage.close();
                }
            }
            if (this.pdf || this.image || i < 0) {
                return null;
            }
            return new float[]{210.0f, 297.0f};
        } catch (Exception unused) {
            return null;
        }
    }

    public int getPageCount() {
        if (this.pdf) {
            return this.pdfRenderer.getPageCount();
        }
        if (this.image) {
            return 1;
        }
        return this.textPageCount;
    }

    public Bitmap renderPage(int i, int i2, int i3) throws Exception {
        int pageCount = getPageCount();
        if (i < 0 || i >= pageCount) {
            throw new IndexOutOfBoundsException("Page " + (i + 1) + " of " + pageCount);
        }
        if (this.pdf) {
            PdfRenderer.Page openPage = this.pdfRenderer.openPage(i);
            try {
                float min = Math.min(i2 / (float) openPage.getWidth(), i3 / (float) openPage.getHeight());
                Bitmap createBitmap = Bitmap.createBitmap(Math.max(1, Math.round(openPage.getWidth() * min)), Math.max(1, Math.round(openPage.getHeight() * min)), Bitmap.Config.ARGB_8888);
                new Canvas(createBitmap).drawColor(-1);
                openPage.render(createBitmap, null, null, 1);
                return createBitmap;
            } finally {
                openPage.close();
            }
        }
        if (this.image) {
            return fitBitmap(this.imageBitmap, i2, i3, true);
        }
        return renderTextPage(i, i2, i3);
    }

    private Bitmap decodeImage(int i, int i2) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        int i3 = 1;
        options.inJustDecodeBounds = true;
        InputStream openInputStream = this.resolver.openInputStream(this.uri);
        Bitmap bitmap = null;
        if (openInputStream == null) {
            if (openInputStream != null) {
                openInputStream.close();
            }
            return null;
        }
        try {
            BitmapFactory.decodeStream(openInputStream, null, options);
            if (openInputStream != null) {
                openInputStream.close();
            }
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null;
            }
            while (true) {
                int i4 = i3 * 2;
                if (options.outWidth / i4 < i || options.outHeight / i4 < i2) {
                    break;
                }
                i3 = i4;
            }
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = i3;
            options2.inPreferredConfig = Bitmap.Config.ARGB_8888;
            openInputStream = this.resolver.openInputStream(this.uri);
            if (openInputStream != null) {
                try {
                    bitmap = BitmapFactory.decodeStream(openInputStream, null, options2);
                } finally {
                }
            }
            if (openInputStream != null) {
                openInputStream.close();
            }
            return bitmap;
        } finally {
        }
    }

    private Bitmap fitBitmap(Bitmap bitmap, int i, int i2, boolean z) {
        float min = Math.min(i / (float) bitmap.getWidth(), i2 / (float) bitmap.getHeight());
        int max = Math.max(1, Math.round(bitmap.getWidth() * min));
        int max2 = Math.max(1, Math.round(bitmap.getHeight() * min));
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawColor(z ? -1 : 0);
        int i3 = (i - max) / 2;
        int i4 = (i2 - max2) / 2;
        canvas.drawBitmap(bitmap, (Rect) null, new Rect(i3, i4, max + i3, max2 + i4), new Paint(7));
        return createBitmap;
    }

    private Bitmap renderTextPage(int i, int i2, int i3) {
        Bitmap createBitmap = Bitmap.createBitmap(i2, i3, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawColor(-1);
        float f = i3;
        float min = Math.min(i2 / 1240.0f, f / 1754.0f);
        float f2 = 62.0f * min;
        float f3 = 70.0f * min;
        Paint paint = new Paint(1);
        paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        paint.setTextSize(22.0f * min);
        paint.setTypeface(Typeface.create("sans-serif", 0));
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float f4 = (fontMetrics.descent - fontMetrics.ascent) * 1.35f;
        int max = Math.max(1, (int) (((f - f3) - (min * 60.0f)) / f4));
        int i4 = i * max;
        int min2 = Math.min(this.textLines.size(), max + i4);
        float f5 = f3 - fontMetrics.ascent;
        while (i4 < min2) {
            canvas.drawText(this.textLines.get(i4), f2, f5, paint);
            f5 += f4;
            i4++;
        }
        return createBitmap;
    }

    private int linesPerPage(int i) {
        return Math.max(1, (int) ((i - 130) / 34.0f));
    }

    private List<String> wrapText(String str, int i, int i2) {
        Paint paint = new Paint(1);
        paint.setTextSize(22.0f);
        paint.setTypeface(Typeface.create("sans-serif", 0));
        float f = i - 124.0f;
        ArrayList arrayList = new ArrayList();
        String[] split = str.replace("\r", "").split("\n", -1);
        int length = split.length;
        for (int i3 = 0; i3 < length; i3++) {
            String str2 = split[i3];
            if (str2.isEmpty()) {
                arrayList.add("");
            } else {
                while (!str2.isEmpty()) {
                    int breakText = paint.breakText(str2, true, f, null);
                    if (breakText <= 0) {
                        breakText = 1;
                    }
                    int lastIndexOf = str2.lastIndexOf(32, Math.min(breakText, str2.length() - 1));
                    if (lastIndexOf > breakText * 0.55f) {
                        breakText = lastIndexOf;
                    }
                    arrayList.add(str2.substring(0, breakText).trim());
                    str2 = str2.substring(breakText).trim();
                }
            }
        }
        if (arrayList.isEmpty()) {
            arrayList.add("(Empty document)");
        }
        return arrayList;
    }

    private String extractTextDocument(String str) throws Exception {
        if (isOfficeOpenXml(str) || "odt".equals(str) || "ods".equals(str) || "odp".equals(str)) {
            return extractOfficeArchive(str);
        }
        byte[] readLimited;
        InputStream openInputStream = this.resolver.openInputStream(this.uri);
        if (openInputStream == null) {
            return null;
        }
        try {
            readLimited = readLimited(openInputStream, 12582912);
        } finally {
            openInputStream.close();
        }
        if (readLimited.length == 0) {
            return null;
        }
        String str2 = new String(readLimited, StandardCharsets.UTF_8);
        String result;
        if ("rtf".equals(str)) {
            result = str2.replaceAll("(?s)\\\\'[0-9a-fA-F]{2}", "").replaceAll("\\\\par[d]?", "\n").replaceAll("\\\\line", "\n").replaceAll("\\\\[a-zA-Z]+-?\\d* ?", "").replaceAll("[{}]", "");
        } else if ("html".equals(str) || "htm".equals(str) || str2.trim().startsWith("<!DOCTYPE html")) {
            result = Html.fromHtml(str2, 0).toString();
        } else {
            if ("doc".equals(str) || "xls".equals(str) || "ppt".equals(str) || str2.indexOf(0) >= 0) {
                return null;
            }
            boolean sawText = false;
            for (int i = 0; i < Math.min(str2.length(), 200); i++) {
                char charAt = str2.charAt(i);
                if (!Character.isISOControl(charAt) || charAt == '\n' || charAt == '\r' || charAt == '\t') {
                    sawText = true;
                } else if (charAt != 0 && charAt != 7 && charAt != '\b') {
                    return null;
                }
            }
            if (!sawText) {
                return null;
            }
            result = str2;
        }
        return result.trim().isEmpty() ? "(Empty document)" : result;
    }

    private String extractOfficeArchive(String str) throws Exception {
        TreeMap<String, String> treeMap = new TreeMap<>();
        InputStream openInputStream = this.resolver.openInputStream(this.uri);
        if (openInputStream == null) {
            if (openInputStream != null) {
                openInputStream.close();
            }
            return null;
        }
        try {
            ZipInputStream zipInputStream = new ZipInputStream(openInputStream);
            while (true) {
                try {
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    if (nextEntry == null) {
                        break;
                    }
                    String name = nextEntry.getName();
                    if (("docx".equals(str) && "word/document.xml".equals(name)) || (("xlsx".equals(str) && (name.equals("xl/sharedStrings.xml") || name.matches("xl/worksheets/sheet\\d+\\.xml"))) || (("pptx".equals(str) && name.matches("ppt/slides/slide\\d+\\.xml")) || (("odt".equals(str) || "ods".equals(str) || "odp".equals(str)) && "content.xml".equals(name))))) {
                        treeMap.put(name, new String(readLimited(zipInputStream, 15728640), StandardCharsets.UTF_8));
                    }
                    zipInputStream.closeEntry();
                } finally {
                }
            }
            zipInputStream.close();
            if (openInputStream != null) {
                openInputStream.close();
            }
            if (treeMap.isEmpty()) {
                return null;
            }
            if ("docx".equals(str)) {
                return extractXmlText(treeMap.get("word/document.xml"), true);
            }
            if (!"pptx".equals(str)) {
                return "odt".equals(str) ? extractXmlText(treeMap.get("content.xml"), true) : "xlsx".equals(str) ? extractSpreadsheet(treeMap) : extractXmlText(treeMap.values().iterator().next(), true);
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : treeMap.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("\n\n--- Slide ---\n");
                }
                sb.append(extractXmlText(entry.getValue(), true));
            }
            return sb.toString();
        } catch (Throwable th) {
            if (openInputStream != null) {
                try {
                    openInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    private String extractSpreadsheet(Map<String, String> map) throws Exception {
        char c;
        String str;
        String sb;
        ArrayList arrayList = new ArrayList();
        String str2 = map.get("xl/sharedStrings.xml");
        char c2 = 3;
        int i = 2;
        int i2 = 1;
        if (str2 != null) {
            XmlPullParser newPullParser = Xml.newPullParser();
            newPullParser.setInput(new StringReader(str2));
            StringBuilder sb2 = null;
            for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                String name = newPullParser.getName();
                if (eventType == 2 && "si".equals(name)) {
                    sb2 = new StringBuilder();
                } else if (eventType == 4 && sb2 != null) {
                    sb2.append(newPullParser.getText());
                } else if (eventType == 3 && "si".equals(name)) {
                    if (sb2 == null) {
                        sb = "";
                    } else {
                        sb = sb2.toString();
                    }
                    arrayList.add(sb);
                    sb2 = null;
                }
            }
        }
        StringBuilder sb3 = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> next = it.next();
            if (next.getKey().startsWith("xl/worksheets/")) {
                XmlPullParser newPullParser2 = Xml.newPullParser();
                newPullParser2.setInput(new StringReader(next.getValue()));
                int eventType2 = newPullParser2.getEventType();
                String str3 = "";
                String str4 = str3;
                boolean z = false;
                boolean z2 = false;
                StringBuilder sb4 = null;
                while (eventType2 != i2) {
                    String name2 = newPullParser2.getName();
                    Iterator<Map.Entry<String, String>> it2 = it;
                    if (eventType2 == i) {
                        if ("row".equals(name2)) {
                            c = 3;
                        } else if ("c".equals(name2)) {
                            str3 = newPullParser2.getAttributeValue(null, "t");
                            sb4 = new StringBuilder();
                            str4 = "";
                            c = 3;
                        } else if ("v".equals(name2)) {
                            c = 3;
                            z = true;
                        } else {
                            c = 3;
                            if ("is".equals(name2)) {
                                z2 = true;
                            }
                        }
                    } else if (eventType2 == 4) {
                        if (z) {
                            str4 = str4 + newPullParser2.getText();
                        } else if (z2 && sb4 != null) {
                            sb4.append(newPullParser2.getText());
                        }
                        c = 3;
                    } else {
                        c = 3;
                        if (eventType2 == 3) {
                            if ("v".equals(name2)) {
                                z = false;
                            } else if ("is".equals(name2)) {
                                z2 = false;
                            } else if ("c".equals(name2)) {
                                str = str4;
                                if ("s".equals(str3)) {
                                    try {
                                        int parseInt = Integer.parseInt(str4);
                                        if (parseInt >= 0 && parseInt < arrayList.size()) {
                                            str = (String) arrayList.get(parseInt);
                                        }
                                    } catch (NumberFormatException unused) {
                                    }
                                } else if (sb4 != null && sb4.length() != 0) {
                                    str = sb4.toString();
                                }
                                sb3.append(str);
                                sb3.append("    ");
                            } else if ("row".equals(name2)) {
                                sb3.append('\n');
                            }
                        }
                    }
                    eventType2 = newPullParser2.next();
                    c2 = c;
                    it = it2;
                    i = 2;
                    i2 = 1;
                }
                sb3.append("\n\n");
                it = it;
                i = 2;
                i2 = 1;
            }
        }
        return sb3.toString();
    }

    private String extractXmlText(String str, boolean z) throws Exception {
        if (str == null || str.isEmpty()) {
            return null;
        }
        XmlPullParser newPullParser = Xml.newPullParser();
        newPullParser.setInput(new StringReader(str));
        StringBuilder sb = new StringBuilder();
        for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
            String name = newPullParser.getName();
            if (eventType == 4) {
                if (newPullParser.getText() != null) {
                    sb.append(newPullParser.getText());
                }
            } else if (eventType == 3 && z) {
                if ("p".equals(name) || "tr".equals(name) || "h".equals(name)) {
                    sb.append('\n');
                } else if ("tc".equals(name) || "table-cell".equals(name)) {
                    sb.append("    ");
                }
            }
        }
        return sb.toString().replaceAll("[ \\t]+\\n", "\n").trim();
    }

    private static byte[] readLimited(InputStream inputStream, int i) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        int i2 = 0;
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                return byteArrayOutputStream.toByteArray();
            }
            i2 += read;
            if (i2 > i) {
                throw new IllegalArgumentException("The selected document is too large to preview safely.");
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    private static boolean isOfficeOpenXml(String str) {
        return "docx".equals(str) || "xlsx".equals(str) || "pptx".equals(str);
    }

    private static boolean isImageExtension(String str) {
        return "jpg".equals(str) || "jpeg".equals(str) || "png".equals(str) || "webp".equals(str) || "bmp".equals(str) || "gif".equals(str) || "heic".equals(str) || "heif".equals(str) || "avif".equals(str) || "tif".equals(str) || "tiff".equals(str);
    }

    public static String extensionOf(String str) {
        int lastIndexOf;
        return (str == null || (lastIndexOf = str.lastIndexOf(46)) < 0 || lastIndexOf == str.length() + (-1)) ? "" : str.substring(lastIndexOf + 1).toLowerCase(Locale.ROOT);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        PdfRenderer pdfRenderer = this.pdfRenderer;
        if (pdfRenderer != null) {
            try {
                pdfRenderer.close();
            } catch (Exception unused) {
            }
            this.pdfRenderer = null;
        }
        ParcelFileDescriptor parcelFileDescriptor = this.pdfDescriptor;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (Exception unused2) {
            }
            this.pdfDescriptor = null;
        }
        Bitmap bitmap = this.imageBitmap;
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        this.imageBitmap.recycle();
        this.imageBitmap = null;
    }
}
