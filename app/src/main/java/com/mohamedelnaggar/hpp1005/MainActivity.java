package com.mohamedelnaggar.hpp1005;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.Window;
import android.view.WindowManager;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileInputStream;

public class MainActivity extends Activity {
    private static final String ACTION_USB_PERMISSION =
            "com.mohamedelnaggar.hpp1005.USB_PERMISSION";
    private UsbManager usbManager;
    private TextView output;
    private PendingIntent permissionIntent;

    private SharedPreferences uiPreferences;
    private final StringBuilder activityLog = new StringBuilder();
    private String currentPage = "home";
    private String themeMode = "system"; // system, light, dark
    private boolean darkMode;
    private boolean hapticsEnabled = true;
    private boolean keepScreenAwake;
    private boolean confirmFirmwareLoad = true;
    private boolean nativeBlurEnabled = true;

    private static final int REQUEST_DOCUMENT = 42;
    private Uri selectedDocumentUri;
    private String selectedDocumentName = "";
    private String selectedDocumentMime = "";
    private DocumentRenderer documentRenderer;
    private Bitmap currentPreviewBitmap;
    private int currentPreviewPage = 0;
    private SharedPreferences printPreferences;
    private String paperSize = "A4";
    private int copies = 1;
    private String pageRange = "all";
    private String duplexMode = "off"; // off, manual-long, manual-short; the P1005 has no automatic duplex unit
    private String pageOrientation = "portrait";
    private int printDensity = 3;
    private boolean draftMode;
    private boolean fitToPage = true;
    private volatile boolean printInProgress;
    private final String[] paperSizes = {"A4", "A5", "Letter", "Legal", "B5"};

    // Current print job shown on the queue page. Pages already accepted by USB
    // cannot be retracted, so cancel stops the remaining pages only.
    private volatile boolean jobCancelRequested;
    private volatile String currentJobName;
    private volatile int currentJobTotal;
    private volatile int currentJobDone;

    // Printer setup flow: 0 pending, 1 running, 2 done, 3 inconclusive, 4 failed
    private final int[] setupSteps = {0, 0, 0};
    private boolean setupRunning;
    private boolean setupDone;
    private boolean setupFinishedWithWarning;

    private interface ToggleAction {
        void onToggle(boolean enabled);
    }

    private interface UsbResultCallback {
        void onResult(boolean success, String detail);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                UsbDevice device = getUsbDevice(intent);
                if (device == null) {
                    append("وصل رد إذن USB دون جهاز مرفق.");
                } else if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    append("مُنح إذن USB · HP LaserJet P1005");
                    inspectDevice(device);
                } else {
                    append("رُفض إذن USB لـ " + describe(device));
                }
                renderApp();
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                append("وُصِل جهاز USB. افحص الأجهزة من صفحة الطباعة.");
                renderApp();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                append("فُصِل جهاز USB.");
                renderApp();
            }
        }
    };

    @SuppressWarnings("deprecation")
    private UsbDevice getUsbDevice(Intent intent) {
        if (Build.VERSION.SDK_INT >= 33) {
            return intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice.class);
        }
        return intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) currentPage = state.getString("page", "home");
        applyArabicLocale();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        uiPreferences = getSharedPreferences("hp_p1005_preferences", MODE_PRIVATE);
        if (uiPreferences.contains("dark_mode") && !uiPreferences.contains("theme_mode")) {
            uiPreferences.edit().putString("theme_mode",
                    uiPreferences.getBoolean("dark_mode", false) ? "dark" : "system").apply();
        }
        themeMode = uiPreferences.getString("theme_mode", "system");
        hapticsEnabled = uiPreferences.getBoolean("haptics", true);
        keepScreenAwake = uiPreferences.getBoolean("keep_awake", false);
        confirmFirmwareLoad = uiPreferences.getBoolean("confirm_firmware", true);
        nativeBlurEnabled = uiPreferences.getBoolean("native_blur", true);
        applyThemeMode();
        printPreferences = getSharedPreferences("hp_p1005_print_preferences", MODE_PRIVATE);
        paperSize = printPreferences.getString("paper_size", "A4");
        copies = printPreferences.getInt("copies", 1);
        pageRange = printPreferences.getString("page_range", "all");
        duplexMode = printPreferences.getString("duplex_mode", "off");
        pageOrientation = printPreferences.getString("orientation", "portrait");
        printDensity = printPreferences.getInt("density", 3);
        draftMode = printPreferences.getBoolean("draft_mode", false);
        fitToPage = printPreferences.getBoolean("fit_to_page", true);
        if (activityLog.length() == 0) {
            activityLog.append("سجل نشاط HP P1005\n");
            activityLog.append("جاهز. صِل الطابعة بمحوّل USB OTG ثم افحص الأجهزة من صفحة الطباعة.\n");
        }

        Intent permission = new Intent(ACTION_USB_PERMISSION).setPackage(getPackageName());
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        // UsbManager adds EXTRA_DEVICE and EXTRA_PERMISSION_GRANTED to this broadcast.
        if (Build.VERSION.SDK_INT >= 31) {
            pendingFlags |= PendingIntent.FLAG_MUTABLE;
        } else if (Build.VERSION.SDK_INT >= 23) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        permissionIntent = PendingIntent.getBroadcast(this, 0, permission, pendingFlags);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(usbReceiver, filter);
        }

        if (!handleIncomingIntent(getIntent())) renderApp();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("page", currentPage);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (!handleIncomingIntent(intent)) renderApp();
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyArabicLocale();
        applyThemeMode();
        renderApp();
    }

    @Override protected void attachBaseContext(Context newBase) {
        Locale arabic = new Locale("ar");
        Configuration override = new Configuration();
        override.setLocale(arabic);
        override.setLayoutDirection(arabic);
        super.attachBaseContext(newBase.createConfigurationContext(override));
    }

    private boolean debuggable() {
        return (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private void applyArabicLocale() {
        Locale.setDefault(new Locale("ar"));
    }

    private void applyThemeMode() {
        boolean systemDark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        darkMode = "dark".equals(themeMode) || ("system".equals(themeMode) && systemDark);
    }

    private String themeModeLabel() {
        if ("dark".equals(themeMode)) return "داكن";
        if ("light".equals(themeMode)) return "فاتح";
        return "حسب النظام";
    }

    private boolean handleIncomingIntent(Intent intent) {
        if (intent == null) return false;
        Uri incoming = intent.getData();
        Uri stream = null;
        try { stream = intent.getParcelableExtra(Intent.EXTRA_STREAM); } catch (Exception ignored) {}
        if (debuggable()) {
            android.util.Log.i("HP1005-Share", "action=" + intent.getAction()
                    + " data=" + incoming + " stream=" + stream
                    + " extras=" + (intent.getExtras() == null ? "none" : intent.getExtras().keySet()));
        } else {
            android.util.Log.i("HP1005-Share", "action=" + intent.getAction()
                    + " hasData=" + (incoming != null) + " hasStream=" + (stream != null));
        }
        if (incoming == null && Intent.ACTION_SEND.equals(intent.getAction())) incoming = stream;
        if (incoming != null) {
            loadSelectedDocument(incoming);
            return true;
        }
        return false;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int pageBackground() { return darkMode ? 0xFF101013 : 0xFFF5F5F5; }
    private int cardColor() { return darkMode ? 0xFF1D1D21 : 0xFFFFFFFF; }
    private int primaryColor() { return darkMode ? 0xFF4C9AFF : 0xFF0072DE; }
    private int primaryDeep() { return darkMode ? 0xFF0E5DBF : 0xFF0E63C4; }
    private int textPrimary() { return darkMode ? 0xFFF2F2F4 : 0xFF1B1B1B; }
    private int textSecondary() { return darkMode ? 0xFF9C9CA4 : 0xFF7A7A80; }
    private int eyebrowColor() { return darkMode ? 0xFF8FB9E8 : 0xFF6E84A3; }
    private int borderColor() { return darkMode ? 0x0DFFFFFF : 0x0A0F1928; }
    private int dividerColor() { return darkMode ? 0x12FFFFFF : 0x0F0F141E; }
    private int tonalBg() { return darkMode ? 0xFF12283F : 0xFFE3EFFE; }
    private int tonalText() { return darkMode ? 0xFF4C9AFF : 0xFF0072DE; }
    private int okBg() { return darkMode ? 0xFF14301F : 0xFFE3F6EA; }
    private int okText() { return darkMode ? 0xFF4CD686 : 0xFF14854B; }
    private int warnBg() { return darkMode ? 0xFF33260F : 0xFFFDF0DC; }
    private int warnText() { return darkMode ? 0xFFF0A94C : 0xFFB26A12; }
    private int trackColor() { return darkMode ? 0xFF2A2A2F : 0xFFEDF0F4; }
    private int iconWell() { return darkMode ? 0xFF12283F : 0xFFE3EFFE; }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private GradientDrawable rounded(int startColor, int endColor, int strokeColor, int radius) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{startColor, endColor});
        drawable.setCornerRadius(dp(radius));
        if (strokeColor != 0) drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private GradientDrawable flat(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private GradientDrawable verticalGradient(int topColor, int bottomColor, int radius) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{topColor, bottomColor});
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private TextView styledText(String value, float size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setFontFeatureSettings("kern");
        text.setIncludeFontPadding(true);
        text.setTypeface(Typeface.create(bold ? "sans-serif-medium" : "sans-serif",
                bold ? Typeface.BOLD : Typeface.NORMAL));
        return text;
    }

    private TextView label(String value, float size, int color, boolean bold) {
        return styledText(value, size, color, bold);
    }

    private ImageView iconView(int drawableRes, int tintColor, int sizeDp) {
        ImageView icon = new ImageView(this);
        icon.setImageResource(drawableRes);
        icon.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp));
        icon.setLayoutParams(params);
        return icon;
    }

    private Button bigPrimaryButton(String title) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(Color.WHITE);
        button.setBackground(verticalGradient(primaryColor(), primaryDeep(), 27));
        button.setMinHeight(dp(56));
        button.setPadding(dp(14), 0, dp(14), 0);
        button.setHapticFeedbackEnabled(hapticsEnabled);
        if (Build.VERSION.SDK_INT >= 21) {
            button.setElevation(dp(2));
            button.setStateListAnimator(null);
        }
        return button;
    }

    private Button tonalButton(String title, float sizeSp) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setTextSize(sizeSp);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(tonalText());
        button.setBackground(flat(tonalBg(), 24));
        button.setMinHeight(dp(52));
        button.setPadding(dp(14), 0, dp(14), 0);
        button.setHapticFeedbackEnabled(hapticsEnabled);
        if (Build.VERSION.SDK_INT >= 21) button.setStateListAnimator(null);
        return button;
    }

    private Toast systemToast(String message, int duration) {
        android.content.Context themed = new android.view.ContextThemeWrapper(getApplicationContext(),
                darkMode ? android.R.style.Theme_DeviceDefault
                        : android.R.style.Theme_DeviceDefault_Light);
        return Toast.makeText(themed, message, duration);
    }

    private LinearLayout card() {
        LinearLayout card = vertical();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(flat(cardColor(), 26));
        card.setClipToOutline(true);
        // No elevation: the reviewed cards are flat; a 1dp shadow ring read as thin
        // dirty edge lines on the light theme.
        return card;
    }

    private TextView statusPill(boolean connected) {
        String value = connected ? "متصلة" : "بانتظار الاتصال";
        TextView pill = label("●  " + value, 11, connected ? okText() : warnText(), true);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(10), dp(5), dp(10), dp(5));
        pill.setBackground(flat(connected ? okBg() : warnBg(), 16));
        return pill;
    }

    private TextView sectionLabel(String value) {
        TextView label = label(value, 13, textPrimary(), true);
        label.setPadding(dp(2), 0, dp(2), 0);
        return label;
    }

    private boolean isTabPage(String page) {
        return "home".equals(page) || "print".equals(page) || "queue".equals(page);
    }

    private boolean wideLayout() {
        return getResources().getConfiguration().screenWidthDp >= 600;
    }

    private void addPair(LinearLayout content, View first, View second) {
        addPair(content, first, second, 1f, 1f);
    }

    private void addPair(LinearLayout content, View first, View second,
                         float firstWeight, float secondWeight) {
        if (!wideLayout()) {
            content.addView(first, margin(0, 0, 0, 14));
            content.addView(second, margin(0, 0, 0, 14));
            return;
        }
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.addView(first, new LinearLayout.LayoutParams(0, -2, firstWeight));
        LinearLayout.LayoutParams secondParams = new LinearLayout.LayoutParams(0, -2, secondWeight);
        secondParams.setMarginStart(dp(16));
        row.addView(second, secondParams);
        content.addView(row, margin(0, 0, 0, 14));
    }

    private void renderApp() {
        int bg = pageBackground();
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(darkMode ? 0 :
                (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR));
        if (keepScreenAwake) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(bg);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        LinearLayout content = vertical();
        boolean tabPage = isTabPage(currentPage);
        // Landscape/wide two-column pages halve the column heights, so 118dp of bottom
        // padding can leave ~zero scroll range and the waiting card stuck under the
        // nav capsule. Wide layouts need enough range to lift the last row clear.
        content.setPadding(dp(20), dp(16), dp(20), dp(tabPage ? (wideLayout() ? 210 : 118) : 42));
        buildPage(content);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        boolean wide = wideLayout();
        boolean roomy = wide && ("home".equals(currentPage) || "print".equals(currentPage)
                || "queue".equals(currentPage) || "preview".equals(currentPage)
                || "settings".equals(currentPage));
        int windowWidthDp = getResources().getConfiguration().screenWidthDp;
        int cap = !wide ? -1 : dp(Math.min(roomy ? 880 : 640, windowWidthDp));
        root.addView(scroll, new FrameLayout.LayoutParams(cap == -1 ? -1 : cap, -1,
                Gravity.CENTER_HORIZONTAL));

        if (tabPage) {
            FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(-2, -2,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            navParams.setMargins(0, 0, 0, dp(16));
            root.addView(buildCapsuleNav(), navParams);
        }
        setContentView(root);
    }

    private void pageAppBar(LinearLayout content, String title) {
        LinearLayout bar = horizontal();
        bar.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout back = new LinearLayout(this);
        back.setGravity(Gravity.CENTER);
        back.setBackground(flat(cardColor(), 19));
        back.setContentDescription("رجوع");
        // Flat like the reviewed design (no elevation ring artifacts).
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_arrow_back);
        arrow.setColorFilter(textPrimary(), PorterDuff.Mode.SRC_IN);
        back.addView(arrow, new LinearLayout.LayoutParams(dp(20), dp(20)));
        back.setClickable(true);
        back.setFocusable(true);
        back.setHapticFeedbackEnabled(hapticsEnabled);
        back.setOnClickListener(v -> { performTapFeedback(v); navigateBack(); });
        bar.addView(back, new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView barTitle = label(title, 17, textPrimary(), true);
        LinearLayout.LayoutParams barTitleParams = new LinearLayout.LayoutParams(-2, -2);
        barTitleParams.setMarginStart(dp(12));
        bar.addView(barTitle, barTitleParams);
        content.addView(bar, margin(0, 2, 0, 14));
    }

    private LinearLayout buildCapsuleNav() {
        LinearLayout capsule = horizontal();
        capsule.setGravity(Gravity.CENTER);
        capsule.setPadding(dp(8), dp(8), dp(8), dp(8));
        // Reviewed proposal: frosted translucent capsule (55% tint). Real backdrop
        // blur behind it is staged separately with runtime verification; this keeps
        // the reviewed tint/geometry on every API level.
        capsule.setBackground(flat(darkMode ? 0x8C1C1C20 : 0x8CFFFFFF, 38));
        capsule.setClipToOutline(true);
        if (Build.VERSION.SDK_INT >= 21) capsule.setElevation(dp(14));
        String selected = selectedBottomTab();
        capsule.addView(navItem(R.drawable.ic_nav_home, "الرئيسية", "home", "home".equals(selected)));
        capsule.addView(navItem(R.drawable.ic_nav_printer, "طباعة", "print", "print".equals(selected)));
        capsule.addView(navItem(R.drawable.ic_nav_queue, "قائمة الطباعة", "queue", "queue".equals(selected)));
        return capsule;
    }

    private String selectedBottomTab() {
        if ("home".equals(currentPage) || "setup".equals(currentPage)) return "home";
        if ("print".equals(currentPage) || "preview".equals(currentPage)) return "print";
        if ("queue".equals(currentPage)) return "queue";
        return "home";
    }

    private LinearLayout navItem(int iconRes, String title, String page, boolean active) {
        LinearLayout item = vertical();
        item.setGravity(Gravity.CENTER);
        // Reviewed proposal: equal-width tab slots so the selection oval never
        // depends on label width; the long queue label ellipsizes inside its slot.
        item.setPadding(dp(0), dp(9), dp(0), dp(8));
        if (active) item.setBackground(flat(darkMode ? 0xFF6E6E73 : 0xFFE9E9EE, 24));
        int ink = active ? (darkMode ? Color.WHITE : 0xFF1B1B1B) : textSecondary();
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(ink, PorterDuff.Mode.SRC_IN);
        item.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView navLabel = label(title, 10.5f, ink, true);
        navLabel.setMaxLines(1);
        // Complete labels only: no truncation. Slot width/label size are verified on
        // phone and tablet pixels; adjust slot geometry, never cut the words.
        LinearLayout.LayoutParams navLabelParams = new LinearLayout.LayoutParams(-2, -2);
        navLabelParams.topMargin = dp(3);
        item.addView(navLabel, navLabelParams);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(dp(96), -2);
        item.setLayoutParams(itemParams);
        item.setContentDescription(title);
        item.setClickable(true);
        item.setFocusable(true);
        item.setHapticFeedbackEnabled(hapticsEnabled);
        item.setOnClickListener(v -> { performTapFeedback(v); openPage(page); });
        return item;
    }

    private void buildPage(LinearLayout content) {
        output = null;
        switch (currentPage) {
            case "preview": buildPreviewPage(content); break;
            case "print": buildPrintPage(content); break;
            case "queue": buildQueuePage(content); break;
            case "setup": buildSetupPage(content); break;
            case "activity": buildActivityPage(content); break;
            case "settings": buildSettingsPage(content); break;
            case "about": buildAboutPage(content); break;
            case "help": buildHelpPage(content); break;
            default: buildHomePage(content); break;
        }
    }

    private void pageHeader(LinearLayout parent, String eyebrow, String title, String subtitle,
                            boolean withSettings) {
        LinearLayout header = horizontal();
        header.setGravity(Gravity.TOP);
        LinearLayout texts = vertical();
        if (eyebrow != null) {
            texts.addView(label(eyebrow, 11, eyebrowColor(), true), margin(0, 4, 0, 6));
        }
        TextView heading = label(title, 29, textPrimary(), true);
        heading.setLineSpacing(0, 1.02f);
        texts.addView(heading, margin(0, 0, 0, 5));
        if (subtitle != null) {
            TextView description = label(subtitle, 13, textSecondary(), false);
            description.setLineSpacing(dp(3), 1.1f);
            texts.addView(description);
        }
        header.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));
        if (withSettings) {
            LinearLayout gear = new LinearLayout(this);
            gear.setGravity(Gravity.CENTER);
            gear.setBackground(flat(cardColor(), 18));
            gear.setContentDescription("الإعدادات");
            // Flat like the reviewed design.
            ImageView icon = new ImageView(this);
            icon.setImageResource(R.drawable.ic_settings);
            icon.setColorFilter(textPrimary(), PorterDuff.Mode.SRC_IN);
            gear.addView(icon, new LinearLayout.LayoutParams(dp(19), dp(19)));
            gear.setClickable(true);
            gear.setFocusable(true);
            gear.setHapticFeedbackEnabled(hapticsEnabled);
            gear.setOnClickListener(v -> { performTapFeedback(v); openPage("settings"); });
            LinearLayout.LayoutParams gearParams = new LinearLayout.LayoutParams(dp(36), dp(36));
            gearParams.setMargins(dp(10), dp(6), 0, 0);
            header.addView(gear, gearParams);
        }
        parent.addView(header, margin(0, 0, 0, 16));
    }

    private void openPage(String page) {
        currentPage = page;
        renderApp();
    }

    private void navigateBack() {
        switch (currentPage) {
            case "preview": openPage("print"); break;
            case "about":
            case "help":
            case "activity": openPage("settings"); break;
            case "settings":
            case "setup": openPage("home"); break;
            case "print":
            case "queue": openPage("home"); break;
            default: break;
        }
    }

    @Override public void onBackPressed() {
        if (!"home".equals(currentPage)) {
            navigateBack();
        } else {
            super.onBackPressed();
        }
    }

    private void performTapFeedback(View view) {
        if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private String readAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {
            return "الإصدار غير متاح";
        }
    }

    private void buildHomePage(LinearLayout content) {
        pageHeader(content, null, "الرئيسية", null, true);

        boolean connected = hasPrinterConnected();
        LinearLayout hero = card();
        LinearLayout heroRow = horizontal();
        heroRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout htext = vertical();
        htext.addView(label("HP LaserJet P1005", 16.5f, textPrimary(), true));
        htext.addView(label("طابعة ليزر أحادية اللون · USB OTG", 12, textSecondary(), false),
                margin(0, 3, 0, 0));
        LinearLayout pillWrap = vertical();
        pillWrap.addView(statusPill(connected), new LinearLayout.LayoutParams(-2, -2));
        htext.addView(pillWrap, margin(0, 9, 0, 0));
        heroRow.addView(htext, new LinearLayout.LayoutParams(0, -2, 1f));
        ImageView photo = new ImageView(this);
        photo.setImageResource(R.drawable.p1005_photo);
        photo.setAdjustViewBounds(true);
        photo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        photo.setContentDescription("طابعة HP LaserJet P1005");
        LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(dp(118), dp(104));
        photoParams.setMargins(0, 0, dp(14), 0);
        heroRow.addView(photo, photoParams);
        hero.addView(heroRow, margin(0, 0, 0, 14));
        LinearLayout actions = horizontal();
        Button printDoc = new Button(this);
        printDoc.setText("طباعة مستند");
        printDoc.setAllCaps(false);
        printDoc.setTextSize(14.5f);
        printDoc.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        printDoc.setTextColor(Color.WHITE);
        printDoc.setBackground(verticalGradient(primaryColor(), primaryDeep(), 24));
        printDoc.setMinHeight(dp(48));
        if (Build.VERSION.SDK_INT >= 21) printDoc.setStateListAnimator(null);
        printDoc.setHapticFeedbackEnabled(hapticsEnabled);
        printDoc.setOnClickListener(v -> { performTapFeedback(v); openDocumentPicker(false); });
        actions.addView(printDoc, new LinearLayout.LayoutParams(0, dp(48), 1f));
        Button printPhoto = tonalButton("طباعة الصور", 14.5f);
        printPhoto.setOnClickListener(v -> { performTapFeedback(v); openDocumentPicker(true); });
        LinearLayout.LayoutParams photoBtnParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        photoBtnParams.setMarginStart(dp(10));
        actions.addView(printPhoto, photoBtnParams);
        hero.addView(actions, margin(0, 0, 0, 0));
        LinearLayout recent = card();
        LinearLayout recentHeader = horizontal();
        recentHeader.addView(label("النشاط الأخير", 15.5f, textPrimary(), true),
                new LinearLayout.LayoutParams(0, -2, 1f));
        TextView viewAll = label("عرض الكل", 12, tonalText(), true);
        viewAll.setPadding(dp(8), dp(6), 0, dp(6));
        viewAll.setOnClickListener(v -> { performTapFeedback(v); openPage("queue"); });
        recentHeader.addView(viewAll);
        recent.addView(recentHeader, margin(0, 0, 0, 10));
        ArrayList<String> lines = recentLogLines(3);
        if (lines.isEmpty()) {
            recent.addView(label("لا يوجد نشاط بعد. صِل الطابعة وافحص أجهزة USB.",
                    11.5f, textSecondary(), false));
        } else {
            for (String line : lines) {
                TextView logLine = label("•  " + line, 11.5f, textSecondary(), false);
                logLine.setLineSpacing(dp(4), 1f);
                recent.addView(logLine, margin(0, 0, 0, 5));
            }
        }
        LinearLayout setup = new LinearLayout(this);
        setup.setGravity(Gravity.CENTER);
        setup.setOrientation(LinearLayout.HORIZONTAL);
        setup.setBackground(flat(tonalBg(), 27));
        setup.setHapticFeedbackEnabled(hapticsEnabled);
        ImageView setupIcon = new ImageView(this);
        setupIcon.setImageResource(R.drawable.ic_nav_printer);
        setupIcon.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
        setup.addView(setupIcon, new LinearLayout.LayoutParams(dp(20), dp(20)));
        TextView setupTitle = label("إعداد الطابعة", 16, tonalText(), true);
        LinearLayout.LayoutParams setupTitleParams = new LinearLayout.LayoutParams(-2, -2);
        setupTitleParams.setMarginStart(dp(9));
        setup.addView(setupTitle, setupTitleParams);
        setup.setClickable(true);
        setup.setFocusable(true);
        setup.setOnClickListener(v -> { performTapFeedback(v); openPage("setup"); });
        LinearLayout homeSide = vertical();
        homeSide.addView(recent, margin(0, 0, 0, 14));
        homeSide.addView(setup, new LinearLayout.LayoutParams(-1, dp(56)));
        addPair(content, hero, homeSide);
    }

    private ArrayList<String> recentLogLines(int max) {
        String[] lines = activityLog.toString().trim().split("\\n");
        ArrayList<String> result = new ArrayList<>();
        for (int i = lines.length - 1; i >= 0 && result.size() < max; i--) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.contains("سجل نشاط HP P1005")
                    || line.contains("HP P1005 activity log")) continue;
            result.add(0, line);
        }
        return result;
    }

    private void buildPrintPage(LinearLayout content) {
        pageHeader(content, null, "طباعة", "اختر ملفًا للمعاينة والطباعة.", false);

        LinearLayout docs = card();
        LinearLayout docsHeader = horizontal();
        LinearLayout docWell = new LinearLayout(this);
        docWell.setGravity(Gravity.CENTER);
        docWell.setBackground(flat(iconWell(), 15));
        ImageView docIcon = new ImageView(this);
        docIcon.setImageResource(R.drawable.ic_doc);
        docIcon.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
        docWell.addView(docIcon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        docsHeader.addView(docWell, new LinearLayout.LayoutParams(dp(46), dp(46)));
        LinearLayout docsText = vertical();
        docsText.setPaddingRelative(dp(12), 0, 0, 0);
        docsText.addView(label("المستندات والصور", 16, textPrimary(), true), margin(0, 0, 0, 4));
        docsText.addView(label(selectedDocumentUri == null ? "PDF وصور وملفات Office ونصوص"
                : selectedDocumentName, 11.5f, textSecondary(), false));
        docsHeader.addView(docsText, new LinearLayout.LayoutParams(0, -2, 1f));
        docs.addView(docsHeader, margin(0, 0, 0, 13));
        TextView docsNote = label(selectedDocumentUri == null
                ? "تصفّح الجهاز أو خدمة سحابية. تحتفظ ملفات PDF والصور بتنسيقها؛ تُستورَد ملفات Office كتخطيط نصي."
                : documentRenderer != null && documentRenderer.isSupported()
                    ? documentRenderer.getPageCount() + " صفحة · " + documentRenderer.getFormatLabel()
                    : "هذا النوع يحتاج إلى فتحه أو تصديره كملف PDF من تطبيق متوافق قبل الطباعة المباشرة.",
                12, textSecondary(), false);
        docsNote.setLineSpacing(dp(3), 1f);
        docs.addView(docsNote, margin(0, 0, 0, 13));
        LinearLayout docButtons = horizontal();
        Button browse = bigPrimaryButton(selectedDocumentUri == null ? "اختيار ملف" : "اختيار ملف آخر");
        browse.setTextSize(14.5f);
        browse.setMinHeight(dp(48));
        browse.setOnClickListener(v -> { performTapFeedback(v); openDocumentPicker(false); });
        docButtons.addView(browse, new LinearLayout.LayoutParams(0, dp(48), 1f));
        Button preview = tonalButton("المعاينة والطباعة", 13.5f);
        preview.setMinHeight(dp(48));
        preview.setEnabled(selectedDocumentUri != null);
        preview.setAlpha(selectedDocumentUri != null ? 1f : 0.5f);
        preview.setOnClickListener(v -> {
            if (selectedDocumentUri == null) openDocumentPicker(false);
            else openPage("preview");
        });
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        previewParams.setMarginStart(dp(9));
        docButtons.addView(preview, previewParams);
        docs.addView(docButtons);

        LinearLayout test = card();
        LinearLayout testHeader = horizontal();
        LinearLayout testWell = new LinearLayout(this);
        testWell.setGravity(Gravity.CENTER);
        testWell.setBackground(flat(iconWell(), 15));
        ImageView testIcon = new ImageView(this);
        testIcon.setImageResource(R.drawable.ic_nav_printer);
        testIcon.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
        testWell.addView(testIcon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        testHeader.addView(testWell, new LinearLayout.LayoutParams(dp(46), dp(46)));
        LinearLayout testText = vertical();
        testText.setPaddingRelative(dp(12), 0, 0, 0);
        testText.addView(label("صفحة تجريبية مدمجة", 16, textPrimary(), true), margin(0, 0, 0, 4));
        testText.addView(label("مهمة XQX أحادية اللون من صفحة واحدة", 11.5f, textSecondary(), false));
        testHeader.addView(testText, new LinearLayout.LayoutParams(0, -2, 1f));
        test.addView(testHeader, margin(0, 0, 0, 13));
        TextView testNote = label("يُرسل التطبيق تدفق الاختبار عبر USB مباشرة؛ يؤكد ذلك مسار النقل، وتحقق من خروج الصفحة فعليًا من الطابعة.",
                12, textSecondary(), false);
        testNote.setLineSpacing(dp(3), 1f);
        test.addView(testNote, margin(0, 0, 0, 14));
        Button printTest = tonalButton("طباعة الصفحة التجريبية", 14.5f);
        printTest.setMinHeight(dp(48));
        printTest.setOnClickListener(v -> { performTapFeedback(v); printBuiltInTestPage(); });
        test.addView(printTest, new LinearLayout.LayoutParams(-1, dp(48)));
        addPair(content, docs, test);

        Button maintenance = tonalButton("صيانة الطابعة", 14.5f);
        maintenance.setMinHeight(dp(52));
        maintenance.setOnClickListener(v -> { performTapFeedback(v); openPage("settings"); });
        content.addView(maintenance, new LinearLayout.LayoutParams(-1, dp(52)));
    }

    private void addToolRow(LinearLayout parent, String title, String subtitle,
                            String action, Runnable callback) {
        LinearLayout row = horizontal();
        row.setPadding(0, dp(12), 0, dp(12));
        LinearLayout labels = vertical();
        labels.addView(label(title, 13.5f, textPrimary(), true), margin(0, 0, 0, 4));
        TextView detail = label(subtitle, 11, textSecondary(), false);
        detail.setMaxLines(3);
        labels.addView(detail);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        Button button = tonalButton(action, 11);
        button.setMinHeight(dp(38));
        button.setPadding(dp(8), 0, dp(8), 0);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(dp(76), dp(38));
        buttonParams.setMarginStart(dp(10));
        row.addView(button, buttonParams);
        button.setOnClickListener(v -> { performTapFeedback(v); callback.run(); });
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addSeparator(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundColor(dividerColor());
        parent.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
    }

    private void buildQueuePage(LinearLayout content) {
        pageHeader(content, "HP LaserJet P1005", "قائمة الطباعة",
                "المهام الجارية والمنتظرة، وسجل ما أُرسل للطابعة.", false);

        LinearLayout queueMain = vertical();
        LinearLayout queueSide = vertical();
        queueMain.addView(sectionLabel("جارية الآن"), margin(2, 0, 0, 8));
        if (printInProgress && currentJobName != null) {
            LinearLayout job = card();
            LinearLayout jobHeader = horizontal();
            LinearLayout jobWell = new LinearLayout(this);
            jobWell.setGravity(Gravity.CENTER);
            jobWell.setBackground(flat(iconWell(), 13));
            ImageView jobIcon = new ImageView(this);
            jobIcon.setImageResource(R.drawable.ic_doc);
            jobIcon.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
            jobWell.addView(jobIcon, new LinearLayout.LayoutParams(dp(20), dp(20)));
            jobHeader.addView(jobWell, new LinearLayout.LayoutParams(dp(40), dp(40)));
            LinearLayout jobText = vertical();
            jobText.setPaddingRelative(dp(11), 0, 0, 0);
            TextView jobName = label(currentJobName, 14, textPrimary(), true);
            // Same RTL correctness as the preview header: align to the view's logical
            // start so LTR filenames sit flush with the icon side and still read LTR.
            jobName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            jobName.setMaxLines(1);
            jobName.setEllipsize(TextUtils.TruncateAt.END);
            jobText.addView(jobName, margin(0, 0, 0, 3));
            jobText.addView(label("تُرسل للطابعة · صفحة " + Math.min(currentJobDone + 1, Math.max(currentJobTotal, 1))
                    + " من " + Math.max(currentJobTotal, 1), 11.5f, textSecondary(), false));
            jobHeader.addView(jobText, new LinearLayout.LayoutParams(0, -2, 1f));
            job.addView(jobHeader, margin(0, 0, 0, 4));
            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(Math.max(currentJobTotal, 1));
            bar.setProgress(Math.min(currentJobDone, Math.max(currentJobTotal, 1)));
            bar.setProgressTintList(ColorStateList.valueOf(primaryColor()));
            bar.setProgressBackgroundTintList(ColorStateList.valueOf(trackColor()));
            bar.setProgressBackgroundTintMode(PorterDuff.Mode.SRC_IN);
            job.addView(bar, new LinearLayout.LayoutParams(-1, dp(8)));
            Button cancel = tonalButton("إلغاء المهمة", 13.5f);
            cancel.setMinHeight(dp(44));
            cancel.setOnClickListener(v -> {
                performTapFeedback(v);
                jobCancelRequested = true;
                systemToast("سيُلغى الإرسال بعد اكتمال الصفحة الجارية.", Toast.LENGTH_LONG).show();
            });
            job.addView(cancel, margin(0, 11, 0, 0));
            queueMain.addView(job, margin(0, 0, 0, 16));
        } else {
            LinearLayout empty = card();
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.addView(label("لا توجد مهمة جارية الآن", 13.5f, textSecondary(), false),
                    margin(0, 6, 0, 6));
            queueMain.addView(empty, margin(0, 0, 0, 16));
        }

        queueMain.addView(sectionLabel("في الانتظار"), margin(2, 0, 0, 8));
        if (selectedDocumentUri != null) {
            LinearLayout waiting = card();
            waiting.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout row = horizontal();
            LinearLayout waitWell = new LinearLayout(this);
            waitWell.setGravity(Gravity.CENTER);
            waitWell.setBackground(flat(iconWell(), 13));
            ImageView waitIcon = new ImageView(this);
            waitIcon.setImageResource(documentRenderer != null && documentRenderer.isImage()
                    ? R.drawable.ic_image : R.drawable.ic_doc);
            waitIcon.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
            waitWell.addView(waitIcon, new LinearLayout.LayoutParams(dp(20), dp(20)));
            row.addView(waitWell, new LinearLayout.LayoutParams(dp(40), dp(40)));
            LinearLayout waitText = vertical();
            waitText.setPaddingRelative(dp(11), 0, 0, 0);
            TextView waitName = label(selectedDocumentName, 13.5f, textPrimary(), true);
            // Same RTL correctness as the preview header: align to the view's logical
            // start so LTR filenames sit flush with the icon side and still read LTR.
            waitName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            waitName.setMaxLines(1);
            waitName.setEllipsize(TextUtils.TruncateAt.END);
            waitText.addView(waitName, margin(0, 0, 0, 3));
            waitText.addView(label(printInProgress ? "بانتظار انتهاء المهمة الحالية" : "بانتظار المعاينة والطباعة",
                    11.5f, textSecondary(), false));
            row.addView(waitText, new LinearLayout.LayoutParams(0, -2, 1f));
            waiting.addView(row);
            queueMain.addView(waiting, margin(0, 0, 0, 16));
        } else {
            LinearLayout empty = card();
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.addView(label("لا توجد مهام منتظرة", 13.5f, textSecondary(), false),
                    margin(0, 6, 0, 6));
            queueMain.addView(empty, margin(0, 0, 0, 16));
        }

        queueSide.addView(sectionLabel("سجل الطباعة"), margin(2, 0, 0, 8));
        LinearLayout history = card();
        history.setPadding(dp(14), dp(6), dp(14), dp(6));
        ArrayList<String> lines = recentLogLines(8);
        if (lines.isEmpty()) {
            history.addView(label("لا يوجد سجل بعد", 12.5f, textSecondary(), false),
                    margin(0, 10, 0, 10));
        } else {
            for (int i = lines.size() - 1; i >= 0; i--) {
                TextView entry = label(lines.get(i), 12, textPrimary(), false);
                entry.setLineSpacing(dp(3), 1f);
                history.addView(entry, margin(2, 9, 2, 9));
                if (i > 0) addSeparator(history);
            }
        }
        queueSide.addView(history, margin(0, 0, 0, 14));
        addPair(content, queueMain, queueSide);

        TextView honesty = label("تعني «أُرسلت للطابعة» أن USB قبل البيانات؛ تحقق دائمًا من خروج الورق فعليًا.",
                11, textSecondary(), false);
        honesty.setLineSpacing(dp(3), 1f);
        content.addView(honesty, margin(3, 0, 3, 8));
    }

    private void buildPreviewPage(LinearLayout content) {
        pageAppBar(content, "معاينة الطباعة");
        if (selectedDocumentUri == null) {
            LinearLayout empty = card();
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.setPadding(dp(22), dp(26), dp(22), dp(22));
            LinearLayout well = new LinearLayout(this);
            well.setGravity(Gravity.CENTER);
            well.setBackground(flat(iconWell(), 18));
            ImageView icon = new ImageView(this);
            icon.setImageResource(R.drawable.ic_doc);
            icon.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
            well.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));
            empty.addView(well, new LinearLayout.LayoutParams(dp(54), dp(54)));
            empty.addView(label("لم يُختَر مستند", 17, textPrimary(), true), margin(0, 14, 0, 6));
            empty.addView(label("اختر ملف PDF أو صورة أو مستند Office أو ملف نصي للمتابعة.",
                    12, textSecondary(), false), margin(0, 0, 0, 15));
            Button browse = bigPrimaryButton("تصفح الملفات");
            browse.setMinHeight(dp(48));
            browse.setOnClickListener(v -> { performTapFeedback(v); openDocumentPicker(false); });
            empty.addView(browse, new LinearLayout.LayoutParams(-1, dp(48)));
            content.addView(empty);
            return;
        }

        boolean supported = documentRenderer != null && documentRenderer.isSupported();
        String format = documentRenderer == null ? selectedDocumentMime : documentRenderer.getFormatLabel();
        int pages = supported ? documentRenderer.getPageCount() : 0;

        LinearLayout fileCard = card();
        LinearLayout fileHeader = horizontal();
        LinearLayout fileWell = new LinearLayout(this);
        fileWell.setGravity(Gravity.CENTER);
        fileWell.setBackground(verticalGradient(primaryColor(), primaryDeep(), 15));
        ImageView fileIcon = new ImageView(this);
        fileIcon.setImageResource(documentRenderer != null && documentRenderer.isImage()
                ? R.drawable.ic_image : R.drawable.ic_doc);
        fileIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        fileWell.addView(fileIcon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        fileHeader.addView(fileWell, new LinearLayout.LayoutParams(dp(46), dp(46)));
        LinearLayout fileInfo = vertical();
        fileInfo.setPadding(dp(11), 0, dp(4), 0);
        TextView fileTitle = label(selectedDocumentName, 13.5f, textPrimary(), true);
        // Align to the view's logical start (right in RTL) instead of the text's own
        // direction, so LTR filenames sit flush with the icon side and still read LTR.
        fileTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        fileTitle.setMaxLines(2);
        fileTitle.setEllipsize(TextUtils.TruncateAt.END);
        fileInfo.addView(fileTitle, margin(0, 0, 0, 4));
        TextView fileMeta = label(supported ? pages + " صفحة · " + format : "لا يمكن معاينة هذا التنسيق مباشرة",
                11, supported ? okText() : warnText(), true);
        fileMeta.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        fileMeta.setMaxLines(1);
        fileMeta.setEllipsize(TextUtils.TruncateAt.END);
        fileInfo.addView(fileMeta);
        fileHeader.addView(fileInfo, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView chooseAnother = label("تغيير", 12, tonalText(), true);
        chooseAnother.setPadding(dp(8), dp(8), 0, dp(8));
        chooseAnother.setOnClickListener(v -> { performTapFeedback(v); openDocumentPicker(false); });
        fileHeader.addView(chooseAnother);
        fileCard.addView(fileHeader, margin(0, 0, 0, 13));

        if (supported) {
            LinearLayout pageMeta = horizontal();
            pageMeta.addView(label("الصفحة " + (currentPreviewPage + 1) + " من " + pages,
                    10.5f, tonalText(), true), new LinearLayout.LayoutParams(0, -2, 1f));
            pageMeta.addView(label(paperSize + " · " + orientationLabel(), 10.5f, textSecondary(), true));
            fileCard.addView(pageMeta, margin(0, 0, 0, 10));

            FrameLayout paper = new FrameLayout(this);
            paper.setBackground(flat(darkMode ? 0xFF2A2A2F : 0xFFF2F3F5, 8));
            paper.setPadding(dp(9), dp(9), dp(9), dp(9));
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setAdjustViewBounds(true);
            image.setContentDescription("معاينة الصفحة " + (currentPreviewPage + 1));
            try {
                Bitmap next = documentRenderer.renderPage(currentPreviewPage, dp(315), dp(410));
                Bitmap old = currentPreviewBitmap;
                currentPreviewBitmap = next;
                image.setImageBitmap(next);
                if (old != null && old != next && !old.isRecycled()) old.recycle();
            } catch (Exception e) {
                image.setImageDrawable(null);
                append("فشل عرض المعاينة: " + e.getMessage());
            }
            paper.addView(image, new FrameLayout.LayoutParams(-1, dp(360), Gravity.CENTER));
            fileCard.addView(paper, margin(0, 0, 0, 12));

            LinearLayout pageNav = horizontal();
            Button previous = tonalButton("السابقة", 13f);
            previous.setMinHeight(dp(43));
            previous.setEnabled(currentPreviewPage > 0);
            previous.setAlpha(currentPreviewPage > 0 ? 1f : 0.5f);
            previous.setOnClickListener(v -> {
                currentPreviewPage = Math.max(0, currentPreviewPage - 1);
                renderApp();
            });
            pageNav.addView(previous, new LinearLayout.LayoutParams(0, dp(43), 1f));
            Button next = tonalButton("التالية", 13f);
            next.setMinHeight(dp(43));
            next.setEnabled(currentPreviewPage < pages - 1);
            next.setAlpha(currentPreviewPage < pages - 1 ? 1f : 0.5f);
            next.setOnClickListener(v -> {
                currentPreviewPage = Math.min(pages - 1, currentPreviewPage + 1);
                renderApp();
            });
            LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, dp(43), 1f);
            nextParams.setMarginStart(dp(9));
            pageNav.addView(next, nextParams);
            fileCard.addView(pageNav);
        } else {
            TextView message = label("يمكن تصفح هذا الملف، لكن هذا الإصدار لا يستطيع تحويله إلى معاينة طباعة. افتحه في تطبيق Office متوافق وصدّره أو شاركه كملف PDF، ثم اختر ملف PDF هنا.",
                    12, textSecondary(), false);
            message.setLineSpacing(dp(4), 1f);
            fileCard.addView(message, margin(0, 0, 0, 13));
            Button openExternal = bigPrimaryButton("فتح في تطبيق آخر");
            openExternal.setMinHeight(dp(46));
            openExternal.setOnClickListener(v -> openSelectedFileExternally());
            fileCard.addView(openExternal, new LinearLayout.LayoutParams(-1, dp(46)));
        }
        if (supported) {
            LinearLayout options = card();
            options.setPadding(dp(16), dp(5), dp(16), dp(5));
            addOptionRow(options, "حجم الورق", paperSize, this::showPaperSizeOptions);
            addSeparator(options);
            addOptionRow(options, "عدد النسخ", String.valueOf(copies), this::showCopiesOptions);
            addSeparator(options);
            addOptionRow(options, "الصفحات", pageRangeSummaryAr(), this::showPageRangeOptions);
            addSeparator(options);
            addOptionRow(options, "الطباعة على الوجهين", duplexLabel(), this::showDuplexOptions);
            addSeparator(options);
            addOptionRow(options, "اتجاه الصفحة", orientationLabel(), this::showOrientationOptions);
            addSeparator(options);
            addOptionRow(options, "القياس", scaleLabel(), this::showScaleOptions);
            addSeparator(options);
            addOptionRow(options, "جودة الطباعة", qualityLabel(), this::showQualityOptions);

            Button print = bigPrimaryButton(printInProgress ? "جارٍ الإرسال…" : "طباعة");
            print.setEnabled(!printInProgress);
            print.setAlpha(printInProgress ? 0.6f : 1f);
            print.setOnClickListener(v -> { performTapFeedback(v); printSelectedDocument(); });

            TextView note = label("طابعة HP LaserJet P1005 أحادية اللون؛ تُحوَّل المستندات والصور الملوّنة إلى إخراج أبيض وأسود.",
                    11, textSecondary(), false);
            note.setLineSpacing(dp(3), 1f);
            LinearLayout previewSide = vertical();
            previewSide.addView(options, margin(0, 0, 0, 13));
            previewSide.addView(print, new LinearLayout.LayoutParams(-1, dp(56)));
            previewSide.addView(note, margin(3, 12, 3, 4));
            addPair(content, fileCard, previewSide);
        } else {
            content.addView(fileCard, margin(0, 0, 0, 13));
        }
    }

    private String duplexLabel() {
        if ("manual-long".equals(duplexMode)) return "يدوي · حافة طويلة";
        if ("manual-short".equals(duplexMode)) return "يدوي · حافة قصيرة";
        return "إيقاف";
    }

    private String orientationLabel() {
        return "landscape".equals(pageOrientation) ? "أفقي" : "عمودي";
    }

    private String scaleLabel() {
        return fitToPage ? "ملاءمة الصفحة" : "ملء الصفحة";
    }

    private String qualityLabel() {
        return draftMode ? "مسودة" : (printDensity >= 4 ? "كثافة عالية" : "قياسية");
    }

    private String pageRangeSummaryAr() {
        return pageRange == null || pageRange.trim().isEmpty() || "all".equalsIgnoreCase(pageRange.trim())
                ? "الكل" : pageRange.trim();
    }

    private void addOptionRow(LinearLayout parent, String title, String value, Runnable action) {
        addOptionRow(parent, title, value, action, true);
    }

    private void addOptionRow(LinearLayout parent, String title, String value, Runnable action, boolean showChevron) {
        LinearLayout row = horizontal();
        row.setPadding(0, dp(13), 0, dp(13));
        row.addView(label(title, 13.5f, textPrimary(), true),
                new LinearLayout.LayoutParams(0, -2, 1f));
        TextView valueText = label(value, 12.5f, textSecondary(), false);
        valueText.setMaxLines(1);
        row.addView(valueText);
        if (showChevron) {
            ImageView chevron = new ImageView(this);
            chevron.setImageResource(R.drawable.ic_chevron_left);
            chevron.setColorFilter(textSecondary(), PorterDuff.Mode.SRC_IN);
            LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(dp(18), dp(18));
            chevronParams.setMarginStart(dp(6));
            row.addView(chevron, chevronParams);
        }
        row.setClickable(true);
        row.setFocusable(true);
        row.setHapticFeedbackEnabled(hapticsEnabled);
        row.setOnClickListener(v -> { performTapFeedback(v); action.run(); });
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private interface IndexAction { void onChosen(int index); }

    // One UI choice dialog (reviewed proposal): app-owned colors so dark mode is
    // legible, radios at the start side, and a single centered إلغاء at the bottom.
    private AlertDialog.Builder dialogBuilder() {
        return new AlertDialog.Builder(new android.view.ContextThemeWrapper(this,
                darkMode ? android.R.style.Theme_Material_NoActionBar
                        : android.R.style.Theme_Material_Light_NoActionBar));
    }

    private void showSelectionDialog(String title, String[] options, int checked, IndexAction action) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        LinearLayout sheet = vertical();
        sheet.setBackground(flat(darkMode ? 0xFF24262B : 0xFFFFFFFF, 28));
        sheet.setPadding(dp(8), dp(20), dp(8), dp(8));
        TextView heading = label(title, 17.5f, darkMode ? 0xFFF2F2F4 : 0xFF1B1B1B, true);
        sheet.addView(heading, margin(18, 0, 18, 6));
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            LinearLayout row = horizontal();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(18), dp(12), dp(18), dp(12));
            row.addView(radioDot(index == checked));
            TextView optionLabel = label(options[i], 14.5f, darkMode ? 0xFFF2F2F4 : 0xFF1B1B1B, false);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, -2, 1f);
            // Start margin, not absolute left: an absolute left margin is on the wrong
            // side in RTL and left the label flush against the radio ring (real-device
            // overlap in duplex/source/scaling dialogs).
            labelParams.setMarginStart(dp(14));
            row.addView(optionLabel, labelParams);
            row.setClickable(true);
            row.setFocusable(true);
            row.setHapticFeedbackEnabled(hapticsEnabled);
            row.setOnClickListener(v -> {
                performTapFeedback(v);
                action.onChosen(index);
                dialog.dismiss();
                renderApp();
            });
            sheet.addView(row);
        }
        TextView cancel = label("إلغاء", 14, darkMode ? 0xFF4C9AFF : 0xFF0072DE, true);
        cancel.setGravity(Gravity.CENTER);
        cancel.setPadding(dp(10), dp(10), dp(10), dp(10));
        LinearLayout cancelRow = horizontal();
        cancelRow.setGravity(Gravity.CENTER);
        cancelRow.addView(cancel);
        cancelRow.setClickable(true);
        cancelRow.setFocusable(true);
        cancelRow.setHapticFeedbackEnabled(hapticsEnabled);
        cancelRow.setOnClickListener(v -> { performTapFeedback(v); dialog.dismiss(); });
        sheet.addView(cancelRow, margin(0, 6, 0, 4));
        dialog.setContentView(sheet);
        dialog.setOnShowListener(ignored -> styleDialogWindow(dialog));
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dp(330), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    // Radio indicator matching the reviewed proposal: 22dp ring, filled center when
    // selected; blue accent in both modes.
    private View radioDot(boolean selected) {
        FrameLayout ring = new FrameLayout(this);
        int accent = darkMode ? 0xFF4C9AFF : 0xFF0072DE;
        GradientDrawable outer = new GradientDrawable();
        outer.setShape(GradientDrawable.OVAL);
        outer.setColor(Color.TRANSPARENT);
        outer.setStroke(dp(2), selected ? accent : 0xFF9A9AA2);
        ring.setBackground(outer);
        if (selected) {
            View dot = new View(this);
            GradientDrawable inner = new GradientDrawable();
            inner.setShape(GradientDrawable.OVAL);
            inner.setColor(accent);
            dot.setBackground(inner);
            ring.addView(dot, new FrameLayout.LayoutParams(dp(11), dp(11), Gravity.CENTER));
        }
        ring.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
        return ring;
    }

    // Shared window styling for app dialogs: pinned RTL decor, dim, app-owned
    // background, and window blur behind on API 31+ when enabled.
    private void styleDialogWindow(android.app.Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) return;
        window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.18f);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // No blur-behind on modal dialogs: user direction (tablet showed a fully
        // blurred backdrop while his phone did not - he wants the phone behavior
        // everywhere). Dim only. The approved bottom-nav capsule keeps its own
        // in-bounds background blur in showCapsuleNav().
    }

    private void showPaperSizeOptions() {
        String[] options = {"A4 (210 × 297 مم)", "A5 (148 × 210 مم)", "Letter (8.5 × 11 بوصة)",
                "Legal (8.5 × 14 بوصة)", "B5 (176 × 250 مم)"};
        int selected = Math.max(0, Arrays.asList(paperSizes).indexOf(paperSize));
        showSelectionDialog("حجم الورق", options, selected, which -> {
            paperSize = paperSizes[which];
            printPreferences.edit().putString("paper_size", paperSize).apply();
        });
    }

    // One UI dialog chrome shared by the print-setting sheets (copies, page
    // range): same rounded sheet family as showSelectionDialog, title +
    // subtitle at the start side, and a two-button row - تطبيق filled,
    // إلغاء tonal, equal halves.
    private android.app.Dialog oneUIFormDialog(String title, String subtitle, View body,
                                               Runnable onApply) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        LinearLayout sheet = vertical();
        sheet.setBackground(flat(darkMode ? 0xFF24262B : 0xFFFFFFFF, 28));
        sheet.setPadding(dp(8), dp(20), dp(8), dp(8));
        TextView heading = label(title, 17.5f, darkMode ? 0xFFF2F2F4 : 0xFF1B1B1B, true);
        sheet.addView(heading, margin(18, 0, 18, subtitle != null ? 2 : 8));
        if (subtitle != null) {
            TextView sub = label(subtitle, 12.5f, textSecondary(), false);
            sheet.addView(sub, margin(18, 0, 18, 8));
        }
        sheet.addView(body, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout buttons = horizontal();
        TextView apply = dialogButton("تطبيق", true);
        TextView cancel = dialogButton("إلغاء", false);
        apply.setOnClickListener(v -> { performTapFeedback(v); dialog.dismiss(); if (onApply != null) onApply.run(); });
        cancel.setOnClickListener(v -> { performTapFeedback(v); dialog.dismiss(); });
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, dp(44), 1f);
        LinearLayout.LayoutParams halfEnd = new LinearLayout.LayoutParams(0, dp(44), 1f);
        halfEnd.setMarginStart(dp(8));
        buttons.addView(apply, half);
        buttons.addView(cancel, halfEnd);
        sheet.addView(buttons, margin(10, 12, 10, 4));
        dialog.setContentView(sheet);
        dialog.setOnShowListener(ignored -> styleDialogWindow(dialog));
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dp(330), WindowManager.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    private TextView dialogButton(String text, boolean primary) {
        TextView button = label(text, 14, primary ? 0xFFFFFFFF : (darkMode ? 0xFFF2F2F4 : 0xFF1B1B1B), true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(flat(primary ? (darkMode ? 0xFF2E6DB4 : 0xFF0072DE)
                : (darkMode ? 0xFF3A3C42 : 0xFFE9E9EE), 22));
        button.setClickable(true);
        button.setFocusable(true);
        button.setHapticFeedbackEnabled(hapticsEnabled);
        return button;
    }

    // The native wheel keeps LIGHT-theme colors (near-black digits and
    // dividers) even when the sheet behind it is dark, reproducing his
    // original unreadable-dark-dialog failure (review of gate run 40254680016
    // capture 08d2). Tint digits via the public API-29 setter (wheel paint +
    // selected EditText); older APIs and the divider drawable need
    // best-effort field access - safe here because targetSdk 28 keeps those
    // greylist members reflectable.
    private void styleNumberPickerForSheet(NumberPicker picker) {
        int digits = textPrimary();
        if (Build.VERSION.SDK_INT >= 29) {
            picker.setTextColor(digits);
        } else {
            try {
                java.lang.reflect.Field paint = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
                paint.setAccessible(true);
                ((android.graphics.Paint) paint.get(picker)).setColor(digits);
            } catch (Exception ignored) {}
            for (int i = 0; i < picker.getChildCount(); i++) {
                View child = picker.getChildAt(i);
                if (child instanceof EditText) ((EditText) child).setTextColor(digits);
            }
        }
        try {
            java.lang.reflect.Field divider = NumberPicker.class.getDeclaredField("mSelectionDivider");
            divider.setAccessible(true);
            GradientDrawable line = new GradientDrawable();
            line.setColor(darkMode ? 0xFF50535C : 0xFFDCDCE4);
            line.setSize(-1, dp(1));
            divider.set(picker, line);
        } catch (Exception ignored) {}
    }

    private void showCopiesOptions() {
        LinearLayout container = vertical();
        container.setPadding(dp(14), dp(4), dp(14), dp(0));
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(99);
        picker.setValue(Math.max(1, Math.min(copies, 99)));
        styleNumberPickerForSheet(picker);
        container.addView(picker, new LinearLayout.LayoutParams(-1, dp(132)));
        oneUIFormDialog("عدد النسخ", "من 1 إلى 99 نسخة", container, () -> {
            copies = picker.getValue();
            printPreferences.edit().putInt("copies", copies).apply();
            renderApp();
        });
    }

    private void showPageRangeOptions() {
        if (documentRenderer == null || !documentRenderer.isSupported()) {
            systemToast("اختر مستندًا قابلاً للمعاينة أولاً.", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout box = vertical();
        box.setPadding(dp(14), dp(0), dp(14), dp(0));
        // The range example stays LTR inside the RTL sentence: without bidi
        // isolates the numbers and dashes scatter across the line (his device).
        TextView help = label("استخدم كل الصفحات أو اكتب نطاقًا مثل \u20661-3,5,8-10\u2069. تبدأ أرقام الصفحات من 1.",
                12, textSecondary(), false);
        help.setLineSpacing(dp(3), 1f);
        box.addView(help, margin(4, 0, 4, 10));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(15);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setHint("اتركه فارغًا لكل الصفحات");
        input.setHintTextColor(textSecondary());
        input.setTextColor(textPrimary());
        input.setBackground(flat(darkMode ? 0xFF313339 : 0xFFF0F0F4, 14));
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setText("all".equalsIgnoreCase(pageRange) ? "" : pageRange);
        input.setSelection(input.getText().length());
        box.addView(input, new LinearLayout.LayoutParams(-1, dp(52)));
        TextView useAll = label("استخدام كل الصفحات", 13.5f, darkMode ? 0xFF4C9AFF : 0xFF0072DE, true);
        useAll.setPadding(dp(4), dp(10), dp(4), dp(2));
        box.addView(useAll, new LinearLayout.LayoutParams(-2, -2));
        final android.app.Dialog[] holder = new android.app.Dialog[1];
        Runnable apply = () -> {
            String requested = input.getText().toString().trim();
            if (requested.isEmpty()) requested = "all";
            // Same acceptance rules as before the restyle: "all" or the raw
            // range with whitespace stripped; the renderer parses it downstream.
            pageRange = requested.equalsIgnoreCase("all") ? "all" : requested.replaceAll("\\s+", "");
            printPreferences.edit().putString("page_range", pageRange).apply();
            renderApp();
        };
        holder[0] = oneUIFormDialog("الصفحات المطلوب طباعتها", null, box, apply);
        useAll.setOnClickListener(v -> {
            performTapFeedback(v);
            pageRange = "all";
            printPreferences.edit().putString("page_range", pageRange).apply();
            holder[0].dismiss();
            renderApp();
        });
    }

    private void showDuplexOptions() {
        String[] options = {
                "على وجه واحد",
                "يدويًا · قلب على الحافة الطويلة",
                "يدويًا · قلب على الحافة القصيرة"
        };
        int selected = "manual-long".equals(duplexMode) ? 1 : "manual-short".equals(duplexMode) ? 2 : 0;
        showSelectionDialog("الطباعة على الوجهين", options, selected, which -> {
            duplexMode = which == 1 ? "manual-long" : which == 2 ? "manual-short" : "off";
            printPreferences.edit().putString("duplex_mode", duplexMode).apply();
        });
    }

    private void showOrientationOptions() {
        String[] options = {"عمودي", "أفقي"};
        int selected = "landscape".equals(pageOrientation) ? 1 : 0;
        showSelectionDialog("اتجاه الصفحة", options, selected, which -> {
            pageOrientation = which == 1 ? "landscape" : "portrait";
            printPreferences.edit().putString("orientation", pageOrientation).apply();
        });
    }

    private void showScaleOptions() {
        String[] options = {"ملاءمة الصفحة القابلة للطباعة", "ملء الصفحة"};
        showSelectionDialog("القياس", options, fitToPage ? 0 : 1, which -> {
            fitToPage = which == 0;
            printPreferences.edit().putBoolean("fit_to_page", fitToPage).apply();
        });
    }

    private void showQualityOptions() {
        String[] options = {"مسودة · توفير الحبر", "جودة قياسية", "كثافة عالية"};
        int selected = draftMode ? 0 : printDensity >= 4 ? 2 : 1;
        showSelectionDialog("جودة الطباعة", options, selected, which -> {
            draftMode = which == 0;
            printDensity = which == 0 ? 2 : which == 1 ? 3 : 5;
            printPreferences.edit().putBoolean("draft_mode", draftMode)
                    .putInt("density", printDensity).apply();
        });
    }

    private void showThemeOptions() {
        String[] options = {"حسب النظام", "فاتح", "داكن"};
        int selected = "light".equals(themeMode) ? 1 : "dark".equals(themeMode) ? 2 : 0;
        showSelectionDialog("المظهر", options, selected, which -> {
            themeMode = which == 1 ? "light" : which == 2 ? "dark" : "system";
            uiPreferences.edit().putString("theme_mode", themeMode).apply();
            applyThemeMode();
            renderApp();
        });
    }

    private void openSelectedFileExternally() {
        if (selectedDocumentUri == null) return;
        try {
            Intent view = new Intent(Intent.ACTION_VIEW);
            view.setDataAndType(selectedDocumentUri,
                    selectedDocumentMime == null || selectedDocumentMime.isEmpty() ? "*/*" : selectedDocumentMime);
            view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(view, "فتح المستند بواسطة"));
        } catch (Exception e) {
            systemToast("لم يُعثر على تطبيق متوافق. صدّر هذا الملف كملف PDF.", Toast.LENGTH_LONG).show();
        }
    }

    private void buildSetupPage(LinearLayout content) {
        pageAppBar(content, "إعداد الطابعة");
        boolean connected = hasPrinterConnected();

        LinearLayout hero = card();
        LinearLayout heroRow = horizontal();
        heroRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout htext = vertical();
        htext.addView(label("HP LaserJet P1005", 16.5f, textPrimary(), true));
        htext.addView(label(connected ? "متصلة عبر USB · تحتاج تهيئة بعد كل تشغيل"
                : "غير متصلة · وصّل الطابعة بمحوّل USB OTG", 12, textSecondary(), false),
                margin(0, 3, 0, 0));
        heroRow.addView(htext, new LinearLayout.LayoutParams(0, -2, 1f));
        ImageView photo = new ImageView(this);
        photo.setImageResource(R.drawable.p1005_photo);
        photo.setAdjustViewBounds(true);
        photo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        photo.setContentDescription("طابعة HP LaserJet P1005");
        LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(dp(118), dp(104));
        photoParams.setMargins(0, 0, dp(14), 0);
        heroRow.addView(photo, photoParams);
        hero.addView(heroRow);
        content.addView(hero, margin(0, 0, 0, 16));

        if (setupDone) {
            LinearLayout success = vertical();
            success.setGravity(Gravity.CENTER_HORIZONTAL);
            success.setPadding(dp(20), dp(26), dp(20), dp(20));
            success.setBackground(flat(okBg(), 26));
            TextView check = label("✓", 30, Color.WHITE, true);
            check.setGravity(Gravity.CENTER);
            check.setBackground(flat(okText(), 30));
            success.addView(check, new LinearLayout.LayoutParams(dp(60), dp(60)));
            success.addView(label("تم بنجاح تعريف الطابعة", 18, darkMode ? textPrimary() : 0xFF0E5A34, true),
                    margin(0, 16, 0, 6));
            TextView sub = label("اكتملت التهيئة وتم التحقق من البرنامج الثابت.\nطابعتك جاهزة للطباعة.",
                    12.5f, darkMode ? textSecondary() : 0xFF1F6B43, false);
            sub.setGravity(Gravity.CENTER);
            sub.setLineSpacing(dp(3), 1f);
            success.addView(sub);
            content.addView(success, margin(0, 0, 0, 14));

            LinearLayout steps = card();
            steps.setPadding(dp(16), dp(8), dp(16), dp(8));
            addSetupResultRow(steps, "تهيئة الطابعة", "اكتملت");
            addSeparator(steps);
            addSetupResultRow(steps, "التحقق من السوفتوير", "الإصدار مثبّت ومقروء من الطابعة");
            addSeparator(steps);
            LinearLayout testRow = horizontal();
            testRow.setPadding(0, dp(12), 0, dp(12));
            LinearLayout testLabels = vertical();
            testLabels.addView(label("طباعة صفحة تجريبية", 14, textPrimary(), true), margin(0, 0, 0, 4));
            testLabels.addView(label("خطوة اختيارية للتأكد من خروج الورق فعليًا", 11.5f, textSecondary(), false));
            testRow.addView(testLabels, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView testLink = label("طباعة", 13, tonalText(), true);
            testLink.setPadding(dp(10), dp(8), 0, dp(8));
            testLink.setOnClickListener(v -> { performTapFeedback(v); printBuiltInTestPage(); });
            testRow.addView(testLink);
            steps.addView(testRow);
            content.addView(steps, margin(0, 0, 0, 14));

            Button home = bigPrimaryButton("العودة إلى الواجهة الرئيسية");
            home.setOnClickListener(v -> { performTapFeedback(v); openPage("home"); });
            content.addView(home, new LinearLayout.LayoutParams(-1, dp(56)));
            return;
        }

        if (setupFinishedWithWarning) {
            LinearLayout warning = vertical();
            warning.setGravity(Gravity.CENTER_HORIZONTAL);
            warning.setPadding(dp(20), dp(20), dp(20), dp(18));
            warning.setBackground(flat(warnBg(), 26));
            warning.addView(label("تعذّر تأكيد التعريف", 17, warnText(), true), margin(0, 0, 0, 6));
            TextView sub = label("أُرسلت بيانات البرنامج الثابت، لكن الطابعة لم تُرجع إصدارًا مؤكدًا. قبول USB للبيانات لا يثبت نجاح التحميل؛ لا تُكرر الرفع كثيرًا.",
                    12, textSecondary(), false);
            sub.setGravity(Gravity.CENTER);
            sub.setLineSpacing(dp(3), 1f);
            warning.addView(sub, margin(0, 0, 0, 0));
            content.addView(warning, margin(0, 0, 0, 14));
        }

        content.addView(sectionLabel("خطوات التهيئة"), margin(2, 0, 0, 8));
        LinearLayout steps = card();
        steps.setPadding(dp(14), dp(8), dp(14), dp(8));
        addSetupStepRow(steps, 0, "1", "تهيئة الطابعة", "تحميل البرنامج الثابت إلى ذاكرة الطابعة");
        addSeparator(steps);
        addSetupStepRow(steps, 1, "2", "التحقق من السوفتوير", "قراءة إصدار البرنامج الثابت من الطابعة للتأكد");
        addSeparator(steps);
        addSetupStepRow(steps, 2, "3", "طباعة صفحة تجريبية", "تأكد أن الصفحة تخرج من الطابعة فعليًا");
        content.addView(steps, margin(0, 0, 0, 16));

        Button start = bigPrimaryButton(setupRunning ? "جارٍ التهيئة…" : "تهيئة الطابعة الآن");
        start.setEnabled(!setupRunning);
        start.setAlpha(setupRunning ? 0.6f : 1f);
        start.setOnClickListener(v -> { performTapFeedback(v); startSetupFlow(); });
        content.addView(start, new LinearLayout.LayoutParams(-1, dp(56)));
    }

    private void addSetupStepRow(LinearLayout parent, int stepIndex, String number,
                                 String title, String desc) {
        LinearLayout row = horizontal();
        row.setPadding(dp(2), dp(11), dp(2), dp(11));
        int state = setupSteps[stepIndex];
        TextView badge;
        if (state == 2) {
            badge = label("✓", 15, Color.WHITE, true);
            badge.setBackground(flat(okText(), 18));
        } else if (state == 1) {
            badge = label("…", 15, tonalText(), true);
            badge.setBackground(flat(tonalBg(), 18));
        } else if (state == 3 || state == 4) {
            badge = label("!", 15, Color.WHITE, true);
            badge.setBackground(flat(state == 3 ? warnText() : 0xFFC24040, 18));
        } else {
            badge = label(number, 13, tonalText(), true);
            badge.setBackground(flat(tonalBg(), 18));
        }
        badge.setGravity(Gravity.CENTER);
        row.addView(badge, new LinearLayout.LayoutParams(dp(36), dp(36)));
        LinearLayout labels = vertical();
        labels.setPadding(dp(12), 0, dp(4), 0);
        labels.addView(label(title, 14, textPrimary(), true), margin(0, 0, 0, 3));
        TextView detail = label(desc, 11.5f, textSecondary(), false);
        detail.setLineSpacing(dp(2), 1f);
        labels.addView(detail);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        if (state == 1) row.addView(label("جارٍ…", 11.5f, tonalText(), true));
        else if (state == 2) row.addView(label("اكتملت", 11.5f, okText(), true));
        else if (state == 3) row.addView(label("غير مؤكد", 11.5f, warnText(), true));
        else if (state == 4) row.addView(label("تعذّر", 11.5f, 0xFFC24040, true));
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addSetupResultRow(LinearLayout parent, String title, String result) {
        LinearLayout row = horizontal();
        row.setPadding(dp(2), dp(12), dp(2), dp(12));
        TextView check = label("✓", 13, Color.WHITE, true);
        check.setGravity(Gravity.CENTER);
        check.setBackground(flat(okText(), 16));
        row.addView(check, new LinearLayout.LayoutParams(dp(32), dp(32)));
        LinearLayout labels = vertical();
        labels.setPadding(dp(12), 0, dp(4), 0);
        labels.addView(label(title, 14, textPrimary(), true));
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(label(result, 11.5f, okText(), true));
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void startSetupFlow() {
        if (setupRunning) return;
        UsbDevice printer = findConnectedPrinter();
        if (printer == null) {
            append("توقّفت التهيئة: لم يُعثر على طابعة HP P1005 متصلة عبر USB.");
            systemToast("صِل الطابعة عبر USB OTG أولاً.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!usbManager.hasPermission(printer)) {
            append("توقّفت التهيئة: إذن USB غير ممنوح بعد.");
            systemToast("امنح إذن USB من نافذة النظام ثم أعد المحاولة.", Toast.LENGTH_LONG).show();
            scanDevices();
            return;
        }
        setupRunning = true;
        setupDone = false;
        setupFinishedWithWarning = false;
        setupSteps[0] = 1;
        setupSteps[1] = 0;
        setupSteps[2] = 0;
        renderApp();
        append("بدأت تهيئة الطابعة: إرسال البرنامج الثابت المدمج.");
        loadBuiltInFirmware((firmwareOk, firmwareDetail) -> {
            if (!firmwareOk) {
                setupSteps[0] = 4;
                setupRunning = false;
                setupFinishedWithWarning = true;
                append("فشلت تهيئة الطابعة أثناء إرسال البرنامج الثابت: " + firmwareDetail);
                renderApp();
                return;
            }
            setupSteps[0] = 2;
            setupSteps[1] = 1;
            renderApp();
            verifyPrinterFirmware((firmwareVerified, verifyDetail) -> {
                setupSteps[1] = firmwareVerified ? 2 : 3;
                setupRunning = false;
                if (firmwareVerified) {
                    setupDone = true;
                    setupFinishedWithWarning = false;
                    append("تم التحقق من البرنامج الثابت في جلسة التشغيل الحالية.");
                } else {
                    setupFinishedWithWarning = true;
                    append("اكتمل إرسال البرنامج الثابت لكن التحقق كان غير مؤكد: " + verifyDetail);
                }
                renderApp();
            });
        });
    }

    private void buildSettingsPage(LinearLayout content) {
        pageAppBar(content, "الإعدادات");

        LinearLayout general = card();
        general.setPadding(dp(16), dp(5), dp(16), dp(5));
        addOptionRow(general, "اللغة", "العربية", () ->
                systemToast("العربية هي لغة التطبيق الحالية.", Toast.LENGTH_SHORT).show(), false);
        addSeparator(general);
        addOptionRow(general, "المظهر", themeModeLabel(), this::showThemeOptions);
        LinearLayout labelPrinter = sectionLabel("الطابعة");
        LinearLayout printer = card();
        printer.setPadding(dp(16), dp(5), dp(16), dp(5));
        LinearLayout setupRow = horizontal();
        setupRow.setPadding(0, dp(13), 0, dp(13));
        LinearLayout setupLabels = vertical();
        setupLabels.addView(label("تهيئة الطابعة", 14, textPrimary(), true), margin(0, 0, 0, 4));
        setupLabels.addView(label("إعداد الطابعة والسوفتوير وصفحة تجريبية", 11.5f, textSecondary(), false));
        setupRow.addView(setupLabels, new LinearLayout.LayoutParams(0, -2, 1f));
        ImageView setupChevron = new ImageView(this);
        setupChevron.setImageResource(R.drawable.ic_chevron_left);
        setupChevron.setColorFilter(textSecondary(), PorterDuff.Mode.SRC_IN);
        setupRow.addView(setupChevron, new LinearLayout.LayoutParams(dp(18), dp(18)));
        setupRow.setClickable(true);
        setupRow.setFocusable(true);
        setupRow.setHapticFeedbackEnabled(hapticsEnabled);
        setupRow.setOnClickListener(v -> { performTapFeedback(v); openPage("setup"); });
        printer.addView(setupRow);
        LinearLayout labelDefaults = sectionLabel("تفضيلات الطباعة");
        LinearLayout defaults = card();
        defaults.setPadding(dp(16), dp(5), dp(16), dp(5));
        addOptionRow(defaults, "حجم الورق", paperSize, this::showPaperSizeOptions);
        addSeparator(defaults);
        addOptionRow(defaults, "عدد النسخ", copies + " نسخة", this::showCopiesOptions);
        addSeparator(defaults);
        addOptionRow(defaults, "الصفحات", pageRangeSummaryAr(), this::showPageRangeOptions);
        addSeparator(defaults);
        addOptionRow(defaults, "الطباعة على الوجهين", duplexLabel(), this::showDuplexOptions);
        addSeparator(defaults);
        addOptionRow(defaults, "اتجاه الصفحة", orientationLabel(), this::showOrientationOptions);
        addSeparator(defaults);
        addOptionRow(defaults, "القياس", scaleLabel(), this::showScaleOptions);
        addSeparator(defaults);
        addOptionRow(defaults, "جودة الطباعة", qualityLabel(), this::showQualityOptions);
        LinearLayout labelBehavior = sectionLabel("السلوك");
        LinearLayout behavior = card();
        behavior.setPadding(dp(16), dp(5), dp(16), dp(5));
        addSettingToggle(behavior, "ردود الفعل اللمسية", "تفعيل استجابة اللمس في عناصر التحكم.",
                hapticsEnabled, enabled -> {
                    hapticsEnabled = enabled;
                    uiPreferences.edit().putBoolean("haptics", enabled).apply();
                    renderApp();
                });
        addSeparator(behavior);
        addSettingToggle(behavior, "إبقاء الشاشة نشطة", "منع الشاشة من السكون أثناء فتح التطبيق.",
                keepScreenAwake, enabled -> {
                    keepScreenAwake = enabled;
                    uiPreferences.edit().putBoolean("keep_awake", enabled).apply();
                    renderApp();
                });
        addSeparator(behavior);
        addSettingToggle(behavior, "تأكيد تحميل البرنامج الثابت", "إظهار تأكيد أمان قبل إرسال بيانات البرنامج الثابت.",
                confirmFirmwareLoad, enabled -> {
                    confirmFirmwareLoad = enabled;
                    uiPreferences.edit().putBoolean("confirm_firmware", enabled).apply();
                    renderApp();
                });
        LinearLayout labelTools = sectionLabel("صيانة الطابعة");
        LinearLayout tools = card();
        tools.setPadding(dp(16), dp(6), dp(16), dp(6));
        addToolRow(tools, "التحقق من البرنامج الثابت", "قراءة معرّف جهاز USB والبحث عن FWVER", "فحص",
                this::verifyPrinterFirmware);
        addSeparator(tools);
        addToolRow(tools, "تحميل البرنامج الثابت المدمج", "يُستخدم بعد تشغيل الطابعة عند الحاجة", "تحميل",
                this::requestFirmwareLoad);
        addSeparator(tools);
        addToolRow(tools, "اختيار ملف برنامج ثابت", "اختيار ملف .dl للفحص دون نقله", "فتح",
                this::selectFirmwareFile);
        // tools card assembled below

        TextView firmwareNote = label("مهم: قبول USB لبيانات البرنامج الثابت لا يثبت وحده أن الطابعة حمّلته بنجاح. تجنّب تكرار الرفع إذا كانت الحالة غير مؤكدة.",
                11.5f, textSecondary(), false);
        firmwareNote.setLineSpacing(dp(4), 1f);
        LinearLayout labelAbout = sectionLabel("حول");
        LinearLayout about = card();
        about.setPadding(dp(16), dp(5), dp(16), dp(5));
        addOptionRow(about, "سجل التشخيص", "USB والطباعة", () -> openPage("activity"));
        addSeparator(about);
        addOptionRow(about, "المساعدة واستكشاف الأخطاء", "", () -> openPage("help"));
        addSeparator(about);
        addOptionRow(about, "عن التطبيق", "", () -> openPage("about"));
        TextView credit = label("brought to you by Instinct", 11.5f, textSecondary(), false);
        credit.setGravity(Gravity.CENTER);

        if (!wideLayout()) {
            content.addView(general, margin(0, 0, 0, 14));
            content.addView(labelPrinter, margin(2, 0, 0, 8));
            content.addView(printer, margin(0, 0, 0, 14));
            content.addView(labelDefaults, margin(2, 0, 0, 8));
            content.addView(defaults, margin(0, 0, 0, 14));
            content.addView(labelBehavior, margin(2, 0, 0, 8));
            content.addView(behavior, margin(0, 0, 0, 14));
            content.addView(labelTools, margin(2, 0, 0, 9));
            content.addView(tools, margin(0, 0, 0, 8));
            content.addView(firmwareNote, margin(3, 2, 3, 14));
            content.addView(labelAbout, margin(2, 0, 0, 8));
            content.addView(about, margin(0, 0, 0, 18));
            content.addView(credit, margin(0, 4, 0, 10));
            return;
        }
        // Tablet: two balanced columns instead of one stretched phone list
        // (his tablet direction: real use of the big screen, coherent layout).
        // Right column = general + print defaults; left = printer setup +
        // behavior + maintenance. حول stays full-width beneath the pair.
        LinearLayout colA = vertical();
        colA.addView(general, margin(0, 0, 0, 14));
        colA.addView(labelDefaults, margin(2, 0, 0, 8));
        colA.addView(defaults, margin(0, 0, 0, 14));
        LinearLayout colB = vertical();
        colB.addView(labelPrinter, margin(2, 0, 0, 8));
        colB.addView(printer, margin(0, 0, 0, 14));
        colB.addView(labelBehavior, margin(2, 0, 0, 8));
        colB.addView(behavior, margin(0, 0, 0, 14));
        colB.addView(labelTools, margin(2, 0, 0, 9));
        colB.addView(tools, margin(0, 0, 0, 8));
        colB.addView(firmwareNote, margin(3, 2, 3, 14));
        addPair(content, colA, colB);
        content.addView(labelAbout, margin(2, 0, 0, 8));
        content.addView(about, margin(0, 0, 0, 18));
        content.addView(credit, margin(0, 4, 0, 10));
    }

    private void addSettingToggle(LinearLayout parent, String title, String subtitle,
                                  boolean enabled, ToggleAction action) {
        LinearLayout row = horizontal();
        row.setPadding(0, dp(12), 0, dp(12));
        LinearLayout labels = vertical();
        labels.addView(label(title, 14, textPrimary(), true), margin(0, 0, 0, 4));
        TextView detail = label(subtitle, 11, textSecondary(), false);
        detail.setLineSpacing(dp(3), 1f);
        labels.addView(detail);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));

        row.addView(oneUiSwitch(enabled), new LinearLayout.LayoutParams(dp(52), dp(31)));
        row.setClickable(true);
        row.setFocusable(true);
        row.setHapticFeedbackEnabled(hapticsEnabled);
        row.setOnClickListener(v -> {
            performTapFeedback(v);
            action.onToggle(!enabled);
        });
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    // One UI style switch (reviewed proposal): track 52x31dp, knob at the start
    // side when off and the end side when on; blue checked track in both modes.
    private FrameLayout oneUiSwitch(boolean enabled) {
        FrameLayout track = new FrameLayout(this);
        track.setBackground(flat(enabled ? 0xFF0072DE : (darkMode ? 0xFF3A3A3F : 0xFFD9D9DE), 16));
        View knob = new View(this);
        knob.setBackground(flat(enabled ? Color.WHITE : (darkMode ? 0xFF8E8E96 : Color.WHITE), 13));
        FrameLayout.LayoutParams knobParams = new FrameLayout.LayoutParams(dp(25), dp(25),
                (enabled ? Gravity.END : Gravity.START) | Gravity.CENTER_VERTICAL);
        knobParams.setMargins(dp(3), 0, dp(3), 0);
        track.addView(knob, knobParams);
        return track;
    }

    private void buildAboutPage(LinearLayout content) {
        pageAppBar(content, "عن التطبيق");

        LinearLayout identity = card();
        identity.setGravity(Gravity.CENTER_HORIZONTAL);
        identity.setPadding(dp(20), dp(24), dp(20), dp(18));
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.mipmap.ic_launcher);
        mark.setContentDescription("أيقونة التطبيق");
        identity.addView(mark, new LinearLayout.LayoutParams(dp(64), dp(64)));
        identity.addView(label("HP P1005", 20, textPrimary(), true), margin(0, 12, 0, 4));
        identity.addView(label("رفيق الطباعة عبر USB", 12, textSecondary(), false), margin(0, 0, 0, 14));
        identity.addView(label("الإصدار", 10.5f, eyebrowColor(), true), margin(0, 0, 0, 3));
        identity.addView(label(readAppVersion(), 14, textPrimary(), true), margin(0, 0, 0, 12));
        identity.addView(label("معرّف التطبيق", 10.5f, eyebrowColor(), true), margin(0, 0, 0, 3));
        identity.addView(label("com.hpp1005.printer", 12.5f, textSecondary(), false));
        content.addView(identity, margin(0, 0, 0, 14));

        LinearLayout features = card();
        features.addView(label("ماذا يفعل هذا الإصدار", 16, textPrimary(), true), margin(0, 0, 0, 10));
        addBullet(features, "يكتشف طابعة HP P1005 عبر USB ويطلب إذن أندرويد.");
        addBullet(features, "يقرأ معرّف جهاز USB للتحقق من إصدار البرنامج الثابت.");
        addBullet(features, "يعاين ملفات PDF والصور الشائعة ويحوّلها إلى تدفق XQX الخاص بالطابعة.");
        addBullet(features, "يستورد النص من ملفات نصية وOffice وOpenDocument؛ التنسيقات المعقدة تُصدَّر كـ PDF.");
        addBullet(features, "يوفّر خيارات حجم الورق ونطاق الصفحات والنسخ المرتبة والاتجاه والقياس والجودة والطباعة اليدوية على الوجهين.");
        addBullet(features, "يرسل البرنامج الثابت المدمج وصفحة اختبار XQX من صفحة واحدة.");
        addBullet(features, "يسجّل نتائج عمليات USB في سجل التشخيص.");
        content.addView(features, margin(0, 0, 0, 14));

        LinearLayout limitations = card();
        limitations.addView(label("القيود الحالية", 16, textPrimary(), true), margin(0, 0, 0, 8));
        TextView body = label("تحتفظ ملفات PDF والصور الشائعة بإخراجها. تُستورَد النصوص وعدد من تنسيقات Office وOpenDocument كتخطيط نصي لا بدقة Office الكاملة؛ لمستندات Word وExcel وPowerPoint المعقدة، صدّرها كملف PDF. اختيار ملف برنامج ثابت يسجّل الاختيار فقط ولا ينقله. قبول USB لبيانات البرنامج الثابت لا يضمن نجاح تحميله.",
                12, textSecondary(), false);
        body.setLineSpacing(dp(4), 1f);
        limitations.addView(body);
        content.addView(limitations, margin(0, 0, 0, 18));

        TextView credit = label("brought to you by Instinct", 11.5f, textSecondary(), false);
        credit.setGravity(Gravity.CENTER);
        content.addView(credit, margin(0, 4, 0, 10));
    }

    private void addBullet(LinearLayout parent, String text) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.TOP);
        TextView dot = label("•", 16, primaryColor(), true);
        row.addView(dot, new LinearLayout.LayoutParams(dp(16), -2));
        TextView body = label(text, 12, textSecondary(), false);
        body.setLineSpacing(dp(3), 1f);
        row.addView(body, new LinearLayout.LayoutParams(0, -2, 1f));
        parent.addView(row, margin(0, 0, 0, 8));
    }

    private void buildHelpPage(LinearLayout content) {
        pageAppBar(content, "المساعدة واستكشاف الأخطاء");

        LinearLayout help = card();
        help.setPadding(dp(16), dp(8), dp(16), dp(8));
        addHelpItem(help, "1", "وصّل الطابعة",
                "شغّل P1005 واستخدم محوّل USB OTG يدعم نقل البيانات. كابلات الشحن فقط لا تعمل.");
        addSeparator(help);
        addHelpItem(help, "2", "امنح إذن USB",
                "افحص أجهزة USB من صفحة الطباعة ووافق على طلب إذن أندرويد إن ظهر.");
        addSeparator(help);
        addHelpItem(help, "3", "تحقق من معرّف الجهاز",
                "استخدم التحقق من البرنامج الثابت لقراءة معرّف جهاز USB. وجود حقل FWVER دليل على حالة البرنامج الثابت، لكنه ليس اختبار طباعة.");
        addSeparator(help);
        addHelpItem(help, "4", "تصفّح وعاين ثم اطبع",
                "من صفحة طباعة اختر ملفًا، راجع معاينة الصفحات، ثم اضبط الورق والنسخ والنطاق والاتجاه والقياس والجودة والطباعة على الوجهين قبل الضغط على طباعة. تُعرض ملفات PDF والصور مباشرة؛ ملفات Office المعقدة يُفضّل تصديرها كـ PDF.");
        addSeparator(help);
        addHelpItem(help, "5", "اطبع صفحة تجريبية",
                "استخدم الصفحة التجريبية المدمجة وانتظر انتهاء نقل USB. تأكد من خروج الورق فعليًا من الطابعة.");
        addSeparator(help);
        addHelpItem(help, "6", "حمّل البرنامج الثابت عند الحاجة فقط",
                "قد يلزم إعادة تحميل البرنامج الثابت بعد كل دورة تشغيل للطابعة. لا تُرسله بشكل متكرر إذا كانت الحالة غير مؤكدة.");
        content.addView(help, margin(0, 0, 0, 15));

        Button scan = tonalButton("فحص أجهزة USB", 14.5f);
        scan.setMinHeight(dp(48));
        scan.setOnClickListener(v -> { scanDevices(); renderApp(); });
        content.addView(scan, new LinearLayout.LayoutParams(-1, dp(48)));
        Button print = tonalButton("طباعة الصفحة التجريبية", 14.5f);
        print.setMinHeight(dp(48));
        print.setOnClickListener(v -> { performTapFeedback(v); printBuiltInTestPage(); });
        content.addView(print, margin(0, 10, 0, 8));
    }

    private void addHelpItem(LinearLayout parent, String number, String title, String detail) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.TOP);
        row.setPadding(dp(2), dp(11), dp(2), dp(11));
        TextView badge = label(number, 12, tonalText(), true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(flat(tonalBg(), 18));
        row.addView(badge, new LinearLayout.LayoutParams(dp(36), dp(36)));
        LinearLayout labels = vertical();
        labels.setPaddingRelative(dp(11), 0, 0, 0);
        labels.addView(label(title, 14, textPrimary(), true), margin(0, 0, 0, 4));
        TextView d = label(detail, 12, textSecondary(), false);
        d.setLineSpacing(dp(3), 1f);
        labels.addView(d);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void buildActivityPage(LinearLayout content) {
        pageAppBar(content, "سجل التشخيص");

        LinearLayout actions = horizontal();
        Button copy = tonalButton("نسخ السجل", 13f);
        copy.setMinHeight(dp(44));
        copy.setOnClickListener(v -> copyActivityLog());
        actions.addView(copy, new LinearLayout.LayoutParams(0, dp(44), 1f));
        Button clear = tonalButton("مسح", 13f);
        clear.setMinHeight(dp(44));
        clear.setOnClickListener(v -> {
            clearActivityLog();
            systemToast("مُسح سجل النشاط", Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        clearParams.setMarginStart(dp(9));
        actions.addView(clear, clearParams);
        content.addView(actions, margin(0, 0, 0, 13));

        LinearLayout logCard = card();
        LinearLayout logTop = horizontal();
        logTop.addView(label("مخرجات الجلسة", 11, eyebrowColor(), true),
                new LinearLayout.LayoutParams(0, -2, 1f));
        logTop.addView(statusPill(hasPrinterConnected()));
        logCard.addView(logTop, margin(0, 0, 0, 13));
        output = label(activityLog.toString(), 12, textPrimary(), false);
        output.setTextIsSelectable(true);
        output.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        output.setLineSpacing(dp(4), 1f);
        logCard.addView(output, new LinearLayout.LayoutParams(-1, -2));
        content.addView(logCard, margin(0, 0, 0, 16));

        Button scan = bigPrimaryButton("فحص أجهزة USB");
        scan.setOnClickListener(v -> { scanDevices(); renderApp(); });
        content.addView(scan, new LinearLayout.LayoutParams(-1, dp(52)));
    }

    private void openDocumentPicker(boolean imagesOnly) {
        try {
            Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            picker.setType(imagesOnly ? "image/*" : "*/*");
            picker.addCategory(Intent.CATEGORY_OPENABLE);
            picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            if (!imagesOnly) {
                picker.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "application/pdf", "image/*",
                        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/rtf", "text/plain", "text/csv", "text/html",
                        "application/vnd.oasis.opendocument.text", "application/vnd.oasis.opendocument.spreadsheet",
                        "application/vnd.oasis.opendocument.presentation"
                });
            }
            startActivityForResult(picker, REQUEST_DOCUMENT);
        } catch (Exception e) {
            append("تعذّر فتح منتقي ملفات أندرويد: " + e.getMessage());
            systemToast("لا يوجد منتقي ملفات متوافق.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadSelectedDocument(Uri uri) {
        if (uri == null) return;
        try {
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
                // Some shared URIs are temporary and cannot be persisted.
            }

            String name = queryDisplayName(uri);
            String mime = getContentResolver().getType(uri);
            if (mime == null || mime.trim().isEmpty()) mime = guessMimeFromName(name);
            DocumentRenderer candidate = new DocumentRenderer(this, uri, name, mime);

            if (documentRenderer != null) documentRenderer.close();
            selectedDocumentUri = uri;
            selectedDocumentName = name;
            selectedDocumentMime = mime == null ? "" : mime;
            documentRenderer = candidate;
            currentPreviewPage = 0;

            if (currentPreviewBitmap != null && !currentPreviewBitmap.isRecycled()) {
                currentPreviewBitmap.recycle();
                currentPreviewBitmap = null;
            }

            if (candidate.isSupported()) {
                append("المستند جاهز: " + name + " (" + candidate.getFormatLabel()
                        + "، " + candidate.getPageCount() + " صفحة).");
            } else {
                append("الملف المختار: " + name + ". المعاينة المباشرة تدعم PDF والصور والنصوص واستخراج نص ملفات Office وOpenDocument. قد يلزم تصدير هذا التنسيق إلى PDF.");
            }
            currentPage = "preview";
            renderApp();
        } catch (Exception e) {
            if (debuggable()) android.util.Log.w("HP1005-Share", "document load failed for " + uri, e);
            else android.util.Log.w("HP1005-Share", "document load failed: " + e.getClass().getSimpleName());
            append("تعذّر فتح المستند: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            systemToast("تعذّرت قراءة الملف المختار.", Toast.LENGTH_LONG).show();
        }
    }

    private String queryDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0) {
                    String value = cursor.getString(column);
                    if (value != null && !value.trim().isEmpty()) return value;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        String last = uri.getLastPathSegment();
        return last == null || last.isEmpty() ? "مستند محدد" : last;
    }

    private String guessMimeFromName(String name) {
        String ext = DocumentRenderer.extensionOf(name);
        switch (ext) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "webp": return "image/webp";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "txt": case "log": case "md": return "text/plain";
            case "csv": return "text/csv";
            case "html": case "htm": return "text/html";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default: return "application/octet-stream";
        }
    }

    private void printSelectedDocument() {
        if (printInProgress) return;
        if (selectedDocumentUri == null || documentRenderer == null) {
            systemToast("اختر مستندًا أولاً.", Toast.LENGTH_SHORT).show();
            openDocumentPicker(false);
            return;
        }
        if (!documentRenderer.isSupported()) {
            showNativeDialog(dialogBuilder()
                    .setTitle("المعاينة مطلوبة")
                    .setMessage("تعذّر عرض هذا الملف هنا. افتحه في تطبيق متوافق وصدّره أو شاركه كملف PDF، ثم اطبع ملف PDF من HP P1005.")
                    .setPositiveButton("فتح في تطبيق آخر", (d, w) -> openSelectedFileExternally())
                    .setNegativeButton("إلغاء", null)
                    .create());
            return;
        }

        final ArrayList<Integer> selectedPages;
        try {
            selectedPages = resolvePageRange(pageRange, documentRenderer.getPageCount());
        } catch (IllegalArgumentException e) {
            systemToast(e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        UsbDevice printer = findConnectedPrinter();
        if (printer == null) {
            append("توقّفت طباعة المستند: جهاز HP P1005 USB غير موجود. صِله وافحص أجهزة USB.");
            systemToast("صِل طابعة HP P1005 وافحص أجهزة USB أولاً.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!usbManager.hasPermission(printer)) {
            append("توقّفت طباعة المستند: إذن USB من أندرويد غير ممنوح.");
            scanDevices();
            systemToast("امنح إذن USB ثم اضغط طباعة مرة أخرى.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean hasOddPages = false;
        boolean hasEvenPages = false;
        for (int page : selectedPages) {
            if (((page + 1) & 1) == 1) hasOddPages = true;
            else hasEvenPages = true;
        }
        boolean manualDuplex = !"off".equals(duplexMode) && hasOddPages && hasEvenPages;
        if (manualDuplex && copies > 1) {
            AlertDialog dialog = dialogBuilder()
                    .setTitle("الطباعة اليدوية على الوجهين")
                    .setMessage("لا تحتوي طابعة HP LaserJet P1005 على وحدة طباعة على الوجهين تلقائية. للحصول على ترتيب صحيح للصفحات، اطبع نسخة واحدة في كل مرة. هل أضبط عدد النسخ على 1 لهذه المهمة؟")
                    .setNegativeButton("إلغاء", null)
                    .setPositiveButton("استخدام نسخة واحدة", (d, w) -> {
                        copies = 1;
                        printPreferences.edit().putInt("copies", copies).apply();
                        renderApp();
                        startDocumentPrintPass(true, false);
                    })
                    .create();
            showNativeDialog(dialog);
        } else if (manualDuplex) {
            AlertDialog dialog = dialogBuilder()
                    .setTitle("الطباعة اليدوية على الوجهين")
                    .setMessage("تطبع P1005 وجهًا واحدًا في كل مرة. ستُرسَل الصفحات الفردية أولًا. عند المطالبة، اجمع الأوراق المطبوعة وأعد إدخالها لطباعة الوجه الآخر. لن تقلب الطابعة الأوراق تلقائيًا.")
                    .setNegativeButton("إلغاء", null)
                    .setPositiveButton("طباعة الصفحات الفردية", (d, w) -> startDocumentPrintPass(true, false))
                    .create();
            showNativeDialog(dialog);
        } else {
            startDocumentPrintPass(false, false);
        }
    }

    private void startDocumentPrintPass(boolean oddPagesOnly, boolean evenPass) {
        if (printInProgress || selectedDocumentUri == null) return;
        printInProgress = true;
        jobCancelRequested = false;

        final Uri documentUri = selectedDocumentUri;
        final String documentName = selectedDocumentName;
        final String documentMime = selectedDocumentMime;
        final String requestedPaper = paperSize;
        final boolean landscape = "landscape".equals(pageOrientation);
        final String requestedDuplex = duplexMode;
        final String requestedRange = pageRange;
        final int requestedCopies = copies;
        final int density = printDensity;
        final boolean draft = draftMode;
        final boolean fit = fitToPage;

        new Thread(() -> {
            // Serialize against system print-service jobs: one printer.
            UsbPrinterIO.JOB_LOCK.lock();
            try {
            String error = null;
            boolean cancelled = false;
            int sentPages = 0;
            int totalStreams = 0;
            try (DocumentRenderer renderer = new DocumentRenderer(getApplicationContext(),
                    documentUri, documentName, documentMime)) {
                UsbDevice printer = findConnectedPrinter();
                if (printer == null) throw new IllegalStateException("انقطع اتصال جهاز HP P1005 USB.");
                if (!usbManager.hasPermission(printer)) throw new IllegalStateException("سُحب إذن USB. افحص الطابعة وحاول مجددًا.");
                int pageCount = renderer.getPageCount();

                ArrayList<Integer> selection = resolvePageRange(requestedRange, pageCount);
                ArrayList<Integer> order = new ArrayList<>();
                if (oddPagesOnly) {
                    for (int i : selection) if (((i + 1) & 1) == 1) order.add(i);
                } else if (evenPass) {
                    for (int n = selection.size() - 1; n >= 0; n--) {
                        int i = selection.get(n);
                        if (((i + 1) & 1) == 0) order.add(i);
                    }
                } else {
                    order.addAll(selection);
                }
                if (order.isEmpty()) throw new IllegalStateException("نطاق الصفحات المحدد لا يحتوي على صفحات لهذا الوجه.");

                // Process a complete page sequence for each copy to preserve collated output.
                // The native converter is invoked with one copy per page stream.
                int copyCount = (oddPagesOnly || evenPass) ? 1 : Math.max(1, requestedCopies);
                totalStreams = order.size() * copyCount;
                currentJobName = documentName;
                currentJobTotal = totalStreams;
                currentJobDone = 0;
                runOnUiThread(this::renderApp);
                for (int copy = 1; copy <= copyCount && !cancelled; copy++) {
                    for (int orderIndex = 0; orderIndex < order.size(); orderIndex++) {
                        if (jobCancelRequested) {
                            cancelled = true;
                            break;
                        }
                        int pageIndex = order.get(orderIndex);
                        Bitmap bitmap = renderer.renderPage(pageIndex, 1800, 2540);
                        Bitmap printBitmap = bitmap;
                        if (landscape) {
                            printBitmap = Foo2XqxConverter.rotate(bitmap, 90f);
                            if (printBitmap != bitmap && !bitmap.isRecycled()) bitmap.recycle();
                        }
                        Foo2XqxConverter.Result job = null;
                        try {
                            boolean rotateBack = evenPass && "manual-long".equals(requestedDuplex);
                            job = Foo2XqxConverter.convert(getApplicationContext(), printBitmap,
                                    requestedPaper, landscape, 1, density, draft, fit, rotateBack);
                            sendXqxFileToPrinter(job.xqxFile, printer);
                        } finally {
                            if (job != null && job.xqxFile.exists()) job.xqxFile.delete();
                            if (printBitmap != null && !printBitmap.isRecycled()) printBitmap.recycle();
                        }
                        sentPages++;
                        final int shownPage = pageIndex + 1;
                        final int currentCopy = copy;
                        final int doneNow = sentPages;
                        final int totalNow = totalStreams;
                        currentJobDone = sentPages;
                        runOnUiThread(() -> {
                            append("أُرسلت الصفحة " + shownPage
                                    + (copyCount > 1 ? " · نسخة " + currentCopy + "/" + copyCount : "")
                                    + " (" + doneNow + "/" + totalNow + ").");
                            if ("queue".equals(currentPage) || "preview".equals(currentPage)) renderApp();
                        });
                    }
                }
            } catch (Exception e) {
                error = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            final String failure = error;
            final boolean wasCancelled = cancelled;
            final int done = sentPages;
            final int total = totalStreams;
            runOnUiThread(() -> {
                printInProgress = false;
                currentJobName = null;
                if (wasCancelled) {
                    append("أُلغيت المهمة بعد إرسال " + done + " من " + total
                            + " صفحة. الصفحات التي قبلها USB قد تكون خرجت فعليًا من الطابعة.");
                    systemToast("أُلغيت المهمة. الصفحات المُرسلة لا يمكن سحبها.", Toast.LENGTH_LONG).show();
                    renderApp();
                } else if (failure != null) {
                    append("فشلت طباعة المستند: " + failure);
                    systemToast("فشلت الطباعة: " + failure, Toast.LENGTH_LONG).show();
                    renderApp();
                } else if (oddPagesOnly) {
                    append("اكتمل إرسال الوجه الفردي. بانتظار إعادة إدخال الورق يدويًا.");
                    renderApp();
                    AlertDialog flip = dialogBuilder()
                            .setTitle("أعد إدخال الورق")
                            .setMessage("أُرسل الوجه الأول (" + done + " صفحة). اجمع الأوراق المطبوعة مع الحفاظ على ترتيبها وأعد إدخالها في درج الورق. عند القلب على الحافة الطويلة، اقلب الرزمة كالكتاب؛ وعند القلب على الحافة القصيرة، اقلبها فوق الحافة القصيرة. جرّب اتجاه التغذية على صفحة واحدة قبل مهمة كبيرة.")
                            .setNegativeButton("التوقف هنا", (d, w) ->
                                    systemToast("طُبع الوجه الأول. لم تكتمل المهمة.", Toast.LENGTH_LONG).show())
                            .setPositiveButton("طباعة الصفحات الزوجية", (d, w) -> startDocumentPrintPass(false, true))
                            .create();
                    showNativeDialog(flip);
                } else {
                    append("اكتملت مهمة الطباعة: أُرسلت " + done + " صفحة للطابعة.");
                    systemToast(evenPass ? "أُرسل الوجه الثاني للطباعة اليدوية." : "أُرسل المستند إلى HP P1005.", Toast.LENGTH_LONG).show();
                    renderApp();
                }
            });
            } finally { UsbPrinterIO.JOB_LOCK.unlock(); }
        }, "P1005-Document-Print").start();
    }

    private UsbDevice findConnectedPrinter() {
        if (usbManager == null) return null;
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getVendorId() == 0x03F0 && device.getProductId() == 0x3D17) return device;
        }
        return null;
    }

    private void sendXqxFileToPrinter(File file, UsbDevice printer) throws Exception {
        UsbDeviceConnection connection = null;
        UsbInterface printerInterface = null;
        boolean claimed = false;
        try {
            connection = usbManager.openDevice(printer);
            if (connection == null) throw new IllegalStateException("تعذّر على أندرويد فتح طابعة USB.");
            UsbEndpoint outEndpoint = null;
            for (int i = 0; i < printer.getInterfaceCount(); i++) {
                UsbInterface candidate = printer.getInterface(i);
                if (candidate.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    printerInterface = candidate;
                    break;
                }
            }
            if (printerInterface == null) throw new IllegalStateException("لم يُعثر على واجهة من فئة الطابعات USB.");
            claimed = connection.claimInterface(printerInterface, true);
            if (!claimed) throw new IllegalStateException("تعذّر حجز واجهة الطابعة.");
            for (int i = 0; i < printerInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = printerInterface.getEndpoint(i);
                if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT
                        && endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    outEndpoint = endpoint;
                    break;
                }
            }
            if (outEndpoint == null) throw new IllegalStateException("لم يُعثر على منفذ bulk OUT.");
            byte[] buffer = new byte[16 * 1024];
            int sentTotal = 0;
            try (FileInputStream input = new FileInputStream(file)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    int offset = 0;
                    while (offset < read) {
                        int sent = connection.bulkTransfer(outEndpoint, buffer, offset,
                                read - offset, 10000);
                        if (sent <= 0) throw new IllegalStateException("توقّف نقل USB بعد " + sentTotal + " بايت (result=" + sent + ").");
                        sentTotal += sent;
                        offset += sent;
                    }
                }
            }
            append("قبل USB " + sentTotal + " بايت XQX لصفحة مستند. تأكد من خروجها فعليًا من الطابعة.");
        } finally {
            if (connection != null) {
                if (claimed && printerInterface != null) {
                    try { connection.releaseInterface(printerInterface); } catch (Exception ignored) {}
                }
                connection.close();
            }
        }
    }

    private boolean hasPrinterConnected() {
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getVendorId() == 0x03F0 && device.getProductId() == 0x3D17) return true;
        }
        return false;
    }

    private void scanDevices() {
        clearLogWithHeader("سجل تشخيص USB");
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
        append("الأجهزة المكتشفة: " + devices.size());
        if (devices.isEmpty()) {
            append("لم يُكتشف أي جهاز USB. تحقق من دعم OTG والمحوّل والكابل وتشغيل الطابعة.");
            return;
        }
        for (UsbDevice device : devices.values()) {
            append("وُجد: " + describe(device));
            if (usbManager.hasPermission(device)) {
                append("الإذن مُنح مسبقًا.");
                inspectDevice(device);
            } else {
                append("جارٍ طلب إذن USB...");
                usbManager.requestPermission(device, permissionIntent);
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DOCUMENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadSelectedDocument(data.getData());
        } else if (requestCode == 41 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            append("حُدّد ملف برنامج ثابت: " + data.getData().getLastPathSegment());
            append("تحديد فقط. تحقق أن هذا هو ملف sihpP1005.dl الصحيح قبل أي نقل.");
        }
    }

    private void verifyPrinterFirmware() {
        verifyPrinterFirmware(null);
    }

    private void verifyPrinterFirmware(UsbResultCallback callback) {
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
        UsbDevice printer = null;
        for (UsbDevice device : devices.values()) {
            if (device.getVendorId() == 0x03F0 && device.getProductId() == 0x3D17) {
                printer = device;
                break;
            }
        }
        if (printer == null) {
            append("تعذّر التحقق من البرنامج الثابت: لم يُعثر على HP P1005 (VID 03F0، PID 3D17).");
            if (callback != null) callback.onResult(false, "لا توجد طابعة متصلة");
            return;
        }
        if (!usbManager.hasPermission(printer)) {
            append("توقّف التحقق من البرنامج الثابت: إذن USB غير ممنوح. افحص أجهزة USB أولاً.");
            if (callback != null) callback.onResult(false, "إذن USB غير ممنوح");
            return;
        }
        final UsbDevice target = printer;
        new Thread(() -> {
            UsbDeviceConnection connection = null;
            try {
                connection = usbManager.openDevice(target);
                if (connection == null) throw new IllegalStateException("تعذّر فتح جهاز USB.");
                int printerInterfaceId = -1;
                for (int i = 0; i < target.getInterfaceCount(); i++) {
                    if (target.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                        printerInterfaceId = target.getInterface(i).getId();
                        break;
                    }
                }
                if (printerInterfaceId < 0) throw new IllegalStateException("لم يُعثر على واجهة الطابعة.");

                byte[] response = new byte[1024];
                int count = connection.controlTransfer(0xA1, 0, 0, printerInterfaceId,
                        response, response.length, 3000);
                if (count < 2) throw new IllegalStateException("لم تُرجع الطابعة معرّف جهاز USB (result=" + count + ").");

                int declaredLength = ((response[0] & 0xFF) << 8) | (response[1] & 0xFF);
                int actualLength = Math.min(Math.max(0, declaredLength - 2), count - 2);
                String deviceId = new String(response, 2, actualLength, java.nio.charset.StandardCharsets.US_ASCII);
                final boolean firmwareFound = deviceId.contains("FWVER:");
                final String result;
                if (firmwareFound) {
                    result = "أعادت الطابعة معرّف الجهاز مع إصدار البرنامج الثابت:\n" + deviceId
                            + "\nهذا دليل قوي على أن البرنامج الثابت محمّل. لا يثبت بعد نجاح الطباعة.";
                } else {
                    result = "أعادت الطابعة معرّف الجهاز دون حقل FWVER:\n" + deviceId
                            + "\nحالة البرنامج الثابت غير مؤكدة؛ لا تُعد التحميل بشكل متكرر بعد.";
                }
                runOnUiThread(() -> {
                    append(result);
                    if (callback != null) callback.onResult(firmwareFound, deviceId);
                });
            } catch (Exception e) {
                final String error = e.getClass().getSimpleName() + ": " + e.getMessage();
                runOnUiThread(() -> {
                    append("تعذّر التحقق من البرنامج الثابت: " + error);
                    if (callback != null) callback.onResult(false, error);
                });
            } finally {
                if (connection != null) connection.close();
            }
        }, "P1005-Firmware-Verify").start();
        append("جارٍ قراءة معرّف جهاز USB وإصدار البرنامج الثابت...");
    }

    private void printBuiltInTestPage() {
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
        UsbDevice printer = null;
        for (UsbDevice device : devices.values()) {
            if (device.getVendorId() == 0x03F0 && device.getProductId() == 0x3D17) {
                printer = device;
                break;
            }
        }
        if (printer == null) {
            append("توقّفت الطباعة التجريبية: لم يُعثر على طابعة HP P1005 (VID 03F0، PID 3D17).");
            return;
        }
        if (!usbManager.hasPermission(printer)) {
            append("توقّفت الطباعة التجريبية: إذن USB غير ممنوح. افحص أجهزة USB أولاً.");
            return;
        }

        final UsbDevice target = printer;
        new Thread(() -> {
            UsbDeviceConnection connection = null;
            UsbInterface printerInterface = null;
            boolean claimed = false;
            try {
                java.io.InputStream input = getResources().openRawResource(R.raw.p1005_test_page);
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] readBuffer = new byte[8192];
                int n;
                while ((n = input.read(readBuffer)) != -1) buffer.write(readBuffer, 0, n);
                input.close();
                byte[] jobBytes = buffer.toByteArray();
                if (jobBytes.length < 16) throw new IllegalStateException("مهمة الطباعة المدمجة مفقودة أو صغيرة جدًا.");

                connection = usbManager.openDevice(target);
                if (connection == null) throw new IllegalStateException("تعذّر على أندرويد فتح جهاز USB.");

                UsbEndpoint outEndpoint = null;
                for (int i = 0; i < target.getInterfaceCount(); i++) {
                    UsbInterface candidate = target.getInterface(i);
                    if (candidate.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                        printerInterface = candidate;
                        break;
                    }
                }
                if (printerInterface == null) throw new IllegalStateException("لم يُعثر على واجهة من فئة الطابعات USB.");
                claimed = connection.claimInterface(printerInterface, true);
                if (!claimed) throw new IllegalStateException("تعذّر حجز واجهة الطابعة.");

                for (int i = 0; i < printerInterface.getEndpointCount(); i++) {
                    UsbEndpoint ep = printerInterface.getEndpoint(i);
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT
                            && ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        outEndpoint = ep;
                        break;
                    }
                }
                if (outEndpoint == null) throw new IllegalStateException("لم يُعثر على منفذ bulk OUT.");

                int sentTotal = 0;
                final int chunkSize = 16 * 1024;
                while (sentTotal < jobBytes.length) {
                    int length = Math.min(chunkSize, jobBytes.length - sentTotal);
                    int sent = connection.bulkTransfer(outEndpoint, jobBytes, sentTotal, length, 10000);
                    if (sent <= 0) {
                        throw new IllegalStateException("توقّف نقل USB عند " + sentTotal
                                + " من " + jobBytes.length + " بايت (bulkTransfer=" + sent + ").");
                    }
                    sentTotal += sent;
                }
                final int total = sentTotal;
                runOnUiThread(() -> append("أُرسل تدفق الصفحة التجريبية: " + total
                        + " بايت. قبل USB البيانات؛ تحقق من خروج الصفحة فعليًا من الطابعة."));
            } catch (Exception e) {
                final String error = e.getClass().getSimpleName() + ": " + e.getMessage();
                runOnUiThread(() -> append("فشلت الطباعة التجريبية: " + error));
            } finally {
                if (connection != null) {
                    if (claimed && printerInterface != null) {
                        try { connection.releaseInterface(printerInterface); } catch (Exception ignored) {}
                    }
                    connection.close();
                }
            }
        }, "P1005-Test-Print").start();
        append("تجهيز صفحة اختبار XQX مدمجة. لا تفصل الطابعة أثناء النقل.");
    }

    private void loadBuiltInFirmware() {
        loadBuiltInFirmware(null);
    }

    private void loadBuiltInFirmware(UsbResultCallback callback) {
        HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
        UsbDevice printer = null;
        for (UsbDevice device : devices.values()) {
            if (device.getVendorId() == 0x03F0 && device.getProductId() == 0x3D17) {
                printer = device;
                break;
            }
        }
        if (printer == null) {
            append("توقّف نقل البرنامج الثابت: لم يُعثر على طابعة HP P1005 (VID 03F0، PID 3D17).");
            if (callback != null) callback.onResult(false, "لا توجد طابعة متصلة");
            return;
        }
        if (!usbManager.hasPermission(printer)) {
            append("توقّف نقل البرنامج الثابت: إذن USB غير ممنوح. افحص أجهزة USB أولاً.");
            if (callback != null) callback.onResult(false, "إذن USB غير ممنوح");
            return;
        }

        final UsbDevice target = printer;
        new Thread(() -> {
            UsbDeviceConnection connection = null;
            UsbInterface printerInterface = null;
            boolean claimed = false;
            try {
                java.io.InputStream input = getResources().openRawResource(R.raw.sihpp1005);
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] readBuffer = new byte[8192];
                int n;
                while ((n = input.read(readBuffer)) != -1) buffer.write(readBuffer, 0, n);
                input.close();
                byte[] firmwareBytes = buffer.toByteArray();
                if (firmwareBytes.length == 0) throw new IllegalStateException("مورد البرنامج الثابت المدمج فارغ.");

                connection = usbManager.openDevice(target);
                if (connection == null) throw new IllegalStateException("تعذّر على أندرويد فتح جهاز USB.");

                UsbEndpoint outEndpoint = null;
                for (int i = 0; i < target.getInterfaceCount(); i++) {
                    UsbInterface candidate = target.getInterface(i);
                    if (candidate.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                        printerInterface = candidate;
                        break;
                    }
                }
                if (printerInterface == null) throw new IllegalStateException("لم يُعثر على واجهة من فئة الطابعات USB.");
                claimed = connection.claimInterface(printerInterface, true);
                if (!claimed) throw new IllegalStateException("تعذّر حجز واجهة الطابعة.");

                for (int i = 0; i < printerInterface.getEndpointCount(); i++) {
                    UsbEndpoint ep = printerInterface.getEndpoint(i);
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT
                            && ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        outEndpoint = ep;
                        break;
                    }
                }
                if (outEndpoint == null) throw new IllegalStateException("لم يُعثر على منفذ bulk OUT.");

                final int chunkSize = 16 * 1024;
                int sentTotal = 0;
                while (sentTotal < firmwareBytes.length) {
                    int length = Math.min(chunkSize, firmwareBytes.length - sentTotal);
                    int sent = connection.bulkTransfer(outEndpoint, firmwareBytes, sentTotal, length, 10000);
                    if (sent <= 0) {
                        throw new IllegalStateException("توقّف نقل USB عند " + sentTotal
                                + " من " + firmwareBytes.length + " بايت (bulkTransfer=" + sent + ").");
                    }
                    sentTotal += sent;
                }
                final int total = sentTotal;
                runOnUiThread(() -> {
                    append("أُرسلت بيانات البرنامج الثابت إلى منفذ USB: " + total
                            + " بايت. هذا يؤكد قبول USB للنقل فقط، لا نجاح التحميل أو الطباعة.");
                    if (callback != null) callback.onResult(true, String.valueOf(total));
                });
            } catch (Exception e) {
                final String error = e.getClass().getSimpleName() + ": " + e.getMessage();
                runOnUiThread(() -> {
                    append("فشل نقل البرنامج الثابت: " + error);
                    if (callback != null) callback.onResult(false, error);
                });
            } finally {
                if (connection != null) {
                    if (claimed && printerInterface != null) {
                        try { connection.releaseInterface(printerInterface); } catch (Exception ignored) {}
                    }
                    connection.close();
                }
            }
        }, "P1005-Firmware-Transfer").start();
        append("بدء نقل البرنامج الثابت إلى الطابعة المكتشفة. لا تفصل الطابعة ولا تطفئها.");
    }

    private void selectFirmwareFile() {
        try {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.setType("*/*");
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(pick, 41);
        } catch (Exception e) {
            append("تعذّر فتح منتقي الملفات: " + e.getMessage());
        }
    }

    private void requestFirmwareLoad() {
        if (!confirmFirmwareLoad) {
            loadBuiltInFirmware();
            return;
        }
        AlertDialog dialog = dialogBuilder()
                .setTitle("تحميل البرنامج الثابت المدمج؟")
                .setMessage("سيُرسَل البرنامج الثابت المدمج الخاص بطابعة HP P1005 عبر USB. قد يلزم ذلك بعد كل دورة تشغيل للطابعة. لا تفصل الطابعة ولا تطفئها أثناء النقل. قبول USB للبيانات لا يثبت نجاح تحميل البرنامج الثابت.")
                .setNegativeButton("إلغاء", (d, which) -> d.dismiss())
                .setPositiveButton("متابعة", (d, which) -> loadBuiltInFirmware())
                .create();
        showNativeDialog(dialog);
    }

    private void showNativeDialog(AlertDialog dialog) {
        dialog.setOnShowListener(ignored -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.18f);
                window.setBackgroundDrawable(new ColorDrawable(darkMode ? 0xF21D1D21 : 0xF2FFFFFF));
                // Dim only, no blur-behind (see styleDialogWindow).
            }
            int buttonColor = primaryColor();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(buttonColor);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(buttonColor);
            TextView message = dialog.findViewById(android.R.id.message);
            if (message != null) message.setTextColor(textPrimary());
            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            TextView alertTitle = titleId == 0 ? null : dialog.findViewById(titleId);
            if (alertTitle != null) alertTitle.setTextColor(textPrimary());
        });
        dialog.show();
    }

    private void copyActivityLog() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("سجل نشاط HP P1005", activityLog.toString()));
            systemToast("نُسخ سجل النشاط", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearActivityLog() {
        activityLog.setLength(0);
        activityLog.append("سجل نشاط HP P1005\nمُسح السجل.\n");
        if (output != null) output.setText(activityLog.toString());
    }

    private String describe(UsbDevice d) {
        return d.getDeviceName()
                + " | VID=0x" + hex(d.getVendorId())
                + " PID=0x" + hex(d.getProductId())
                + " | class=" + d.getDeviceClass()
                + " subclass=" + d.getDeviceSubclass()
                + " protocol=" + d.getDeviceProtocol()
                + " | interfaces=" + d.getInterfaceCount();
    }

    private String hex(int value) { return String.format("%04X", value); }

    private void inspectDevice(UsbDevice device) {
        append("فحص الواجهات ونقاط النهاية:");
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbInterface = device.getInterface(i);
            append(" واجهة " + i + ": class=" + usbInterface.getInterfaceClass()
                    + " subclass=" + usbInterface.getInterfaceSubclass()
                    + " protocol=" + usbInterface.getInterfaceProtocol()
                    + " endpoints=" + usbInterface.getEndpointCount());
            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint ep = usbInterface.getEndpoint(j);
                String direction = ep.getDirection() == UsbConstants.USB_DIR_OUT ? "OUT"
                        : ep.getDirection() == UsbConstants.USB_DIR_IN ? "IN" : "OTHER";
                append("  نقطة نهاية " + j + ": address=0x" + hex(ep.getAddress())
                        + " direction=" + direction
                        + " type=" + ep.getType()
                        + " maxPacket=" + ep.getMaxPacketSize());
            }
        }
        append("تشخيص فقط: لم تُرسَل أي بيانات برنامج ثابت أو طباعة.");
    }

    private void clearLogWithHeader(String header) {
        activityLog.setLength(0);
        activityLog.append(header).append("\n");
        if (output != null) output.setText(activityLog.toString());
    }

    private void append(String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> append(message));
            return;
        }
        activityLog.append(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()))
                .append("  ").append(message).append("\n");
        if (output != null) output.setText(activityLog.toString());
    }

    @Override protected void onDestroy() {
        try { unregisterReceiver(usbReceiver); } catch (IllegalArgumentException ignored) {}
        if (documentRenderer != null) {
            documentRenderer.close();
            documentRenderer = null;
        }
        if (currentPreviewBitmap != null && !currentPreviewBitmap.isRecycled()) {
            currentPreviewBitmap.recycle();
            currentPreviewBitmap = null;
        }
        super.onDestroy();
    }
}
