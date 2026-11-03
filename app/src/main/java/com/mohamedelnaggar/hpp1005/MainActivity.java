package com.mohamedelnaggar.hpp1005;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Property;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.BackEventCompat;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import com.mohamedelnaggar.hpp1005.Foo2XqxConverter;
import com.mohamedelnaggar.hpp1005.PrintFlow;
import com.mohamedelnaggar.hpp1005.SheetComposer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kotlin.UByte;
import kotlinx.coroutines.DebugKt;

/* loaded from: classes.dex */
public class MainActivity extends ComponentActivity {
    private static final String ACTION_USB_PERMISSION = "com.mohamedelnaggar.hpp1005.USB_PERMISSION";
    private static final int DIR_NONE = 0;
    private static final int DIR_TAB_BACK = 4;
    private static final int DIR_TAB_FORWARD = 3;
    private static final int REQUEST_DOCUMENT = 42;
    private boolean backCommitAnimating;
    private boolean backCommitting;
    private View backGesturePrevView;
    private float backGestureProgress;
    private Dialog capsuleDialog;
    private volatile int currentJobDone;
    private volatile String currentJobName;
    private volatile int currentJobTotal;
    private View currentPageView;
    private Bitmap currentPreviewBitmap;
    private ScrollView currentScrollView;
    private boolean darkMode;
    private DocumentRenderer documentRenderer;
    private boolean draftMode;
    private volatile boolean jobCancelRequested;
    private boolean keepScreenAwake;
    private ValueAnimator layoutModeAnimator;
    private TextView[] layoutModeLabels;
    private View layoutModePill;
    private float layoutModePosition;
    private Button layoutNext;
    private Bitmap layoutPreviewBitmap;
    private TextView layoutPreviewCount;
    private int layoutPreviewGeneration;
    private FrameLayout layoutPreviewHost;
    private ImageView layoutPreviewImage;
    private TextView layoutPreviewStatus;
    private Future<?> layoutPreviewTask;
    private Button layoutPrevious;
    private LinearLayout layoutSettingsHost;
    private TextView output;
    private FrameLayout pageHost;
    private FrameLayout pendingBackArrived;
    private PendingIntent permissionIntent;
    private OnBackPressedCallback predictiveBackCallback;
    private volatile boolean printInProgress;
    private SharedPreferences printPreferences;
    private Uri selectedDocumentUri;
    private boolean setupDone;
    private boolean setupFinishedWithWarning;
    private boolean setupRunning;
    private SharedPreferences uiPreferences;
    private UsbManager usbManager;
    private final StringBuilder activityLog = new StringBuilder();
    private String currentPage = "home";
    private final ArrayDeque<String> pageStack = new ArrayDeque<>();
    private final HashMap<String, Integer> scrollPositions = new HashMap<>();
    private final ArrayList<Animator> backAnimators = new ArrayList<>();
    private int DIR_SIGN = 1;
    private String appLanguage = "en";
    private String themeMode = "system";
    private boolean hapticsEnabled = true;
    private boolean confirmFirmwareLoad = true;
    private int prepareState = 0;
    private int prepareDetail = 0;
    private String prepareFailureReason = "";
    private boolean prepareReadyBannerDismissed = false;
    private boolean autoPreparePermissionRequested = false;
    private final Handler autoPrepareHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoPrepareRunnable = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.2
        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.maybeAutoPrepare();
        }
    };
    private String selectedDocumentName = "";
    private String selectedDocumentMime = "";
    private int currentPreviewPage = 0;
    private final ExecutorService layoutPreviewExecutor = Executors.newSingleThreadExecutor();
    private String paperSize = "A4";
    private int copies = 1;
    private String pageRange = "all";
    private String duplexMode = DebugKt.DEBUG_PROPERTY_VALUE_OFF;
    private int printDensity = 3;
    private String layoutMode = "size";
    private String scaleModePref = "fit";
    private int customScalePercent = 100;
    private String layoutOrientation = DebugKt.DEBUG_PROPERTY_VALUE_AUTO;
    private int pagesPerSheet = 2;
    private String paperSource = DebugKt.DEBUG_PROPERTY_VALUE_AUTO;
    private String sourceMapMain = "";
    private String sourceMapPriority = "";
    private boolean posterCutMarks = true;
    private int posterOverlapMm = 5;
    private float posterScalePercent = 150.0f;
    private String readingOrder = "rtl";
    private String bookletBinding = "right";
    private final HashMap<String, Float> imageAspectCache = new HashMap<>();
    private final String[] paperSizes = {"A4", "A5", "Letter", "Legal", "B5"};
    private final int[] setupSteps = {0, 0, 0};
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (MainActivity.ACTION_USB_PERMISSION.equals(intent.getAction())) {
                UsbDevice usbDevice = MainActivity.this.getUsbDevice(intent);
                if (usbDevice == null) {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.lambda$append$117(mainActivity.getString(R.string.text_022));
                } else if (intent.getBooleanExtra("permission", false)) {
                    MainActivity mainActivity2 = MainActivity.this;
                    mainActivity2.lambda$append$117(mainActivity2.getString(R.string.text_023));
                    MainActivity.this.inspectDevice(usbDevice);
                    MainActivity.this.maybeAutoPrepare();
                } else {
                    MainActivity.this.lambda$append$117(MainActivity.this.getString(R.string.text_024) + MainActivity.this.describe(usbDevice));
                }
                MainActivity.this.renderApp();
                return;
            }
            if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
                MainActivity mainActivity3 = MainActivity.this;
                mainActivity3.lambda$append$117(mainActivity3.getString(R.string.text_025));
                MainActivity.this.renderApp();
                MainActivity.this.scheduleAutoPrepare(1500L);
            } else if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(intent.getAction())) {
                MainActivity mainActivity4 = MainActivity.this;
                mainActivity4.lambda$append$117(mainActivity4.getString(R.string.text_026));
                MainActivity.this.onPrinterDetached();
                MainActivity.this.renderApp();
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    interface FormApply {
        boolean apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    interface IndexAction {
        void onChosen(int i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    interface ToggleAction {
        void onToggle(boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    interface UsbResultCallback {
        void onResult(boolean z, String str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public UsbDevice getUsbDevice(Intent intent) {
        Object parcelableExtra;
        if (Build.VERSION.SDK_INT >= 33) {
            parcelableExtra = intent.getParcelableExtra("device", UsbDevice.class);
            return (UsbDevice) parcelableExtra;
        }
        return (UsbDevice) intent.getParcelableExtra("device");
    }

    @Override // androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null) {
            this.currentPage = bundle.getString("page", "home");
        }
        applyAppLocale();
        this.usbManager = (UsbManager) getSystemService("usb");
        SharedPreferences sharedPreferences = getSharedPreferences("hp_p1005_preferences", 0);
        this.uiPreferences = sharedPreferences;
        if (sharedPreferences.contains("dark_mode") && !this.uiPreferences.contains("theme_mode")) {
            this.uiPreferences.edit().putString("theme_mode", this.uiPreferences.getBoolean("dark_mode", false) ? "dark" : "system").apply();
        }
        this.themeMode = this.uiPreferences.getString("theme_mode", "system");
        this.hapticsEnabled = this.uiPreferences.getBoolean("haptics", true);
        this.keepScreenAwake = this.uiPreferences.getBoolean("keep_awake", false);
        this.confirmFirmwareLoad = this.uiPreferences.getBoolean("confirm_firmware", true);
        applyThemeMode();
        FrameLayout frameLayout = new FrameLayout(this);
        this.pageHost = frameLayout;
        setContentView(frameLayout);
        registerBackCallback();
        if (bundle != null) {
            restorePageStack(bundle.getString("page_stack", ""));
        }
        updateBackCallbackState();
        SharedPreferences sharedPreferences2 = getSharedPreferences("hp_p1005_print_preferences", 0);
        this.printPreferences = sharedPreferences2;
        this.paperSize = sharedPreferences2.getString("paper_size", "A4");
        this.copies = this.printPreferences.getInt("copies", 1);
        this.pageRange = this.printPreferences.getString("page_range", "all");
        this.duplexMode = this.printPreferences.getString("duplex_mode", DebugKt.DEBUG_PROPERTY_VALUE_OFF);
        this.printDensity = this.printPreferences.getInt("density", 3);
        this.draftMode = this.printPreferences.getBoolean("draft_mode", false);
        this.layoutMode = this.printPreferences.getString("layout_mode", "size");
        this.scaleModePref = this.printPreferences.getString("scale_mode", "fit");
        this.customScalePercent = this.printPreferences.getInt("custom_scale", 100);
        SharedPreferences sharedPreferences3 = this.printPreferences;
        this.layoutOrientation = sharedPreferences3.getString("layout_orientation", "landscape".equals(sharedPreferences3.getString("orientation", "")) ? "landscape" : DebugKt.DEBUG_PROPERTY_VALUE_AUTO);
        this.pagesPerSheet = this.printPreferences.getInt("pages_per_sheet", 2);
        this.paperSource = this.printPreferences.getString("paper_source", DebugKt.DEBUG_PROPERTY_VALUE_AUTO);
        this.sourceMapMain = this.printPreferences.getString("source_map_main", "");
        this.sourceMapPriority = this.printPreferences.getString("source_map_priority", "");
        this.posterCutMarks = this.printPreferences.getBoolean("poster_cut_marks", true);
        this.posterOverlapMm = this.printPreferences.getInt("poster_overlap_mm", 5);
        this.posterScalePercent = this.printPreferences.getFloat("poster_scale_percent", 150.0f);
        this.readingOrder = this.printPreferences.getString("reading_order", "rtl");
        this.bookletBinding = this.printPreferences.getString("booklet_binding", "right");
        if (this.activityLog.length() == 0) {
            this.activityLog.append(getString(R.string.text_027));
            this.activityLog.append(getString(R.string.text_028));
        }
        this.permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION).setPackage(getPackageName()), Build.VERSION.SDK_INT >= 31 ? 167772160 : 201326592);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(this.usbReceiver, intentFilter, 4);
        } else {
            registerReceiver(this.usbReceiver, intentFilter);
        }
        MainDebugHooks.register(this);
        scheduleAutoPrepare(1000L);
        if (handleIncomingIntent(getIntent())) {
            return;
        }
        renderApp();
    }

    @Override // androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("page", this.currentPage);
        bundle.putString("page_stack", TextUtils.join(",", this.pageStack));
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (handleIncomingIntent(intent)) {
            return;
        }
        renderApp();
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        applyAppLocale();
        applyThemeMode();
        renderApp();
    }

    @Override // android.app.Activity, android.view.ContextThemeWrapper, android.content.ContextWrapper
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(AppLanguage.wrap(context, AppLanguage.selected(context)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean debuggable() {
        return (getApplicationInfo().flags & 2) != 0;
    }

    private void applyAppLocale() {
        this.appLanguage = AppLanguage.selected(this);
        Locale locale = new Locale(this.appLanguage);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        this.DIR_SIGN = isArabic() ? -1 : 1;
        getWindow().getDecorView().setLayoutDirection(appDirection());
    }

    private boolean isArabic() {
        return "ar".equals(this.appLanguage);
    }

    private int appDirection() {
        return isArabic() ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLanguageOptions() {
        if (this.printInProgress || this.setupRunning) {
            systemToast(getString(R.string.language_busy), 1).show();
            return;
        }
        String string = getString(R.string.text_167);
        String[] strArr = {getString(R.string.language_english), getString(R.string.language_arabic)};
        boolean isArabic = isArabic();
        showSelectionDialog(string, strArr, isArabic ? 1 : 0, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda73
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i) {
                MainActivity.this.lambda$showLanguageOptions$0(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showLanguageOptions$0(int i) {
        String str = i == 1 ? "ar" : "en";
        if (str.equals(this.appLanguage)) {
            return;
        }
        this.uiPreferences.edit().putString("app_language", str).apply();
        applyAppLocale();
        this.layoutPreviewGeneration++;
        Future<?> future = this.layoutPreviewTask;
        if (future != null) {
            future.cancel(true);
        }
        this.layoutSettingsHost = null;
        this.pageHost.removeAllViews();
        this.currentPageView = null;
        this.currentScrollView = null;
    }

    private void applyThemeMode() {
        boolean z = true;
        boolean z2 = (getResources().getConfiguration().uiMode & 48) == 32;
        if (!"dark".equals(this.themeMode) && (!"system".equals(this.themeMode) || !z2)) {
            z = false;
        }
        this.darkMode = z;
    }

    private String themeModeLabel() {
        return "dark".equals(this.themeMode) ? getString(R.string.text_029) : "light".equals(this.themeMode) ? getString(R.string.text_030) : getString(R.string.text_031);
    }

    private boolean handleIncomingIntent(Intent intent) {
        Uri uri;
        if (intent == null) {
            return false;
        }
        Uri data = intent.getData();
        try {
            uri = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
        } catch (Exception unused) {
            uri = null;
        }
        if (debuggable()) {
            StringBuilder sb = new StringBuilder("action=");
            sb.append(intent.getAction());
            sb.append(" data=");
            sb.append(data);
            sb.append(" stream=");
            sb.append(uri);
            sb.append(" extras=");
            sb.append(intent.getExtras() == null ? "none" : intent.getExtras().keySet());
            Log.i("HP1005-Share", sb.toString());
        } else {
            StringBuilder sb2 = new StringBuilder("action=");
            sb2.append(intent.getAction());
            sb2.append(" hasData=");
            sb2.append(data != null);
            sb2.append(" hasStream=");
            sb2.append(uri != null);
            Log.i("HP1005-Share", sb2.toString());
        }
        if (data == null && "android.intent.action.SEND".equals(intent.getAction())) {
            data = uri;
        }
        if (data == null) {
            return false;
        }
        loadSelectedDocument(data);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int dp(float f) {
        return (int) ((f * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int pageBackground() {
        return this.darkMode ? -16711423 : -657931;
    }

    private int cardColor() {
        return this.darkMode ? -15198184 : -1;
    }

    private int primaryColor() {
        return this.darkMode ? -16547330 : -16747810;
    }

    private int primaryDeep() {
        return this.darkMode ? -16547330 : -15834172;
    }

    private int textPrimary() {
        return this.darkMode ? -855308 : -15000805;
    }

    private int textSecondary() {
        return this.darkMode ? -6513500 : -8750464;
    }

    private int eyebrowColor() {
        return this.darkMode ? -7357976 : -9534301;
    }

    private int borderColor() {
        return this.darkMode ? 234881023 : 168761640;
    }

    private int dividerColor() {
        return this.darkMode ? 318767103 : 252646430;
    }

    private int tonalBg() {
        return this.darkMode ? -15587265 : -1839106;
    }

    private int tonalText() {
        return this.darkMode ? -16547330 : -16747810;
    }

    private int okBg() {
        return this.darkMode ? -15454177 : -1837334;
    }

    private int okText() {
        return this.darkMode ? -11741562 : -15432373;
    }

    private int warnBg() {
        return this.darkMode ? -13425137 : -134948;
    }

    private int warnText() {
        return this.darkMode ? -1005236 : -5084654;
    }

    private int trackColor() {
        return this.darkMode ? -14013905 : -1183500;
    }

    private int iconWell() {
        return this.darkMode ? -15587265 : -1839106;
    }

    private LinearLayout vertical() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        return linearLayout;
    }

    private LinearLayout horizontal() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        return linearLayout;
    }

    private LinearLayout.LayoutParams margin(int i, int i2, int i3, int i4) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(dp(i), dp(i2), dp(i3), dp(i4));
        return layoutParams;
    }

    private GradientDrawable rounded(int i, int i2, int i3, int i4) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{i, i2});
        gradientDrawable.setCornerRadius(dp(i4));
        if (i3 != 0) {
            gradientDrawable.setStroke(dp(1.0f), i3);
        }
        return gradientDrawable;
    }

    private GradientDrawable flat(int i, int i2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(i2));
        return gradientDrawable;
    }

    private GradientDrawable verticalGradient(int i, int i2, int i3) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{i, i2});
        gradientDrawable.setCornerRadius(dp(i3));
        return gradientDrawable;
    }

    private TextView styledText(String str, float f, int i, boolean z) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(f);
        textView.setTextColor(i);
        textView.setGravity(16);
        textView.setFontFeatureSettings("kern");
        textView.setIncludeFontPadding(true);
        textView.setTypeface(Typeface.create(z ? "sans-serif-medium" : "sans-serif", z ? 1 : 0));
        return textView;
    }

    private TextView label(String str, float f, int i, boolean z) {
        return styledText(str, f, i, z);
    }

    private ImageView iconView(int i, int i2, int i3) {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(i);
        if (i == R.drawable.ic_chevron_left) {
            imageView.setScaleX(isArabic() ? 1.0f : -1.0f);
        }
        imageView.setColorFilter(i2, PorterDuff.Mode.SRC_IN);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        float f = i3;
        imageView.setLayoutParams(new LinearLayout.LayoutParams(dp(f), dp(f)));
        return imageView;
    }

    private Button bigPrimaryButton(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setAllCaps(false);
        button.setTextSize(16.0f);
        button.setTypeface(Typeface.create("sans-serif-medium", 1));
        button.setTextColor(-1);
        button.setBackground(flat(primaryColor(), 28));
        button.setMinHeight(dp(56.0f));
        button.setPadding(dp(14.0f), 0, dp(14.0f), 0);
        button.setHapticFeedbackEnabled(this.hapticsEnabled);
        button.setStateListAnimator(null);
        return button;
    }

    private Button tonalButton(String str, float f) {
        Button button = new Button(this);
        button.setText(str);
        button.setAllCaps(false);
        button.setTextSize(f);
        button.setTypeface(Typeface.create("sans-serif-medium", 1));
        button.setTextColor(tonalText());
        button.setBackground(rounded(0, 0, tonalText(), 26));
        button.setMinHeight(dp(52.0f));
        button.setPadding(dp(14.0f), 0, dp(14.0f), 0);
        button.setHapticFeedbackEnabled(this.hapticsEnabled);
        button.setStateListAnimator(null);
        return button;
    }

    private Toast systemToast(String str, int i) {
        return Toast.makeText(new ContextThemeWrapper(getApplicationContext(), this.darkMode ? android.R.style.Theme_DeviceDefault : android.R.style.Theme_DeviceDefault_Light), str, i);
    }

    private LinearLayout card() {
        LinearLayout vertical = vertical();
        vertical.setPadding(dp(18.0f), dp(18.0f), dp(18.0f), dp(18.0f));
        vertical.setBackground(flat(cardColor(), 26));
        vertical.setClipToOutline(true);
        return vertical;
    }

    private TextView statusPill(boolean z) {
        int i;
        int i2;
        int i3;
        if (!z) {
            i = R.string.text_033;
            i2 = warnText();
            i3 = warnBg();
        } else if (this.prepareState == 1) {
            i = R.string.text_450;
            i2 = tonalText();
            i3 = tonalBg();
        } else if (this.prepareState == 2) {
            i = R.string.text_451;
            i2 = okText();
            i3 = okBg();
        } else if (this.prepareState == 3) {
            i = R.string.text_452;
            i2 = warnText();
            i3 = warnBg();
        } else {
            i = R.string.text_032;
            i2 = okText();
            i3 = okBg();
        }
        TextView label = label("●  " + getString(i), 13.0f, i2, true);
        label.setGravity(17);
        label.setPadding(dp(10.0f), dp(5.0f), dp(10.0f), dp(5.0f));
        label.setBackground(flat(i3, 16));
        return label;
    }

    private TextView sectionLabel(String str) {
        TextView label = label(str, 13.0f, textPrimary(), true);
        label.setPadding(dp(2.0f), 0, dp(2.0f), 0);
        return label;
    }

    private boolean isTabPage(String str) {
        return "home".equals(str) || "print".equals(str) || "queue".equals(str);
    }

    private boolean wideLayout() {
        return getResources().getConfiguration().screenWidthDp >= 600;
    }

    private void addPair(LinearLayout linearLayout, View view, View view2) {
        addPair(linearLayout, view, view2, 1.0f, 1.0f);
    }

    private void addPair(LinearLayout linearLayout, View view, View view2, float f, float f2) {
        if (!wideLayout()) {
            linearLayout.addView(view, margin(0, 0, 0, 14));
            linearLayout.addView(view2, margin(0, 0, 0, 14));
            return;
        }
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(48);
        linearLayout2.addView(view, new LinearLayout.LayoutParams(0, -2, f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, f2);
        layoutParams.setMarginStart(dp(16.0f));
        linearLayout2.addView(view2, layoutParams);
        linearLayout.addView(linearLayout2, margin(0, 0, 0, 14));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void renderApp() {
        LinearLayout linearLayout;
        if ("layout".equals(this.currentPage) && (linearLayout = this.layoutSettingsHost) != null && linearLayout.isAttachedToWindow()) {
            refreshLayoutSettings();
        } else {
            saveScrollPosition();
            swapToPage(this.currentPage, 0);
        }
    }

    private FrameLayout buildCurrentPageView() {
        int i;
        int dp;
        int pageBackground = pageBackground();
        getWindow().setStatusBarColor(pageBackground);
        getWindow().setNavigationBarColor(pageBackground);
        boolean z = false;
        getWindow().getDecorView().setSystemUiVisibility(this.darkMode ? 0 : 8208);
        if (this.keepScreenAwake) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(pageBackground);
        frameLayout.setLayoutDirection(appDirection());
        FadeScrollView fadeScrollView = new FadeScrollView(this);
        fadeScrollView.setFillViewport(false);
        fadeScrollView.setClipToPadding(false);
        fadeScrollView.setVerticalScrollBarEnabled(false);
        fadeScrollView.setOverScrollMode(1);
        LinearLayout vertical = vertical();
        boolean isTabPage = isTabPage(this.currentPage);
        if (isTabPage) {
            i = wideLayout() ? 210 : 118;
        } else {
            i = 42;
        }
        vertical.setPadding(dp(20.0f), dp(16.0f), dp(20.0f), dp(i));
        fadeScrollView.setBottomFadeHeight(isTabPage ? dp(56.0f) : 0);
        buildPage(vertical);
        fadeScrollView.addView(vertical, new FrameLayout.LayoutParams(-1, -2));
        boolean wideLayout = wideLayout();
        if (wideLayout && ("home".equals(this.currentPage) || "print".equals(this.currentPage) || "queue".equals(this.currentPage) || "preview".equals(this.currentPage) || "settings".equals(this.currentPage) || "layout".equals(this.currentPage))) {
            z = true;
        }
        int i2 = getResources().getConfiguration().screenWidthDp;
        if (wideLayout) {
            dp = dp(Math.min("layout".equals(this.currentPage) ? 1280 : z ? 880 : 640, i2));
        } else {
            dp = -1;
        }
        if (dp == -1) {
            dp = -1;
        }
        frameLayout.addView(fadeScrollView, new FrameLayout.LayoutParams(dp, -1, 1));
        this.currentScrollView = fadeScrollView;
        return frameLayout;
    }

    private static Interpolator oneUiEase() {
        return new PathInterpolator(0.15f, 0.5f, 0.1f, 0.9f);
    }

    private static Interpolator oneUiParallaxEase() {
        return new PathInterpolator(0.15f, 0.0f, 0.3f, 0.85f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean reducedMotion() {
        try {
            return Settings.Global.getFloat(getContentResolver(), "animator_duration_scale", 1.0f) == 0.0f;
        } catch (Exception unused) {
            return false;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private int tabOrder(String str) {
        int z;
        str.hashCode();
        switch (str.hashCode()) {
            case 3208415:
                if (str.equals("home")) {
                    z = 0;
                    break;
                }
                z = -1;
                break;
            case 106934957:
                if (str.equals("print")) {
                    z = 1;
                    break;
                }
                z = -1;
                break;
            case 107944209:
                if (str.equals("queue")) {
                    z = 2;
                    break;
                }
                z = -1;
                break;
            default:
                z = -1;
                break;
        }
        switch (z) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return -1;
        }
    }

    private void saveScrollPosition() {
        String str;
        ScrollView scrollView = this.currentScrollView;
        if (scrollView == null || (str = this.currentPage) == null) {
            return;
        }
        this.scrollPositions.put(str, Integer.valueOf(scrollView.getScrollY()));
    }

    private void restoreScrollPosition(String str) {
        ScrollView scrollView;
        final Integer num = this.scrollPositions.get(str);
        if (num == null || num.intValue() <= 0 || (scrollView = this.currentScrollView) == null) {
            return;
        }
        scrollView.post(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda75
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$restoreScrollPosition$1(num);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$restoreScrollPosition$1(Integer num) {
        this.currentScrollView.scrollTo(0, num.intValue());
    }

    private void restorePageStack(String str) {
        this.pageStack.clear();
        if (str == null || str.isEmpty()) {
            return;
        }
        String[] split = str.split(",");
        for (int length = split.length - 1; length >= 0; length--) {
            if (!split[length].isEmpty()) {
                this.pageStack.push(split[length]);
            }
        }
    }

    private void openPage(String str) {
        if (str.equals(this.currentPage)) {
            renderApp();
            return;
        }
        saveScrollPosition();
        int r1 = 3;
        if (isTabPage(str)) {
            r1 = tabOrder(str) <= tabOrder(this.currentPage) ? 4 : 3;
            this.pageStack.clear();
            if (isTabPage(this.currentPage)) {
                r1 = 0;
            }
        } else {
            this.pageStack.push(this.currentPage);
            this.scrollPositions.remove(str);
        }
        this.currentPage = str;
        swapToPage(str, r1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void navigateBack() {
        if (this.pageStack.isEmpty() && "home".equals(this.currentPage)) {
            return;
        }
        saveScrollPosition();
        String pop = this.pageStack.isEmpty() ? "home" : this.pageStack.pop();
        this.currentPage = pop;
        swapToPage(pop, 4);
    }

    private void swapToPage(final String str, int i) {
        final FrameLayout buildCurrentPageView = buildCurrentPageView();
        final View view = this.currentPageView;
        int r10 = this.pageHost.getWidth();
        boolean z = (view == null || i == 0 || reducedMotion() || this.pageHost.getWidth() <= 0) ? false : true;
        boolean z2 = i == 3 || i == 4;
        int i2 = i == 4 ? -1 : 1;
        if (z) {
            buildCurrentPageView.setTranslationX(i2 * r10 * this.DIR_SIGN);
        }
        this.pageHost.addView(buildCurrentPageView);
        this.currentPageView = buildCurrentPageView;
        if (isTabPage(str)) {
            showCapsuleNav(buildCurrentPageView);
        } else if (z && this.capsuleDialog != null) {
            fadeOutCapsuleAndDismiss();
        } else {
            dismissCapsuleOverlay();
        }
        if (!z) {
            if (view != null) {
                this.pageHost.removeView(view);
            }
            restoreScrollPosition(str);
            updateBackCallbackState();
            return;
        }
        Runnable runnable = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$swapToPage$2(view, buildCurrentPageView, str);
            }
        };
        if (z2) {
            animateProp(buildCurrentPageView, View.TRANSLATION_X, buildCurrentPageView.getTranslationX(), 0.0f, 345L, oneUiEase(), runnable);
            animateProp(view, View.TRANSLATION_X, 0.0f, (-i2) * r10 * this.DIR_SIGN, 345L, oneUiEase(), null);
        } else {
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$swapToPage$2(View view, FrameLayout frameLayout, String str) {
        this.pageHost.removeView(view);
        if (this.currentPageView != frameLayout) {
            return;
        }
        restoreScrollPosition(str);
        updateBackCallbackState();
    }

    private ObjectAnimator animateProp(View view, Property<View, Float> property, float f, float f2, long j, Interpolator interpolator, final Runnable runnable) {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, property, f, f2);
        ofFloat.setDuration(j);
        if (interpolator != null) {
            ofFloat.setInterpolator(interpolator);
        }
        if (runnable != null) {
            ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    runnable.run();
                }
            });
        }
        ofFloat.start();
        return ofFloat;
    }

    private void fadeOutCapsuleAndDismiss() {
        final Dialog dialog = this.capsuleDialog;
        if (dialog == null || !dialog.isShowing() || dialog.getWindow() == null || reducedMotion()) {
            dismissCapsuleOverlay();
            return;
        }
        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams attributes = window.getAttributes();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat.setDuration(120L);
        ofFloat.setInterpolator(oneUiParallaxEase());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda63
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MainActivity.lambda$fadeOutCapsuleAndDismiss$3(attributes, window, valueAnimator);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                attributes.alpha = 1.0f;
                window.setAttributes(attributes);
                if (MainActivity.this.capsuleDialog == dialog) {
                    MainActivity.this.dismissCapsuleOverlay();
                }
            }
        });
        ofFloat.start();
    }

    static /* synthetic */ void lambda$fadeOutCapsuleAndDismiss$3(WindowManager.LayoutParams layoutParams, Window window, ValueAnimator valueAnimator) {
        layoutParams.alpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        window.setAttributes(layoutParams);
    }

    private void registerBackCallback() {
        this.predictiveBackCallback = new OnBackPressedCallback(false) { // from class: com.mohamedelnaggar.hpp1005.MainActivity.4
            @Override // androidx.activity.OnBackPressedCallback
            public void handleOnBackStarted(BackEventCompat backEventCompat) {
                int width;
                if (MainActivity.this.debuggable()) {
                    Log.i("HP1005-Back", "started");
                }
                MainActivity.this.settleInFlightBackCommit();
                if (MainActivity.this.pageStack.isEmpty() || MainActivity.this.reducedMotion() || (width = MainActivity.this.pageHost.getWidth()) <= 0) {
                    return;
                }
                MainActivity mainActivity = MainActivity.this;
                mainActivity.backGesturePrevView = mainActivity.buildPageViewFor((String) mainActivity.pageStack.peek());
                MainActivity.this.backGesturePrevView.setTranslationX(((-width) / 4.0f) * MainActivity.this.DIR_SIGN);
                MainActivity.this.backGesturePrevView.setAlpha(0.73f);
                MainActivity.this.pageHost.addView(MainActivity.this.backGesturePrevView, 0);
                MainActivity.this.backGestureProgress = 0.0f;
            }

            @Override // androidx.activity.OnBackPressedCallback
            public void handleOnBackProgressed(BackEventCompat backEventCompat) {
                if (MainActivity.this.debuggable()) {
                    Log.i("HP1005-Back", "progressed " + backEventCompat.getProgress());
                }
                if (MainActivity.this.backGesturePrevView == null || MainActivity.this.currentPageView == null) {
                    return;
                }
                int width = MainActivity.this.pageHost.getWidth();
                MainActivity.this.backGestureProgress = backEventCompat.getProgress();
                MainActivity.this.currentPageView.setTranslationX(MainActivity.this.DIR_SIGN * width * MainActivity.this.backGestureProgress);
                MainActivity.this.backGesturePrevView.setTranslationX(((-width) / 4.0f) * MainActivity.this.DIR_SIGN * (1.0f - MainActivity.this.backGestureProgress));
                MainActivity.this.backGesturePrevView.setAlpha((MainActivity.this.backGestureProgress * 0.27f) + 0.73f);
            }

            @Override // androidx.activity.OnBackPressedCallback
            public void handleOnBackPressed() {
                MainActivity.this.settleInFlightBackCommit();
                if (MainActivity.this.debuggable()) {
                    Log.i("HP1005-Back", MainActivity.this.backGesturePrevView != null ? "gesture commit" : "plain back pressed");
                }
                if (MainActivity.this.backGesturePrevView != null) {
                    MainActivity.this.finishBackGesture();
                } else {
                    MainActivity.this.navigateBack();
                }
            }

            @Override // androidx.activity.OnBackPressedCallback
            public void handleOnBackCancelled() {
                if (MainActivity.this.debuggable()) {
                    Log.i("HP1005-Back", "cancelled");
                }
                MainActivity.this.cancelBackGesture();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, this.predictiveBackCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public FrameLayout buildPageViewFor(String str) {
        String str2 = this.currentPage;
        ScrollView scrollView = this.currentScrollView;
        this.currentPage = str;
        FrameLayout buildCurrentPageView = buildCurrentPageView();
        this.currentPage = str2;
        this.currentScrollView = scrollView;
        return buildCurrentPageView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishBackGesture() {
        View view = this.currentPageView;
        final FrameLayout frameLayout = (FrameLayout) this.backGesturePrevView;
        this.backGesturePrevView = null;
        int width = this.pageHost.getWidth();
        if (view == null || frameLayout == null || width <= 0 || reducedMotion()) {
            lambda$finishBackGesture$4(frameLayout);
            return;
        }
        this.pendingBackArrived = frameLayout;
        this.backCommitAnimating = true;
        this.backAnimators.add(animateProp(view, View.TRANSLATION_X, view.getTranslationX(), width * this.DIR_SIGN, Math.max(60L, (long) ((1.0f - this.backGestureProgress) * 345.0f)), oneUiEase(), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda16
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$finishBackGesture$4(frameLayout);
            }
        }));
        this.backAnimators.add(animateProp(frameLayout, View.TRANSLATION_X, frameLayout.getTranslationX(), 0.0f, 120L, oneUiParallaxEase(), null));
        this.backAnimators.add(animateProp(frameLayout, View.ALPHA, frameLayout.getAlpha(), 1.0f, 75L, null, null));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void settleInFlightBackCommit() {
        FrameLayout frameLayout;
        if (!this.backCommitAnimating || (frameLayout = this.pendingBackArrived) == null) {
            return;
        }
        lambda$finishBackGesture$4(frameLayout);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: commitBackGesture, reason: merged with bridge method [inline-methods] */
    public void lambda$finishBackGesture$4(FrameLayout frameLayout) {
        if (frameLayout == null || this.backCommitting) {
            return;
        }
        this.backCommitting = true;
        for (int size = this.backAnimators.size() - 1; size >= 0; size--) {
            this.backAnimators.get(size).cancel();
        }
        this.backAnimators.clear();
        this.backCommitAnimating = false;
        this.pendingBackArrived = null;
        this.backGesturePrevView = null;
        frameLayout.setTranslationX(0.0f);
        frameLayout.setAlpha(1.0f);
        saveScrollPosition();
        View view = this.currentPageView;
        if (view != null && view != frameLayout) {
            this.pageHost.removeView(view);
        }
        this.currentPageView = frameLayout;
        this.currentPage = this.pageStack.isEmpty() ? "home" : this.pageStack.pop();
        this.currentScrollView = findScrollView(frameLayout);
        if (isTabPage(this.currentPage)) {
            showCapsuleNav(frameLayout);
        } else {
            dismissCapsuleOverlay();
        }
        restoreScrollPosition(this.currentPage);
        updateBackCallbackState();
        this.backCommitting = false;
    }

    private ScrollView findScrollView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ScrollView findScrollView = findScrollView(viewGroup.getChildAt(i));
            if (findScrollView != null) {
                return findScrollView;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelBackGesture() {
        final View view = this.backGesturePrevView;
        this.backGesturePrevView = null;
        int width = this.pageHost.getWidth();
        if (this.currentPageView == null || view == null || width <= 0 || reducedMotion()) {
            View view2 = this.currentPageView;
            if (view2 != null) {
                view2.setTranslationX(0.0f);
            }
            if (view != null) {
                this.pageHost.removeView(view);
                return;
            }
            return;
        }
        animateProp(this.currentPageView, View.TRANSLATION_X, this.currentPageView.getTranslationX(), 0.0f, Math.max(60L, (long) (this.backGestureProgress * 345.0f)), oneUiEase(), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda142
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$cancelBackGesture$5(view);
            }
        });
        animateProp(view, View.TRANSLATION_X, view.getTranslationX(), ((-width) / 4.0f) * this.DIR_SIGN, 120L, oneUiParallaxEase(), null);
        animateProp(view, View.ALPHA, view.getAlpha(), 0.73f, 75L, null, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$cancelBackGesture$5(View view) {
        this.pageHost.removeView(view);
    }

    private void updateBackCallbackState() {
        OnBackPressedCallback onBackPressedCallback = this.predictiveBackCallback;
        if (onBackPressedCallback != null) {
            onBackPressedCallback.setEnabled((this.pageStack.isEmpty() && "home".equals(this.currentPage)) ? false : true);
        }
    }

    private void pageAppBar(LinearLayout linearLayout, String str) {
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(16);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(17);
        linearLayout2.setBackground(flat(cardColor(), 19));
        linearLayout2.setContentDescription(getString(R.string.text_034));
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_arrow_back);
        imageView.setColorFilter(textPrimary(), PorterDuff.Mode.SRC_IN);
        linearLayout2.addView(imageView, new LinearLayout.LayoutParams(dp(20.0f), dp(20.0f)));
        linearLayout2.setClickable(true);
        linearLayout2.setFocusable(true);
        linearLayout2.setHapticFeedbackEnabled(this.hapticsEnabled);
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda35
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$pageAppBar$6(view);
            }
        });
        horizontal.addView(linearLayout2, new LinearLayout.LayoutParams(dp(38.0f), dp(38.0f)));
        View label = label(str, 20.0f, textPrimary(), true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMarginStart(dp(12.0f));
        horizontal.addView(label, layoutParams);
        linearLayout.addView(horizontal, margin(0, 2, 0, 14));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$pageAppBar$6(View view) {
        performTapFeedback(view);
        navigateBack();
    }

    private static class FadeScrollView extends ScrollView {
        private int fadeHeight;
        private final Paint fadePaint;

        FadeScrollView(Context context) {
            super(context);
            Paint paint = new Paint(1);
            this.fadePaint = paint;
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }

        void setBottomFadeHeight(int i) {
            if (this.fadeHeight != i) {
                this.fadeHeight = i;
                invalidate();
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void dispatchDraw(Canvas canvas) {
            if (this.fadeHeight <= 0 || getHeight() <= 0) {
                super.dispatchDraw(canvas);
                return;
            }
            int min = Math.min(this.fadeHeight, getHeight() / 2);
            int scrollX = getScrollX();
            float f = scrollX;
            int r3 = getScrollY();
            int saveLayer = canvas.saveLayer(f, getScrollY(), getWidth() + scrollX, getHeight() + r3, null);
            super.dispatchDraw(canvas);
            this.fadePaint.setShader(new LinearGradient(0.0f, (getHeight() + r3) - min, 0.0f, getHeight() + r3, new int[]{-1, -1275068417, 654311423, ViewCompat.MEASURED_SIZE_MASK}, new float[]{0.0f, 0.35f, 0.75f, 1.0f}, Shader.TileMode.CLAMP));
            canvas.drawRect(f, (getHeight() + r3) - min, scrollX + getWidth(), r3 + getHeight(), this.fadePaint);
            canvas.restoreToCount(saveLayer);
        }
    }

    private GradientDrawable flatBordered(int i, int i2, int i3) {
        GradientDrawable flat = flat(i, i3);
        flat.setStroke(dp(1.0f), i2);
        return flat;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissCapsuleOverlay() {
        Dialog dialog = this.capsuleDialog;
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (RuntimeException unused) {
            }
            this.capsuleDialog = null;
        }
    }

    /* Rebuilt by hand from bytecode (decompiler output was incomplete). */
    private void showCapsuleNav(final FrameLayout frameLayout) {
        boolean z = Build.VERSION.SDK_INT >= 31 && ((WindowManager) getSystemService(WindowManager.class)).isCrossWindowBlurEnabled();
        Log.i("HPP1005", z ? "capsule mode: blur-dialog (flat tint + background blur)" : "capsule mode: overlay-dialog (opaque flat tint, blur unavailable)");
        LinearLayout buildCapsuleNav = buildCapsuleNav(true);
        buildCapsuleNav.setImportantForAccessibility(2);
        Dialog dialog = this.capsuleDialog;
        if (dialog != null && dialog.isShowing()) {
            try {
                this.capsuleDialog.setContentView(buildCapsuleNav);
                Window window2 = this.capsuleDialog.getWindow();
                if (window2 != null) {
                    window2.setBackgroundDrawable(capsuleWindowBackground(z));
                }
                LinearLayout buildCapsuleNav2 = buildCapsuleNav(false);
                buildCapsuleNav2.setAlpha(0.01f);
                addCapsuleInline(frameLayout, buildCapsuleNav2);
                return;
            } catch (RuntimeException unused) {
                dismissCapsuleOverlay();
            }
        }
        dismissCapsuleOverlay();
        LinearLayout buildCapsuleNav3 = buildCapsuleNav(false);
        buildCapsuleNav3.setAlpha(0.01f);
        addCapsuleInline(frameLayout, buildCapsuleNav3);
        final Dialog dialog2 = new Dialog(this);
        dialog2.setContentView(buildCapsuleNav);
        dialog2.setCancelable(false);
        Window window = dialog2.getWindow();
        if (window == null) {
            addCapsuleInline(frameLayout, buildCapsuleNav);
            return;
        }
        window.setBackgroundDrawable(capsuleWindowBackground(z));
        if (z && Build.VERSION.SDK_INT >= 31) {
            window.setBackgroundBlurRadius(dp(28.0f));
        }
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = 81;
        attributes.y = dp(16.0f);
        attributes.width = -2;
        attributes.height = -2;
        attributes.flags |= 40;
        attributes.flags &= -3;
        window.setAttributes(attributes);
        this.capsuleDialog = dialog2;
        frameLayout.post(new Runnable() {
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$showCapsuleNav$7(dialog2, frameLayout);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showCapsuleNav$7(Dialog dialog, FrameLayout frameLayout) {
        if (this.capsuleDialog != dialog || isFinishing() || isDestroyed()) {
            return;
        }
        try {
            dialog.show();
        } catch (RuntimeException e) {
            Log.w("HPP1005", "capsule blur dialog failed; inline fallback", e);
            if (this.capsuleDialog == dialog) {
                this.capsuleDialog = null;
            }
            addCapsuleInline(frameLayout, buildCapsuleNav(false));
        }
    }

    private GradientDrawable capsuleWindowBackground(boolean z) {
        boolean z2 = this.darkMode;
        return flatBordered(z2 ? z ? -651877083 : -14342875 : z ? -637534209 : -1, z2 ? 452984831 : 436207616, 38);
    }

    private void addCapsuleInline(FrameLayout frameLayout, LinearLayout linearLayout) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 81);
        layoutParams.setMargins(0, 0, 0, dp(16.0f));
        frameLayout.addView(linearLayout, layoutParams);
    }

    private LinearLayout buildCapsuleNav(boolean z) {
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(17);
        horizontal.setPadding(dp(8.0f), dp(8.0f), dp(8.0f), dp(8.0f));
        if (z) {
            horizontal.setBackground(null);
        } else {
            horizontal.setBackground(flat(this.darkMode ? -14342875 : -1, 38));
            horizontal.setClipToOutline(true);
            horizontal.setElevation(dp(14.0f));
        }
        String selectedBottomTab = selectedBottomTab();
        horizontal.addView(navItem(R.drawable.ic_nav_home, getString(R.string.text_035), "home", "home".equals(selectedBottomTab)));
        horizontal.addView(navItem(R.drawable.ic_nav_printer, getString(R.string.text_036), "print", "print".equals(selectedBottomTab)));
        horizontal.addView(navItem(R.drawable.ic_nav_queue, getString(R.string.text_037), "queue", "queue".equals(selectedBottomTab)));
        return horizontal;
    }

    private String selectedBottomTab() {
        return ("home".equals(this.currentPage) || "setup".equals(this.currentPage)) ? "home" : ("print".equals(this.currentPage) || "preview".equals(this.currentPage)) ? "print" : "queue".equals(this.currentPage) ? "queue" : "home";
    }

    private LinearLayout navItem(int i, String str, final String str2, boolean z) {
        int textSecondary;
        LinearLayout vertical = vertical();
        vertical.setGravity(17);
        vertical.setPadding(dp(0.0f), dp(9.0f), dp(0.0f), dp(8.0f));
        if (z) {
            final GradientDrawable flat = flat(this.darkMode ? -12698046 : -1447442, 24);
            vertical.setBackground(flat);
            if (!reducedMotion()) {
                flat.setAlpha(0);
                ValueAnimator ofInt = ValueAnimator.ofInt(0, 255);
                ofInt.setDuration(90L);
                ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda76
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        flat.setAlpha(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofInt.start();
            }
        }
        if (z) {
            textSecondary = this.darkMode ? -1 : -15000805;
        } else {
            textSecondary = textSecondary();
        }
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(i);
        imageView.setColorFilter(textSecondary, PorterDuff.Mode.SRC_IN);
        vertical.addView(imageView, new LinearLayout.LayoutParams(dp(22.0f), dp(22.0f)));
        TextView label = label(str, 12.0f, textSecondary, true);
        label.setMaxLines(1);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = dp(3.0f);
        vertical.addView(label, layoutParams);
        vertical.setLayoutParams(new LinearLayout.LayoutParams(dp(100.0f), -2));
        vertical.setContentDescription(str);
        vertical.setClickable(true);
        vertical.setFocusable(true);
        vertical.setHapticFeedbackEnabled(this.hapticsEnabled);
        vertical.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda77
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$navItem$9(str2, view);
            }
        });
        return vertical;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$navItem$9(String str, View view) {
        performTapFeedback(view);
        openPage(str);
    }

    private void buildPage(LinearLayout linearLayout) {
        this.output = null;
        String str = this.currentPage;
        str.hashCode();
        switch (str) {
            case "sourcemap":
                buildSourceMapPage(linearLayout);
                break;
            case "activity":
                buildActivityPage(linearLayout);
                break;
            case "layout":
                buildLayoutPage(linearLayout);
                break;
            case "preview":
                buildPreviewPage(linearLayout);
                break;
            case "help":
                buildHelpPage(linearLayout);
                break;
            case "about":
                buildAboutPage(linearLayout);
                break;
            case "print":
                buildPrintPage(linearLayout);
                break;
            case "queue":
                buildQueuePage(linearLayout);
                break;
            case "setup":
                buildSetupPage(linearLayout);
                break;
            case "settings":
                buildSettingsPage(linearLayout);
                break;
            default:
                buildHomePage(linearLayout);
                break;
        }
    }

    private void pageHeader(LinearLayout linearLayout, String str, String str2, String str3, boolean z) {
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(48);
        LinearLayout vertical = vertical();
        if (str != null) {
            vertical.addView(label(str, 11.0f, eyebrowColor(), true), margin(0, 4, 0, 6));
        }
        TextView label = label(str2, 20.0f, textPrimary(), true);
        label.setLineSpacing(0.0f, 1.02f);
        vertical.addView(label, margin(0, 0, 0, 5));
        if (str3 != null) {
            TextView label2 = label(str3, 13.0f, textSecondary(), false);
            label2.setLineSpacing(dp(3.0f), 1.1f);
            vertical.addView(label2);
        }
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (z) {
            final LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setGravity(17);
            linearLayout2.setBackground(flat(cardColor(), 18));
            linearLayout2.setContentDescription(getString(R.string.text_038));
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.ic_more_vert);
            imageView.setColorFilter(textPrimary(), PorterDuff.Mode.SRC_IN);
            linearLayout2.addView(imageView, new LinearLayout.LayoutParams(dp(19.0f), dp(19.0f)));
            linearLayout2.setClickable(true);
            linearLayout2.setFocusable(true);
            linearLayout2.setHapticFeedbackEnabled(this.hapticsEnabled);
            linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda39
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$pageHeader$10(linearLayout2, view);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f));
            layoutParams.setMargins(dp(10.0f), dp(6.0f), 0, 0);
            horizontal.addView(linearLayout2, layoutParams);
        }
        linearLayout.addView(horizontal, margin(0, 0, 0, 16));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$pageHeader$10(LinearLayout linearLayout, View view) {
        performTapFeedback(view);
        showMainMenu(linearLayout);
    }

    private void showMainMenu(View view) {
        boolean z = this.darkMode;
        int i = z ? -14342875 : -1;
        int i2 = z ? 788529151 : 335544320;
        final PopupWindow[] popupWindowArr = new PopupWindow[1];
        LinearLayout vertical = vertical();
        vertical.setMinimumWidth(dp(208.0f));
        vertical.setPadding(dp(6.0f), dp(6.0f), dp(6.0f), dp(6.0f));
        String[] strArr = {getString(R.string.text_039), getString(R.string.text_040)};
        for (int i3 = 0; i3 < 2; i3++) {
            final String str = strArr[i3];
            TextView label = label(str, 16.0f, textPrimary(), false);
            label.setGravity(8388627);
            label.setMinHeight(dp(50.0f));
            label.setPadding(dp(20.0f), 0, dp(20.0f), 0);
            label.setBackground(new RippleDrawable(ColorStateList.valueOf(i2), null, flat(this.darkMode ? -1 : ViewCompat.MEASURED_STATE_MASK, 20)));
            label.setClickable(true);
            label.setFocusable(true);
            label.setHapticFeedbackEnabled(this.hapticsEnabled);
            label.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda133
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    MainActivity.this.lambda$showMainMenu$11(popupWindowArr, str, view2);
                }
            });
            vertical.addView(label, margin(0, 0, 0, 0));
        }
        vertical.setOutlineProvider(new ViewOutlineProvider() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.5
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view2, Outline outline) {
                outline.setRoundRect(0, 0, view2.getWidth(), view2.getHeight(), MainActivity.this.dp(26.0f));
            }
        });
        vertical.setClipToOutline(true);
        PopupWindow popupWindow = new PopupWindow((View) vertical, -2, -2, true);
        popupWindowArr[0] = popupWindow;
        popupWindow.setBackgroundDrawable(flat(i, 26));
        popupWindow.setElevation(dp(12.0f));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view, 0, dp(4.0f), 8388661);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showMainMenu$11(PopupWindow[] popupWindowArr, String str, View view) {
        performTapFeedback(view);
        popupWindowArr[0].dismiss();
        openPage(getString(R.string.text_039).contentEquals(str) ? "settings" : "about");
    }

    private void performTapFeedback(View view) {
        if (this.hapticsEnabled) {
            view.performHapticFeedback(1);
        }
    }

    private String readAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception unused) {
            return getString(R.string.text_041);
        }
    }

    private void buildHomePage(LinearLayout linearLayout) {
        pageHeader(linearLayout, null, getString(R.string.text_035), null, true);
        boolean hasPrinterConnected = hasPrinterConnected();
        LinearLayout card = card();
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(16);
        LinearLayout vertical = vertical();
        vertical.addView(label("HP LaserJet P1005", 16.5f, textPrimary(), true));
        vertical.addView(label(getString(R.string.text_042), 13.0f, textSecondary(), false), margin(0, 3, 0, 0));
        LinearLayout vertical2 = vertical();
        vertical2.addView(statusPill(hasPrinterConnected), new LinearLayout.LayoutParams(-2, -2));
        vertical.addView(vertical2, margin(0, 9, 0, 0));
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.p1005_photo);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setContentDescription(getString(R.string.text_043));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(118.0f), dp(104.0f));
        layoutParams.setMargins(0, 0, dp(14.0f), 0);
        horizontal.addView(imageView, layoutParams);
        card.addView(horizontal, margin(0, 0, 0, 0));
        LinearLayout horizontal2 = horizontal();
        Button button = new Button(this);
        button.setText(getString(R.string.text_044));
        button.setAllCaps(false);
        button.setTextSize(16.0f);
        button.setTypeface(Typeface.create("sans-serif-medium", 1));
        button.setTextColor(-1);
        button.setBackground(flat(primaryColor(), 24));
        button.setMinHeight(dp(48.0f));
        button.setStateListAnimator(null);
        button.setHapticFeedbackEnabled(this.hapticsEnabled);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda15
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildHomePage$12(view);
            }
        });
        horizontal2.addView(button, new LinearLayout.LayoutParams(0, dp(48.0f), 1.0f));
        Button button2 = tonalButton(getString(R.string.text_045), 16.0f);
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda26
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildHomePage$13(view);
            }
        });
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(48.0f), 1.0f);
        layoutParams2.setMarginStart(dp(10.0f));
        horizontal2.addView(button2, layoutParams2);
        LinearLayout horizontal3 = horizontal();
        horizontal3.addView(label(getString(R.string.text_046), 16.0f, textPrimary(), true), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView label = label(getString(R.string.text_047), 13.0f, tonalText(), true);
        label.setPadding(dp(8.0f), dp(6.0f), 0, dp(6.0f));
        label.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda37
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildHomePage$14(view);
            }
        });
        horizontal3.addView(label);
        LinearLayout card2 = card();
        ArrayList<String> recentLogLines = recentLogLines(3);
        if (recentLogLines.isEmpty()) {
            card2.addView(label(getString(R.string.text_048), 13.0f, textSecondary(), false));
        } else {
            Iterator<String> it = recentLogLines.iterator();
            while (it.hasNext()) {
                TextView label2 = label("•  " + it.next(), 13.0f, textSecondary(), false);
                label2.setLineSpacing((float) dp(4.0f), 1.0f);
                card2.addView(label2, margin(0, 0, 0, 5));
            }
        }
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(17);
        linearLayout2.setOrientation(0);
        linearLayout2.setBackground(rounded(0, 0, this.darkMode ? -12961218 : -2565922, 28));
        linearLayout2.setHapticFeedbackEnabled(this.hapticsEnabled);
        ImageView imageView2 = new ImageView(this);
        imageView2.setImageResource(R.drawable.ic_nav_printer);
        imageView2.setColorFilter(textPrimary(), PorterDuff.Mode.SRC_IN);
        linearLayout2.addView(imageView2, new LinearLayout.LayoutParams(dp(20.0f), dp(20.0f)));
        TextView label3 = label(getString(R.string.text_049), 16.0f, textPrimary(), true);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.setMarginStart(dp(9.0f));
        linearLayout2.addView(label3, layoutParams3);
        linearLayout2.setClickable(true);
        linearLayout2.setFocusable(true);
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda48
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildHomePage$15(view);
            }
        });
        LinearLayout vertical3 = vertical();
        vertical3.addView(card, margin(0, 0, 0, 14));
        addPrepareBanner(vertical3);
        vertical3.addView(horizontal2, margin(0, 0, 0, 0));
        LinearLayout vertical4 = vertical();
        vertical4.addView(horizontal3, margin(2, 0, 0, 8));
        vertical4.addView(card2, margin(0, 0, 0, 14));
        vertical4.addView(linearLayout2, new LinearLayout.LayoutParams(-1, dp(56.0f)));
        addPair(linearLayout, vertical3, vertical4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildHomePage$12(View view) {
        performTapFeedback(view);
        openDocumentPicker(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildHomePage$13(View view) {
        performTapFeedback(view);
        openDocumentPicker(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildHomePage$14(View view) {
        performTapFeedback(view);
        openPage("queue");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildHomePage$15(View view) {
        performTapFeedback(view);
        openPage("setup");
    }

    private ArrayList<String> recentLogLines(int i) {
        String[] split = this.activityLog.toString().trim().split("\\n");
        ArrayList<String> arrayList = new ArrayList<>();
        for (int length = split.length - 1; length >= 0 && arrayList.size() < i; length--) {
            String trim = split[length].trim();
            if (!trim.isEmpty() && !trim.contains(getString(R.string.text_050)) && !trim.contains("HP P1005 activity log")) {
                arrayList.add(0, trim);
            }
        }
        return arrayList;
    }

    private void buildPrintPage(LinearLayout linearLayout) {
        String string;
        pageHeader(linearLayout, null, getString(R.string.text_036), getString(R.string.text_051), false);
        LinearLayout card = card();
        LinearLayout horizontal = horizontal();
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(17);
        linearLayout2.setBackground(flat(iconWell(), 15));
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_doc);
        imageView.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
        linearLayout2.addView(imageView, new LinearLayout.LayoutParams(dp(24.0f), dp(24.0f)));
        horizontal.addView(linearLayout2, new LinearLayout.LayoutParams(dp(46.0f), dp(46.0f)));
        LinearLayout vertical = vertical();
        vertical.setPaddingRelative(dp(12.0f), 0, 0, 0);
        vertical.addView(label(getString(R.string.text_052), 16.0f, textPrimary(), true), margin(0, 0, 0, 4));
        vertical.addView(label(this.selectedDocumentUri == null ? getString(R.string.text_053) : this.selectedDocumentName, 11.5f, textSecondary(), false));
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        card.addView(horizontal, margin(0, 0, 0, 13));
        if (this.selectedDocumentUri == null) {
            string = getString(R.string.text_054);
        } else {
            DocumentRenderer documentRenderer = this.documentRenderer;
            if (documentRenderer != null && documentRenderer.isSupported()) {
                string = this.documentRenderer.getPageCount() + getString(R.string.text_055) + this.documentRenderer.getFormatLabel();
            } else {
                string = getString(R.string.text_056);
            }
        }
        TextView label = label(string, 12.0f, textSecondary(), false);
        label.setLineSpacing(dp(3.0f), 1.0f);
        card.addView(label, margin(0, 0, 0, 13));
        LinearLayout horizontal2 = horizontal();
        Button bigPrimaryButton = bigPrimaryButton(getString(this.selectedDocumentUri == null ? R.string.text_057 : R.string.text_058));
        bigPrimaryButton.setTextSize(14.5f);
        bigPrimaryButton.setMinHeight(dp(48.0f));
        bigPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda90
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildPrintPage$16(view);
            }
        });
        horizontal2.addView(bigPrimaryButton, new LinearLayout.LayoutParams(0, dp(48.0f), 1.0f));
        Button button = tonalButton(getString(R.string.text_059), 13.5f);
        button.setMinHeight(dp(48.0f));
        button.setEnabled(this.selectedDocumentUri != null);
        button.setAlpha(this.selectedDocumentUri != null ? 1.0f : 0.5f);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda91
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildPrintPage$17(view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(48.0f), 1.0f);
        layoutParams.setMarginStart(dp(9.0f));
        horizontal2.addView(button, layoutParams);
        card.addView(horizontal2);
        LinearLayout card2 = card();
        LinearLayout horizontal3 = horizontal();
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setGravity(17);
        linearLayout3.setBackground(flat(iconWell(), 15));
        ImageView imageView2 = new ImageView(this);
        imageView2.setImageResource(R.drawable.ic_nav_printer);
        imageView2.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
        linearLayout3.addView(imageView2, new LinearLayout.LayoutParams(dp(24.0f), dp(24.0f)));
        horizontal3.addView(linearLayout3, new LinearLayout.LayoutParams(dp(46.0f), dp(46.0f)));
        LinearLayout vertical2 = vertical();
        vertical2.setPaddingRelative(dp(12.0f), 0, 0, 0);
        vertical2.addView(label(getString(R.string.text_060), 16.0f, textPrimary(), true), margin(0, 0, 0, 4));
        vertical2.addView(label(getString(R.string.text_061), 11.5f, textSecondary(), false));
        horizontal3.addView(vertical2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        card2.addView(horizontal3, margin(0, 0, 0, 13));
        TextView label2 = label(getString(R.string.text_062), 12.0f, textSecondary(), false);
        label2.setLineSpacing(dp(3.0f), 1.0f);
        card2.addView(label2, margin(0, 0, 0, 14));
        Button button2 = tonalButton(getString(R.string.text_063), 14.5f);
        button2.setMinHeight(dp(48.0f));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda92
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildPrintPage$18(view);
            }
        });
        card2.addView(button2, new LinearLayout.LayoutParams(-1, dp(48.0f)));
        addPair(linearLayout, card, card2);
        Button button3 = tonalButton(getString(R.string.text_064), 14.5f);
        button3.setMinHeight(dp(52.0f));
        button3.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda93
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildPrintPage$19(view);
            }
        });
        linearLayout.addView(button3, new LinearLayout.LayoutParams(-1, dp(52.0f)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPrintPage$16(View view) {
        performTapFeedback(view);
        openDocumentPicker(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPrintPage$17(View view) {
        if (this.selectedDocumentUri == null) {
            openDocumentPicker(false);
        } else {
            openPage("preview");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPrintPage$18(View view) {
        performTapFeedback(view);
        printBuiltInTestPage();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPrintPage$19(View view) {
        performTapFeedback(view);
        openPage("settings");
    }

    private void addToolRow(LinearLayout linearLayout, String str, String str2, String str3, final Runnable runnable) {
        LinearLayout horizontal = horizontal();
        horizontal.setPadding(0, dp(12.0f), 0, dp(12.0f));
        LinearLayout vertical = vertical();
        vertical.addView(label(str, 16.0f, textPrimary(), true), margin(0, 0, 0, 4));
        TextView label = label(str2, 13.0f, textSecondary(), false);
        label.setMaxLines(3);
        vertical.addView(label);
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Button button = tonalButton(str3, 13.0f);
        button.setMinHeight(dp(38.0f));
        button.setPadding(dp(8.0f), 0, dp(8.0f), 0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(76.0f), dp(38.0f));
        layoutParams.setMarginStart(dp(10.0f));
        horizontal.addView(button, layoutParams);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda124
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$addToolRow$20(runnable, view);
            }
        });
        linearLayout.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$addToolRow$20(Runnable runnable, View view) {
        performTapFeedback(view);
        runnable.run();
    }

    private void addSeparator(LinearLayout linearLayout) {
        View view = new View(this);
        view.setBackgroundColor(dividerColor());
        linearLayout.addView(view, new LinearLayout.LayoutParams(-1, dp(1.0f)));
    }

    private void buildQueuePage(LinearLayout linearLayout) {
        pageHeader(linearLayout, "HP LaserJet P1005", getString(R.string.text_037), getString(R.string.text_065), false);
        LinearLayout vertical = vertical();
        LinearLayout vertical2 = vertical();
        vertical.addView(sectionLabel(getString(R.string.text_066)), margin(2, 0, 0, 8));
        if (this.printInProgress && this.currentJobName != null) {
            LinearLayout card = card();
            LinearLayout horizontal = horizontal();
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setGravity(17);
            linearLayout2.setBackground(flat(iconWell(), 13));
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.ic_doc);
            imageView.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
            linearLayout2.addView(imageView, new LinearLayout.LayoutParams(dp(20.0f), dp(20.0f)));
            horizontal.addView(linearLayout2, new LinearLayout.LayoutParams(dp(40.0f), dp(40.0f)));
            LinearLayout vertical3 = vertical();
            vertical3.setPaddingRelative(dp(11.0f), 0, 0, 0);
            TextView label = label(this.currentJobName, 14.0f, textPrimary(), true);
            label.setTextAlignment(5);
            label.setMaxLines(1);
            label.setEllipsize(TextUtils.TruncateAt.END);
            vertical3.addView(label, margin(0, 0, 0, 3));
            vertical3.addView(label(getString(R.string.text_067) + Math.min(this.currentJobDone + 1, Math.max(this.currentJobTotal, 1)) + getString(R.string.text_016) + Math.max(this.currentJobTotal, 1), 11.5f, textSecondary(), false));
            horizontal.addView(vertical3, new LinearLayout.LayoutParams(0, -2, 1.0f));
            card.addView(horizontal, margin(0, 0, 0, 4));
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(Math.max(this.currentJobTotal, 1));
            progressBar.setProgress(Math.min(this.currentJobDone, Math.max(this.currentJobTotal, 1)));
            progressBar.setProgressTintList(ColorStateList.valueOf(primaryColor()));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(trackColor()));
            progressBar.setProgressBackgroundTintMode(PorterDuff.Mode.SRC_IN);
            card.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(8.0f)));
            Button button = tonalButton(getString(R.string.text_068), 13.5f);
            button.setMinHeight(dp(44.0f));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda59
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildQueuePage$21(view);
                }
            });
            card.addView(button, margin(0, 11, 0, 0));
            vertical.addView(card, margin(0, 0, 0, 16));
        } else {
            LinearLayout card2 = card();
            card2.setGravity(1);
            card2.addView(label(getString(R.string.text_070), 13.5f, textSecondary(), false), margin(0, 6, 0, 6));
            vertical.addView(card2, margin(0, 0, 0, 16));
        }
        vertical.addView(sectionLabel(getString(R.string.text_071)), margin(2, 0, 0, 8));
        if (this.selectedDocumentUri != null) {
            LinearLayout card3 = card();
            card3.setPadding(dp(14.0f), dp(12.0f), dp(14.0f), dp(12.0f));
            LinearLayout horizontal2 = horizontal();
            LinearLayout linearLayout3 = new LinearLayout(this);
            linearLayout3.setGravity(17);
            linearLayout3.setBackground(flat(iconWell(), 13));
            ImageView imageView2 = new ImageView(this);
            DocumentRenderer documentRenderer = this.documentRenderer;
            imageView2.setImageResource((documentRenderer == null || !documentRenderer.isImage()) ? R.drawable.ic_doc : R.drawable.ic_image);
            imageView2.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
            linearLayout3.addView(imageView2, new LinearLayout.LayoutParams(dp(20.0f), dp(20.0f)));
            horizontal2.addView(linearLayout3, new LinearLayout.LayoutParams(dp(40.0f), dp(40.0f)));
            LinearLayout vertical4 = vertical();
            vertical4.setPaddingRelative(dp(11.0f), 0, 0, 0);
            TextView label2 = label(this.selectedDocumentName, 13.5f, textPrimary(), true);
            label2.setTextAlignment(5);
            label2.setMaxLines(1);
            label2.setEllipsize(TextUtils.TruncateAt.END);
            vertical4.addView(label2, margin(0, 0, 0, 3));
            vertical4.addView(label(getString(this.printInProgress ? R.string.text_072 : R.string.text_073), 11.5f, textSecondary(), false));
            horizontal2.addView(vertical4, new LinearLayout.LayoutParams(0, -2, 1.0f));
            card3.addView(horizontal2);
            vertical.addView(card3, margin(0, 0, 0, 16));
        } else {
            LinearLayout card4 = card();
            card4.setGravity(1);
            card4.addView(label(getString(R.string.text_074), 13.5f, textSecondary(), false), margin(0, 6, 0, 6));
            vertical.addView(card4, margin(0, 0, 0, 16));
        }
        vertical2.addView(sectionLabel(getString(R.string.text_075)), margin(2, 0, 0, 8));
        LinearLayout card5 = card();
        card5.setPadding(dp(14.0f), dp(6.0f), dp(14.0f), dp(6.0f));
        ArrayList<String> recentLogLines = recentLogLines(8);
        if (recentLogLines.isEmpty()) {
            card5.addView(label(getString(R.string.text_076), 12.5f, textSecondary(), false), margin(0, 10, 0, 10));
        } else {
            for (int size = recentLogLines.size() - 1; size >= 0; size--) {
                TextView label3 = label(recentLogLines.get(size), 12.0f, textPrimary(), false);
                label3.setLineSpacing(dp(3.0f), 1.0f);
                card5.addView(label3, margin(2, 9, 2, 9));
                if (size > 0) {
                    addSeparator(card5);
                }
            }
        }
        vertical2.addView(card5, margin(0, 0, 0, 14));
        addPair(linearLayout, vertical, vertical2);
        TextView label4 = label(getString(R.string.text_077), 11.0f, textSecondary(), false);
        label4.setLineSpacing(dp(3.0f), 1.0f);
        linearLayout.addView(label4, margin(3, 0, 3, 8));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildQueuePage$21(View view) {
        performTapFeedback(view);
        this.jobCancelRequested = true;
        systemToast(getString(R.string.text_069), 1).show();
    }

    private void buildPreviewPage(LinearLayout linearLayout) {
        String string;
        Runnable mainActivity$$ExternalSyntheticLambda112;
        Runnable mainActivity$$ExternalSyntheticLambda114;
        pageAppBar(linearLayout, getString(R.string.text_078));
        if (this.selectedDocumentUri == null) {
            LinearLayout card = card();
            card.setGravity(1);
            card.setPadding(dp(22.0f), dp(26.0f), dp(22.0f), dp(22.0f));
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setGravity(17);
            linearLayout2.setBackground(flat(iconWell(), 18));
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.ic_doc);
            imageView.setColorFilter(tonalText(), PorterDuff.Mode.SRC_IN);
            linearLayout2.addView(imageView, new LinearLayout.LayoutParams(dp(26.0f), dp(26.0f)));
            card.addView(linearLayout2, new LinearLayout.LayoutParams(dp(54.0f), dp(54.0f)));
            card.addView(label(getString(R.string.text_079), 17.0f, textPrimary(), true), margin(0, 14, 0, 6));
            card.addView(label(getString(R.string.text_080), 12.0f, textSecondary(), false), margin(0, 0, 0, 15));
            Button bigPrimaryButton = bigPrimaryButton(getString(R.string.text_081));
            bigPrimaryButton.setMinHeight(dp(48.0f));
            bigPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda40
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildPreviewPage$22(view);
                }
            });
            card.addView(bigPrimaryButton, new LinearLayout.LayoutParams(-1, dp(48.0f)));
            linearLayout.addView(card);
            return;
        }
        DocumentRenderer documentRenderer = this.documentRenderer;
        boolean z = documentRenderer != null && documentRenderer.isSupported();
        DocumentRenderer documentRenderer2 = this.documentRenderer;
        String formatLabel = documentRenderer2 == null ? this.selectedDocumentMime : documentRenderer2.getFormatLabel();
        int pageCount = z ? this.documentRenderer.getPageCount() : 0;
        LinearLayout card2 = card();
        LinearLayout horizontal = horizontal();
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setGravity(17);
        linearLayout3.setBackground(verticalGradient(primaryColor(), primaryDeep(), 15));
        ImageView imageView2 = new ImageView(this);
        DocumentRenderer documentRenderer3 = this.documentRenderer;
        imageView2.setImageResource((documentRenderer3 == null || !documentRenderer3.isImage()) ? R.drawable.ic_doc : R.drawable.ic_image);
        imageView2.setColorFilter(-1, PorterDuff.Mode.SRC_IN);
        linearLayout3.addView(imageView2, new LinearLayout.LayoutParams(dp(24.0f), dp(24.0f)));
        horizontal.addView(linearLayout3, new LinearLayout.LayoutParams(dp(46.0f), dp(46.0f)));
        LinearLayout vertical = vertical();
        vertical.setPadding(dp(11.0f), 0, dp(4.0f), 0);
        TextView label = label(this.selectedDocumentName, 13.5f, textPrimary(), true);
        label.setTextAlignment(5);
        label.setMaxLines(2);
        label.setEllipsize(TextUtils.TruncateAt.END);
        vertical.addView(label, margin(0, 0, 0, 4));
        if (z) {
            string = pageCount + getString(R.string.text_055) + formatLabel;
        } else {
            string = getString(R.string.text_082);
        }
        TextView label2 = label(string, 11.0f, z ? okText() : warnText(), true);
        label2.setTextAlignment(5);
        label2.setMaxLines(1);
        label2.setEllipsize(TextUtils.TruncateAt.END);
        vertical.addView(label2);
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView label3 = label(getString(R.string.text_083), 12.0f, tonalText(), true);
        label3.setPadding(dp(8.0f), dp(8.0f), 0, dp(8.0f));
        label3.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda43
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildPreviewPage$23(view);
            }
        });
        horizontal.addView(label3);
        card2.addView(horizontal, margin(0, 0, 0, 13));
        if (z) {
            ArrayList<SheetJob> previewPlan = previewPlan();
            int size = previewPlan.size();
            if (this.currentPreviewPage >= size) {
                this.currentPreviewPage = Math.max(0, size - 1);
            }
            LinearLayout horizontal2 = horizontal();
            boolean equals = "size".equals(this.layoutMode);
            StringBuilder sb = new StringBuilder();
            sb.append(getString(equals ? R.string.text_084 : R.string.text_085));
            sb.append(this.currentPreviewPage + 1);
            sb.append(getString(R.string.text_016));
            sb.append(size);
            horizontal2.addView(label(sb.toString(), 10.5f, tonalText(), true), new LinearLayout.LayoutParams(0, -2, 1.0f));
            horizontal2.addView(label(this.paperSize + " · " + modeLabel(), 10.5f, textSecondary(), true));
            card2.addView(horizontal2, margin(0, 0, 0, 10));
            FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.setBackground(flat(this.darkMode ? -14013905 : -855051, 8));
            frameLayout.setPadding(dp(9.0f), dp(9.0f), dp(9.0f), dp(9.0f));
            ImageView imageView3 = new ImageView(this);
            imageView3.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView3.setAdjustViewBounds(true);
            imageView3.setContentDescription(getString(R.string.text_086) + (this.currentPreviewPage + 1));
            try {
                Bitmap renderPreviewSheet = renderPreviewSheet(buildSheetSpec(dp(315.0f)), previewPlan.get(this.currentPreviewPage));
                Bitmap bitmap = this.currentPreviewBitmap;
                this.currentPreviewBitmap = renderPreviewSheet;
                imageView3.setImageBitmap(renderPreviewSheet);
                if (bitmap != null && bitmap != renderPreviewSheet && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception e) {
                imageView3.setImageDrawable(null);
                lambda$append$117(getString(R.string.text_087) + e.getMessage());
            }
            frameLayout.addView(imageView3, new FrameLayout.LayoutParams(-1, dp(360.0f), 17));
            card2.addView(frameLayout, margin(0, 0, 0, 12));
            LinearLayout horizontal3 = horizontal();
            Button button = tonalButton(getString(R.string.text_088), 13.0f);
            button.setMinHeight(dp(43.0f));
            button.setEnabled(this.currentPreviewPage > 0);
            button.setAlpha(this.currentPreviewPage > 0 ? 1.0f : 0.5f);
            button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda44
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildPreviewPage$24(view);
                }
            });
            horizontal3.addView(button, new LinearLayout.LayoutParams(0, dp(43.0f), 1.0f));
            Button button2 = tonalButton(getString(R.string.text_089), 13.0f);
            button2.setMinHeight(dp(43.0f));
            final int size2 = previewPlan.size();
            int i = size2 - 1;
            button2.setEnabled(this.currentPreviewPage < i);
            button2.setAlpha(this.currentPreviewPage < i ? 1.0f : 0.5f);
            button2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda45
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildPreviewPage$25(size2, view);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(43.0f), 1.0f);
            layoutParams.setMarginStart(dp(9.0f));
            horizontal3.addView(button2, layoutParams);
            card2.addView(horizontal3);
        } else {
            TextView label4 = label(getString(R.string.text_090), 12.0f, textSecondary(), false);
            label4.setLineSpacing(dp(4.0f), 1.0f);
            card2.addView(label4, margin(0, 0, 0, 13));
            Button bigPrimaryButton2 = bigPrimaryButton(getString(R.string.text_091));
            bigPrimaryButton2.setMinHeight(dp(46.0f));
            bigPrimaryButton2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda46
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildPreviewPage$26(view);
                }
            });
            card2.addView(bigPrimaryButton2, new LinearLayout.LayoutParams(-1, dp(46.0f)));
        }
        if (z) {
            LinearLayout card3 = card();
            card3.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
            boolean equals2 = "booklet".equals(this.layoutMode);
            addOptionRow(card3, getString(R.string.text_092), this.paperSize, new MainActivity$$ExternalSyntheticLambda111(this));
            addSeparator(card3);
            String string2 = getString(R.string.text_093);
            String string3 = equals2 ? getString(R.string.text_094) : String.valueOf(this.copies);
            if (equals2) {
                mainActivity$$ExternalSyntheticLambda112 = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda47
                    @Override // java.lang.Runnable
                    public final void run() {
                        MainActivity.this.lambda$buildPreviewPage$27();
                    }
                };
            } else {
                mainActivity$$ExternalSyntheticLambda112 = new MainActivity$$ExternalSyntheticLambda112(this);
            }
            addOptionRow(card3, string2, string3, mainActivity$$ExternalSyntheticLambda112);
            addSeparator(card3);
            addOptionRow(card3, getString(R.string.text_096), pageRangeSummaryAr(), new MainActivity$$ExternalSyntheticLambda113(this));
            addSeparator(card3);
            String string4 = getString(R.string.text_097);
            String string5 = equals2 ? getString(R.string.text_098) : duplexLabel();
            if (equals2) {
                mainActivity$$ExternalSyntheticLambda114 = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda49
                    @Override // java.lang.Runnable
                    public final void run() {
                        MainActivity.this.lambda$buildPreviewPage$28();
                    }
                };
            } else {
                mainActivity$$ExternalSyntheticLambda114 = new MainActivity$$ExternalSyntheticLambda114(this);
            }
            addOptionRow(card3, string4, string5, mainActivity$$ExternalSyntheticLambda114);
            addSeparator(card3);
            addOptionRow(card3, getString(R.string.text_100), layoutSummary(), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda41
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$buildPreviewPage$29();
                }
            });
            addSeparator(card3);
            addOptionRow(card3, getString(R.string.text_101), qualityLabel(), new MainActivity$$ExternalSyntheticLambda116(this));
            Button bigPrimaryButton3 = bigPrimaryButton(getString(this.printInProgress ? R.string.text_102 : R.string.text_036));
            bigPrimaryButton3.setEnabled(!this.printInProgress);
            bigPrimaryButton3.setAlpha(this.printInProgress ? 0.6f : 1.0f);
            bigPrimaryButton3.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda42
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildPreviewPage$30(view);
                }
            });
            TextView label5 = label(getString(R.string.text_103), 11.0f, textSecondary(), false);
            label5.setLineSpacing(dp(3.0f), 1.0f);
            LinearLayout vertical2 = vertical();
            vertical2.addView(card3, margin(0, 0, 0, 13));
            vertical2.addView(bigPrimaryButton3, new LinearLayout.LayoutParams(-1, dp(56.0f)));
            vertical2.addView(label5, margin(3, 12, 3, 4));
            addPair(linearLayout, card2, vertical2);
            return;
        }
        linearLayout.addView(card2, margin(0, 0, 0, 13));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$22(View view) {
        performTapFeedback(view);
        openDocumentPicker(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$23(View view) {
        performTapFeedback(view);
        openDocumentPicker(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$24(View view) {
        this.currentPreviewPage = Math.max(0, this.currentPreviewPage - 1);
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$25(int i, View view) {
        this.currentPreviewPage = Math.min(i - 1, this.currentPreviewPage + 1);
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$26(View view) {
        openSelectedFileExternally();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$27() {
        Toast.makeText(this, getString(R.string.text_095), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$28() {
        Toast.makeText(this, getString(R.string.text_099), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$29() {
        openPage("layout");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildPreviewPage$30(View view) {
        performTapFeedback(view);
        printSelectedDocument();
    }

    private String duplexLabel() {
        return "manual-long".equals(this.duplexMode) ? getString(R.string.text_104) : "manual-short".equals(this.duplexMode) ? getString(R.string.text_105) : getString(R.string.text_106);
    }

    private String qualityLabel() {
        return getString(this.draftMode ? R.string.text_107 : this.printDensity >= 4 ? R.string.text_108 : R.string.text_109);
    }

    private String pageRangeSummaryAr() {
        String str = this.pageRange;
        return (str == null || str.trim().isEmpty() || "all".equalsIgnoreCase(this.pageRange.trim())) ? getString(R.string.text_110) : this.pageRange.trim();
    }

    private void addOptionRow(LinearLayout linearLayout, String str, String str2, Runnable runnable) {
        addOptionRow(linearLayout, str, str2, runnable, true);
    }

    private void addOptionRow(LinearLayout linearLayout, String str, String str2, final Runnable runnable, boolean z) {
        LinearLayout horizontal = horizontal();
        horizontal.setPadding(0, dp(13.0f), 0, dp(13.0f));
        horizontal.addView(label(str, 16.0f, textPrimary(), true), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView label = label(str2, 13.0f, textSecondary(), false);
        label.setMaxLines(1);
        horizontal.addView(label);
        if (z) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.ic_chevron_left);
            imageView.setScaleX(isArabic() ? 1.0f : -1.0f);
            imageView.setColorFilter(textSecondary(), PorterDuff.Mode.SRC_IN);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(18.0f), dp(18.0f));
            layoutParams.setMarginStart(dp(6.0f));
            horizontal.addView(imageView, layoutParams);
        }
        horizontal.setClickable(true);
        horizontal.setFocusable(true);
        horizontal.setHapticFeedbackEnabled(this.hapticsEnabled);
        horizontal.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda74
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$addOptionRow$31(runnable, view);
            }
        });
        linearLayout.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$addOptionRow$31(Runnable runnable, View view) {
        performTapFeedback(view);
        runnable.run();
    }

    private AlertDialog.Builder dialogBuilder() {
        return new AlertDialog.Builder(new ContextThemeWrapper(this, this.darkMode ? android.R.style.Theme_Material_NoActionBar : android.R.style.Theme_Material_Light_NoActionBar));
    }

    private void showSelectionDialog(String str, String[] strArr, int i, final IndexAction indexAction) {
        final Dialog dialog = new Dialog(this);
        LinearLayout vertical = vertical();
        vertical.setBackground(flat(this.darkMode ? -14408149 : -1, 28));
        vertical.setPadding(dp(8.0f), dp(20.0f), dp(8.0f), dp(8.0f));
        vertical.addView(label(str, 17.5f, this.darkMode ? -855308 : -15000805, true), margin(18, 0, 18, 6));
        int i2 = 0;
        while (i2 < strArr.length) {
            final int i2f = i2;
            LinearLayout horizontal = horizontal();
            horizontal.setGravity(16);
            horizontal.setPadding(dp(18.0f), dp(12.0f), dp(18.0f), dp(12.0f));
            horizontal.addView(radioDot(i2 == i));
            View label = label(strArr[i2], 14.5f, this.darkMode ? -855308 : -15000805, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.setMarginStart(dp(14.0f));
            horizontal.addView(label, layoutParams);
            horizontal.setClickable(true);
            horizontal.setFocusable(true);
            horizontal.setHapticFeedbackEnabled(this.hapticsEnabled);
            horizontal.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda54
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$showSelectionDialog$32(indexAction, i2f, dialog, view);
                }
            });
            vertical.addView(horizontal);
            i2++;
        }
        TextView label2 = label(getString(R.string.text_111), 14.0f, this.darkMode ? -11756801 : -16747810, true);
        label2.setGravity(17);
        label2.setPadding(dp(10.0f), dp(10.0f), dp(10.0f), dp(10.0f));
        LinearLayout horizontal2 = horizontal();
        horizontal2.setGravity(17);
        horizontal2.addView(label2);
        horizontal2.setClickable(true);
        horizontal2.setFocusable(true);
        horizontal2.setHapticFeedbackEnabled(this.hapticsEnabled);
        horizontal2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda55
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$showSelectionDialog$33(dialog, view);
            }
        });
        vertical.addView(horizontal2, margin(0, 6, 0, 4));
        dialog.setContentView(vertical);
        styleDialogWindow(dialog);
        dialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showSelectionDialog$32(IndexAction indexAction, int i, Dialog dialog, View view) {
        performTapFeedback(view);
        indexAction.onChosen(i);
        dialog.dismiss();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showSelectionDialog$33(Dialog dialog, View view) {
        performTapFeedback(view);
        dialog.dismiss();
    }

    private View radioDot(boolean z) {
        FrameLayout frameLayout = new FrameLayout(this);
        int i = this.darkMode ? -11756801 : -16747810;
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(1);
        gradientDrawable.setColor(0);
        gradientDrawable.setStroke(dp(2.0f), z ? i : -6645086);
        frameLayout.setBackground(gradientDrawable);
        if (z) {
            View view = new View(this);
            GradientDrawable gradientDrawable2 = new GradientDrawable();
            gradientDrawable2.setShape(1);
            gradientDrawable2.setColor(i);
            view.setBackground(gradientDrawable2);
            frameLayout.addView(view, new FrameLayout.LayoutParams(dp(11.0f), dp(11.0f), 17));
        }
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(dp(22.0f), dp(22.0f)));
        return frameLayout;
    }

    private void styleDialogWindow(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.getDecorView().setLayoutDirection(appDirection());
        window.addFlags(2);
        window.setDimAmount(0.45f);
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.setLayout(dp(330.0f), -2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPaperSizeOptions() {
        showSelectionDialog(getString(R.string.text_092), new String[]{getString(R.string.text_112), getString(R.string.text_113), getString(R.string.text_114), getString(R.string.text_115), getString(R.string.text_116)}, Math.max(0, Arrays.asList(this.paperSizes).indexOf(this.paperSize)), new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda12
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i) {
                MainActivity.this.lambda$showPaperSizeOptions$34(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPaperSizeOptions$34(int i) {
        this.paperSize = this.paperSizes[i];
        this.printPreferences.edit().putString("paper_size", this.paperSize).apply();
    }

    private Dialog oneUIFormDialog(String str, String str2, View view, final FormApply formApply) {
        final Dialog dialog = new Dialog(this);
        LinearLayout vertical = vertical();
        vertical.setBackground(flat(this.darkMode ? -14408149 : -1, 28));
        vertical.setPadding(dp(8.0f), dp(20.0f), dp(8.0f), dp(8.0f));
        vertical.addView(label(str, 17.5f, this.darkMode ? -855308 : -15000805, true), margin(18, 0, 18, str2 != null ? 2 : 8));
        if (str2 != null) {
            vertical.addView(label(str2, 12.5f, textSecondary(), false), margin(18, 0, 18, 8));
        }
        vertical.addView(view, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout horizontal = horizontal();
        TextView dialogButton = dialogButton(getString(R.string.text_117), true);
        TextView dialogButton2 = dialogButton(getString(R.string.text_111), false);
        dialogButton.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda121
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                MainActivity.this.lambda$oneUIFormDialog$35(formApply, dialog, view2);
            }
        });
        dialogButton2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda122
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                MainActivity.this.lambda$oneUIFormDialog$36(dialog, view2);
            }
        });
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(44.0f), 1.0f);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(44.0f), 1.0f);
        layoutParams2.setMarginStart(dp(8.0f));
        horizontal.addView(dialogButton, layoutParams);
        horizontal.addView(dialogButton2, layoutParams2);
        vertical.addView(horizontal, margin(10, 12, 10, 4));
        dialog.setContentView(vertical);
        styleDialogWindow(dialog);
        dialog.show();
        return dialog;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$oneUIFormDialog$35(FormApply formApply, Dialog dialog, View view) {
        performTapFeedback(view);
        if (formApply == null || formApply.apply()) {
            dialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$oneUIFormDialog$36(Dialog dialog, View view) {
        performTapFeedback(view);
        dialog.dismiss();
    }

    private TextView dialogButton(String str, boolean z) {
        int i;
        int i2;
        if (z) {
            i = -1;
        } else {
            i = this.darkMode ? -855308 : -15000805;
        }
        TextView label = label(str, 14.0f, i, true);
        label.setGravity(17);
        if (z) {
            i2 = this.darkMode ? -13734476 : -16747810;
        } else {
            i2 = this.darkMode ? -12960702 : -1447442;
        }
        label.setBackground(flat(i2, 22));
        label.setClickable(true);
        label.setFocusable(true);
        label.setHapticFeedbackEnabled(this.hapticsEnabled);
        return label;
    }

    private void styleNumberPickerForSheet(NumberPicker numberPicker) {
        int textPrimary = textPrimary();
        if (Build.VERSION.SDK_INT >= 29) {
            numberPicker.setTextColor(textPrimary);
        } else {
            try {
                Field declaredField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
                declaredField.setAccessible(true);
                ((Paint) declaredField.get(numberPicker)).setColor(textPrimary);
            } catch (Exception unused) {
            }
            for (int i = 0; i < numberPicker.getChildCount(); i++) {
                View childAt = numberPicker.getChildAt(i);
                if (childAt instanceof EditText) {
                    ((EditText) childAt).setTextColor(textPrimary);
                }
            }
        }
        try {
            Field declaredField2 = NumberPicker.class.getDeclaredField("mSelectionDivider");
            declaredField2.setAccessible(true);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(this.darkMode ? -11512996 : -2302748);
            gradientDrawable.setSize(-1, dp(1.0f));
            declaredField2.set(numberPicker, gradientDrawable);
        } catch (Exception unused2) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showCopiesOptions() {
        LinearLayout vertical = vertical();
        vertical.setPadding(dp(14.0f), dp(4.0f), dp(14.0f), dp(0.0f));
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(99);
        numberPicker.setValue(Math.max(1, Math.min(this.copies, 99)));
        styleNumberPickerForSheet(numberPicker);
        vertical.addView(numberPicker, new LinearLayout.LayoutParams(-1, dp(132.0f)));
        oneUIFormDialog(getString(R.string.text_093), getString(R.string.text_118), vertical, new FormApply() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda29
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.FormApply
            public final boolean apply() {
                boolean lambda$showCopiesOptions$37;
                lambda$showCopiesOptions$37 = MainActivity.this.lambda$showCopiesOptions$37(numberPicker);
                return lambda$showCopiesOptions$37;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$showCopiesOptions$37(NumberPicker numberPicker) {
        this.copies = numberPicker.getValue();
        this.printPreferences.edit().putInt("copies", this.copies).apply();
        renderApp();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPageRangeOptions() {
        DocumentRenderer documentRenderer = this.documentRenderer;
        if (documentRenderer == null || !documentRenderer.isSupported()) {
            systemToast(getString(R.string.text_119), 0).show();
            return;
        }
        LinearLayout vertical = vertical();
        vertical.setPadding(dp(14.0f), dp(0.0f), dp(14.0f), dp(0.0f));
        TextView label = label(getString(R.string.text_120), 12.0f, textSecondary(), false);
        label.setLineSpacing(dp(3.0f), 1.0f);
        vertical.addView(label, margin(4, 0, 4, 10));
        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setTextSize(15.0f);
        editText.setInputType(524289);
        editText.setHint(getString(R.string.text_121));
        editText.setHintTextColor(textSecondary());
        editText.setTextColor(textPrimary());
        editText.setBackground(flat(this.darkMode ? -13552839 : -986892, 14));
        editText.setPadding(dp(14.0f), 0, dp(14.0f), 0);
        editText.setText("all".equalsIgnoreCase(this.pageRange) ? "" : this.pageRange);
        editText.setSelection(editText.getText().length());
        vertical.addView(editText, new LinearLayout.LayoutParams(-1, dp(52.0f)));
        TextView label2 = label(getString(R.string.text_122), 13.5f, this.darkMode ? -11756801 : -16747810, true);
        label2.setPadding(dp(4.0f), dp(10.0f), dp(4.0f), dp(2.0f));
        vertical.addView(label2, new LinearLayout.LayoutParams(-2, -2));
        final Dialog[] dialogArr = {oneUIFormDialog(getString(R.string.text_123), null, vertical, new FormApply() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda27
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.FormApply
            public final boolean apply() {
                boolean lambda$showPageRangeOptions$38;
                lambda$showPageRangeOptions$38 = MainActivity.this.lambda$showPageRangeOptions$38(editText);
                return lambda$showPageRangeOptions$38;
            }
        })};
        label2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda28
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$showPageRangeOptions$39(dialogArr, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$showPageRangeOptions$38(EditText editText) {
        String trim = editText.getText().toString().trim();
        if (trim.isEmpty()) {
            trim = "all";
        }
        try {
            resolvePageRange(trim, this.documentRenderer.getPageCount());
            this.pageRange = trim.equalsIgnoreCase("all") ? "all" : trim.replaceAll("\\s+", "");
            this.printPreferences.edit().putString("page_range", this.pageRange).apply();
            renderApp();
            return true;
        } catch (IllegalArgumentException e) {
            systemToast(e.getMessage(), 1).show();
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPageRangeOptions$39(Dialog[] dialogArr, View view) {
        performTapFeedback(view);
        this.pageRange = "all";
        this.printPreferences.edit().putString("page_range", this.pageRange).apply();
        dialogArr[0].dismiss();
        renderApp();
    }

    private ArrayList<Integer> resolvePageRange(String str, int i) {
        if (i < 1) {
            throw new IllegalArgumentException(getString(R.string.text_124));
        }
        ArrayList<Integer> arrayList = new ArrayList<>();
        if (str != null && !str.trim().isEmpty() && !"all".equalsIgnoreCase(str.trim())) {
            for (String part : str.split(",", -1)) {
                String trim = part.trim();
                if (trim.isEmpty()) {
                    throw new IllegalArgumentException(getString(R.string.text_125));
                }
                int indexOf = trim.indexOf(45);
                int parseInt;
                int i2;
                try {
                    if (indexOf < 0) {
                        parseInt = Integer.parseInt(trim);
                        i2 = parseInt;
                    } else {
                        int i5 = indexOf + 1;
                        if (trim.indexOf(45, i5) >= 0) {
                            throw new NumberFormatException();
                        }
                        parseInt = Integer.parseInt(trim.substring(0, indexOf).trim());
                        i2 = Integer.parseInt(trim.substring(i5).trim());
                    }
                } catch (NumberFormatException unused) {
                    throw new IllegalArgumentException(getString(R.string.text_126));
                }
                if (parseInt < 1 || i2 < parseInt || i2 > i) {
                    throw new IllegalArgumentException(getString(R.string.text_127) + i + ".");
                }
                while (parseInt <= i2) {
                    Integer valueOf = Integer.valueOf(parseInt - 1);
                    if (!arrayList.contains(valueOf)) {
                        arrayList.add(valueOf);
                    }
                    parseInt++;
                }
            }
            if (arrayList.isEmpty()) {
                throw new IllegalArgumentException(getString(R.string.text_128));
            }
            return arrayList;
        }
        for (int k = 0; k < i; k++) {
            arrayList.add(Integer.valueOf(k));
        }
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDuplexOptions() {
        showSelectionDialog(getString(R.string.text_097), new String[]{getString(R.string.text_129), getString(R.string.text_130), getString(R.string.text_131)}, "manual-long".equals(this.duplexMode) ? 1 : "manual-short".equals(this.duplexMode) ? 2 : 0, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda89
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i) {
                MainActivity.this.lambda$showDuplexOptions$40(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showDuplexOptions$40(int i) {
        String str;
        if (i == 1) {
            str = "manual-long";
        } else {
            str = i == 2 ? "manual-short" : DebugKt.DEBUG_PROPERTY_VALUE_OFF;
        }
        this.duplexMode = str;
        this.printPreferences.edit().putString("duplex_mode", this.duplexMode).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showQualityOptions() {
        showSelectionDialog(getString(R.string.text_101), new String[]{getString(R.string.text_132), getString(R.string.text_133), getString(R.string.text_108)}, this.draftMode ? 0 : this.printDensity >= 4 ? 2 : 1, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda118
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i) {
                MainActivity.this.lambda$showQualityOptions$41(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showQualityOptions$41(int i) {
        this.draftMode = i == 0;
        this.printDensity = i == 0 ? 2 : i == 1 ? 3 : 5;
        this.printPreferences.edit().putBoolean("draft_mode", this.draftMode).putInt("density", this.printDensity).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showThemeOptions() {
        showSelectionDialog(getString(R.string.text_134), new String[]{getString(R.string.text_031), getString(R.string.text_030), getString(R.string.text_029)}, "light".equals(this.themeMode) ? 1 : "dark".equals(this.themeMode) ? 2 : 0, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda38
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i) {
                MainActivity.this.lambda$showThemeOptions$42(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showThemeOptions$42(int i) {
        String str;
        if (i == 1) {
            str = "light";
        } else {
            str = i == 2 ? "dark" : "system";
        }
        this.themeMode = str;
        this.uiPreferences.edit().putString("theme_mode", this.themeMode).apply();
        applyThemeMode();
        renderApp();
    }

    private void openSelectedFileExternally() {
        String str;
        if (this.selectedDocumentUri == null) {
            return;
        }
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            Uri uri = this.selectedDocumentUri;
            String str2 = this.selectedDocumentMime;
            str = (str2 == null || str2.isEmpty()) ? "*/*" : str2;
            intent.setDataAndType(uri, str);
            intent.addFlags(1);
            startActivity(Intent.createChooser(intent, getString(R.string.text_135)));
        } catch (Exception unused) {
            systemToast(getString(R.string.text_136), 1).show();
        }
    }

    private void buildSetupPage(LinearLayout linearLayout) {
        String string;
        float f;
        pageAppBar(linearLayout, getString(R.string.text_049));
        boolean hasPrinterConnected = hasPrinterConnected();
        LinearLayout card = card();
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(16);
        LinearLayout vertical = vertical();
        vertical.addView(label("HP LaserJet P1005", 16.5f, textPrimary(), true));
        if (hasPrinterConnected) {
            string = getString(R.string.text_137);
        } else {
            string = getString(R.string.text_138);
        }
        vertical.addView(label(string, 12.0f, textSecondary(), false), margin(0, 3, 0, 0));
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.p1005_photo);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setContentDescription(getString(R.string.text_043));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(118.0f), dp(104.0f));
        layoutParams.setMargins(0, 0, dp(14.0f), 0);
        horizontal.addView(imageView, layoutParams);
        card.addView(horizontal);
        linearLayout.addView(card, margin(0, 0, 0, 16));
        if (this.setupDone) {
            LinearLayout vertical2 = vertical();
            vertical2.setGravity(1);
            vertical2.setPadding(dp(20.0f), dp(26.0f), dp(20.0f), dp(20.0f));
            vertical2.setBackground(flat(okBg(), 26));
            TextView label = label("✓", 30.0f, -1, true);
            label.setGravity(17);
            label.setBackground(flat(okText(), 30));
            vertical2.addView(label, new LinearLayout.LayoutParams(dp(60.0f), dp(60.0f)));
            vertical2.addView(label(getString(R.string.text_139), 18.0f, this.darkMode ? textPrimary() : -15836620, true), margin(0, 16, 0, 6));
            TextView label2 = label(getString(R.string.text_140), 12.5f, this.darkMode ? textSecondary() : -14718141, false);
            label2.setGravity(17);
            label2.setLineSpacing(dp(3.0f), 1.0f);
            vertical2.addView(label2);
            linearLayout.addView(vertical2, margin(0, 0, 0, 14));
            LinearLayout card2 = card();
            card2.setPadding(dp(16.0f), dp(8.0f), dp(16.0f), dp(8.0f));
            addSetupResultRow(card2, getString(R.string.text_141), getString(R.string.text_142));
            addSeparator(card2);
            addSetupResultRow(card2, getString(R.string.text_143), getString(R.string.text_144));
            addSeparator(card2);
            LinearLayout horizontal2 = horizontal();
            horizontal2.setPadding(0, dp(12.0f), 0, dp(12.0f));
            LinearLayout vertical3 = vertical();
            vertical3.addView(label(getString(R.string.text_145), 14.0f, textPrimary(), true), margin(0, 0, 0, 4));
            vertical3.addView(label(getString(R.string.text_146), 11.5f, textSecondary(), false));
            horizontal2.addView(vertical3, new LinearLayout.LayoutParams(0, -2, 1.0f));
            TextView label3 = label(getString(R.string.text_036), 13.0f, tonalText(), true);
            label3.setPadding(dp(10.0f), dp(8.0f), 0, dp(8.0f));
            label3.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda94
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildSetupPage$43(view);
                }
            });
            horizontal2.addView(label3);
            card2.addView(horizontal2);
            linearLayout.addView(card2, margin(0, 0, 0, 14));
            Button bigPrimaryButton = bigPrimaryButton(getString(R.string.text_147));
            bigPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda95
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$buildSetupPage$44(view);
                }
            });
            linearLayout.addView(bigPrimaryButton, new LinearLayout.LayoutParams(-1, dp(56.0f)));
            return;
        }
        if (this.setupFinishedWithWarning) {
            LinearLayout vertical4 = vertical();
            vertical4.setGravity(1);
            vertical4.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(18.0f));
            vertical4.setBackground(flat(warnBg(), 26));
            vertical4.addView(label(getString(R.string.text_148), 17.0f, warnText(), true), margin(0, 0, 0, 6));
            TextView label4 = label(getString(R.string.text_149), 12.0f, textSecondary(), false);
            label4.setGravity(17);
            f = 1.0f;
            label4.setLineSpacing(dp(3.0f), 1.0f);
            vertical4.addView(label4, margin(0, 0, 0, 0));
            linearLayout.addView(vertical4, margin(0, 0, 0, 14));
        } else {
            f = 1.0f;
        }
        linearLayout.addView(sectionLabel(getString(R.string.text_150)), margin(2, 0, 0, 8));
        LinearLayout card3 = card();
        card3.setPadding(dp(14.0f), dp(8.0f), dp(14.0f), dp(8.0f));
        addSetupStepRow(card3, 0, "1", getString(R.string.text_141), getString(R.string.text_151));
        addSeparator(card3);
        addSetupStepRow(card3, 1, "2", getString(R.string.text_143), getString(R.string.text_152));
        addSeparator(card3);
        addSetupStepRow(card3, 2, "3", getString(R.string.text_145), getString(R.string.text_153));
        linearLayout.addView(card3, margin(0, 0, 0, 16));
        Button bigPrimaryButton2 = bigPrimaryButton(getString(this.setupRunning ? R.string.text_154 : R.string.text_155));
        bigPrimaryButton2.setEnabled(!this.setupRunning);
        if (this.setupRunning) {
            f = 0.6f;
        }
        bigPrimaryButton2.setAlpha(f);
        bigPrimaryButton2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda96
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildSetupPage$45(view);
            }
        });
        linearLayout.addView(bigPrimaryButton2, new LinearLayout.LayoutParams(-1, dp(56.0f)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSetupPage$43(View view) {
        performTapFeedback(view);
        printBuiltInTestPage();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSetupPage$44(View view) {
        performTapFeedback(view);
        openPage("home");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSetupPage$45(View view) {
        performTapFeedback(view);
        startSetupFlow();
    }

    private void addSetupStepRow(LinearLayout linearLayout, int i, String str, String str2, String str3) {
        TextView label;
        LinearLayout horizontal = horizontal();
        horizontal.setPadding(dp(2.0f), dp(11.0f), dp(2.0f), dp(11.0f));
        int i2 = this.setupSteps[i];
        if (i2 == 2) {
            label = label("✓", 15.0f, -1, true);
            label.setBackground(flat(okText(), 18));
        } else if (i2 == 1) {
            label = label("…", 15.0f, tonalText(), true);
            label.setBackground(flat(tonalBg(), 18));
        } else if (i2 == 3 || i2 == 4) {
            label = label("!", 15.0f, -1, true);
            label.setBackground(flat(i2 == 3 ? warnText() : -4046784, 18));
        } else {
            label = label(str, 13.0f, tonalText(), true);
            label.setBackground(flat(tonalBg(), 18));
        }
        label.setGravity(17);
        horizontal.addView(label, new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f)));
        LinearLayout vertical = vertical();
        vertical.setPadding(dp(12.0f), 0, dp(4.0f), 0);
        vertical.addView(label(str2, 14.0f, textPrimary(), true), margin(0, 0, 0, 3));
        TextView label2 = label(str3, 11.5f, textSecondary(), false);
        label2.setLineSpacing(dp(2.0f), 1.0f);
        vertical.addView(label2);
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (i2 == 1) {
            horizontal.addView(label(getString(R.string.text_156), 11.5f, tonalText(), true));
        } else if (i2 == 2) {
            horizontal.addView(label(getString(R.string.text_142), 11.5f, okText(), true));
        } else if (i2 == 3) {
            horizontal.addView(label(getString(R.string.text_157), 11.5f, warnText(), true));
        } else if (i2 == 4) {
            horizontal.addView(label(getString(R.string.text_158), 11.5f, -4046784, true));
        }
        linearLayout.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addSetupResultRow(LinearLayout linearLayout, String str, String str2) {
        LinearLayout horizontal = horizontal();
        horizontal.setPadding(dp(2.0f), dp(12.0f), dp(2.0f), dp(12.0f));
        TextView label = label("✓", 13.0f, -1, true);
        label.setGravity(17);
        label.setBackground(flat(okText(), 16));
        horizontal.addView(label, new LinearLayout.LayoutParams(dp(32.0f), dp(32.0f)));
        LinearLayout vertical = vertical();
        vertical.setPadding(dp(12.0f), 0, dp(4.0f), 0);
        vertical.addView(label(str, 14.0f, textPrimary(), true));
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        horizontal.addView(label(str2, 11.5f, okText(), true));
        linearLayout.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
    }

    private void onPrinterDetached() {
        this.autoPreparePermissionRequested = false;
        if (this.prepareState != 1) {
            this.prepareState = 0;
            this.prepareDetail = 0;
            this.prepareFailureReason = "";
            this.prepareReadyBannerDismissed = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleAutoPrepare(long j) {
        this.autoPrepareHandler.removeCallbacks(this.autoPrepareRunnable);
        this.autoPrepareHandler.postDelayed(this.autoPrepareRunnable, j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void maybeAutoPrepare() {
        if (this.setupRunning || this.prepareState == 1 || this.prepareState == 2) {
            return;
        }
        UsbDevice findConnectedPrinter = findConnectedPrinter();
        if (findConnectedPrinter == null) {
            return;
        }
        if (!this.usbManager.hasPermission(findConnectedPrinter)) {
            if (!this.autoPreparePermissionRequested) {
                this.autoPreparePermissionRequested = true;
                lambda$append$117(getString(R.string.text_461));
                this.usbManager.requestPermission(findConnectedPrinter, this.permissionIntent);
            }
            return;
        }
        startAutoPrepare(findConnectedPrinter);
    }

    private void retryAutoPrepare() {
        this.prepareState = 0;
        this.prepareDetail = 0;
        this.prepareFailureReason = "";
        renderApp();
        maybeAutoPrepare();
    }

    private void startAutoPrepare(final UsbDevice usbDevice) {
        this.prepareState = 1;
        this.prepareDetail = 1;
        this.prepareFailureReason = "";
        this.prepareReadyBannerDismissed = false;
        lambda$append$117(getString(R.string.text_445));
        renderApp();
        new Thread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.3
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.runAutoPrepare(usbDevice);
            }
        }, "P1005-Auto-Prepare").start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runAutoPrepare(UsbDevice usbDevice) {
        UsbPrinterIO.JOB_LOCK.lock();
        try {
            try {
                Thread.sleep(700L);
                FirmwareCheck checkFirmwareStatus = checkFirmwareStatus();
                if (checkFirmwareStatus.status == 1) {
                    finishAutoPrepare(true, getString(R.string.text_446), null);
                    return;
                }
                updatePrepareDetail(2);
                lambda$append$117(getString(R.string.text_447));
                sendBuiltInFirmware(usbDevice);
                FirmwareCheck firmwareCheck = null;
                for (int i = 0; i < 12; i++) {
                    Thread.sleep(2000L);
                    firmwareCheck = checkFirmwareStatus();
                    if (firmwareCheck.status == 1) {
                        break;
                    }
                }
                if (firmwareCheck != null && firmwareCheck.status == 1) {
                    finishAutoPrepare(true, getString(R.string.text_448), null);
                } else if (firmwareCheck != null && firmwareCheck.status == 2) {
                    finishAutoPrepare(false, null, getString(R.string.text_462));
                } else {
                    finishAutoPrepare(false, null, firmwareCheck == null ? getString(R.string.text_401) : firmwareCheck.detail);
                }
            } finally {
                UsbPrinterIO.JOB_LOCK.unlock();
            }
        } catch (Exception e) {
            finishAutoPrepare(false, null, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void updatePrepareDetail(final int i) {
        runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.4
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.prepareDetail = i;
                MainActivity.this.renderApp();
            }
        });
    }

    private void finishAutoPrepare(final boolean z, final String str, final String str2) {
        runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.5
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.prepareState = z ? 2 : 3;
                MainActivity.this.prepareDetail = 0;
                if (z) {
                    MainActivity.this.prepareFailureReason = "";
                    if (str != null) {
                        MainActivity.this.lambda$append$117(str);
                    }
                } else {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.prepareFailureReason = str2 == null ? "" : str2;
                    MainActivity.this.lambda$append$117(MainActivity.this.getString(R.string.text_449) + MainActivity.this.prepareFailureReason);
                }
                MainActivity.this.renderApp();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    static final class FirmwareCheck {
        String detail;
        int status;

        FirmwareCheck(int i, String str) {
            this.status = i;
            this.detail = str;
        }
    }

    private FirmwareCheck checkFirmwareStatus() {
        PrinterFirmware.Check check = PrinterFirmware.check(this, this.usbManager);
        return new FirmwareCheck(check.status, check.detail);
    }

    private void addPrepareBanner(LinearLayout linearLayout) {
        int i = this.prepareState;
        if (i == 1) {
            LinearLayout vertical = vertical();
            vertical.setPadding(dp(16.0f), dp(14.0f), dp(16.0f), dp(14.0f));
            vertical.setBackground(flat(warnBg(), 26));
            vertical.setClipToOutline(true);
            LinearLayout horizontal = horizontal();
            horizontal.setGravity(16);
            TextView label = label("!", 15.0f, -1, true);
            label.setGravity(17);
            label.setBackground(flat(warnText(), 18));
            horizontal.addView(label, new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f)));
            LinearLayout vertical2 = vertical();
            vertical2.setPadding(dp(12.0f), 0, dp(4.0f), 0);
            vertical2.addView(label(getString(R.string.text_453), 14.5f, textPrimary(), true), margin(0, 0, 0, 3));
            TextView label2 = label(getString(R.string.text_454), 11.5f, textSecondary(), false);
            label2.setLineSpacing(dp(2.0f), 1.0f);
            vertical2.addView(label2);
            horizontal.addView(vertical2, new LinearLayout.LayoutParams(0, -2, 1.0f));
            vertical.addView(horizontal);
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(true);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = dp(10.0f);
            vertical.addView(progressBar, layoutParams);
            TextView label3 = label(getString(this.prepareDetail == 2 ? R.string.text_460 : R.string.text_459), 11.5f, warnText(), true);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.topMargin = dp(6.0f);
            vertical.addView(label3, layoutParams2);
            linearLayout.addView(vertical, margin(0, 0, 0, 14));
            return;
        }
        if (i == 2 && !this.prepareReadyBannerDismissed) {
            LinearLayout vertical3 = vertical();
            vertical3.setPadding(dp(16.0f), dp(14.0f), dp(16.0f), dp(14.0f));
            vertical3.setBackground(flat(okBg(), 26));
            vertical3.setClipToOutline(true);
            LinearLayout horizontal2 = horizontal();
            horizontal2.setGravity(16);
            TextView label4 = label("\u2713", 15.0f, -1, true);
            label4.setGravity(17);
            label4.setBackground(flat(okText(), 18));
            horizontal2.addView(label4, new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f)));
            LinearLayout vertical4 = vertical();
            vertical4.setPadding(dp(12.0f), 0, dp(4.0f), 0);
            vertical4.addView(label(getString(R.string.text_455), 14.5f, textPrimary(), true), margin(0, 0, 0, 3));
            TextView label5 = label(getString(R.string.text_456), 11.5f, textSecondary(), false);
            label5.setLineSpacing(dp(2.0f), 1.0f);
            vertical4.addView(label5);
            horizontal2.addView(vertical4, new LinearLayout.LayoutParams(0, -2, 1.0f));
            vertical3.addView(horizontal2);
            vertical3.setClickable(true);
            vertical3.setFocusable(true);
            vertical3.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.6
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    MainActivity.this.performTapFeedback(view);
                    MainActivity.this.prepareReadyBannerDismissed = true;
                    MainActivity.this.renderApp();
                }
            });
            linearLayout.addView(vertical3, margin(0, 0, 0, 14));
            return;
        }
        if (i == 3) {
            LinearLayout vertical5 = vertical();
            vertical5.setPadding(dp(16.0f), dp(14.0f), dp(16.0f), dp(14.0f));
            vertical5.setBackground(flat(warnBg(), 26));
            vertical5.setClipToOutline(true);
            LinearLayout horizontal3 = horizontal();
            horizontal3.setGravity(16);
            TextView label6 = label("!", 15.0f, -1, true);
            label6.setGravity(17);
            label6.setBackground(flat(warnText(), 18));
            horizontal3.addView(label6, new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f)));
            LinearLayout vertical6 = vertical();
            vertical6.setPadding(dp(12.0f), 0, dp(4.0f), 0);
            vertical6.addView(label(getString(R.string.text_457), 14.5f, textPrimary(), true), margin(0, 0, 0, 3));
            TextView label7 = label(this.prepareFailureReason, 11.5f, textSecondary(), false);
            label7.setLineSpacing(dp(2.0f), 1.0f);
            vertical6.addView(label7);
            horizontal3.addView(vertical6, new LinearLayout.LayoutParams(0, -2, 1.0f));
            vertical5.addView(horizontal3);
            Button tonalButton = tonalButton(getString(R.string.text_458), 13.0f);
            tonalButton.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.7
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    MainActivity.this.performTapFeedback(view);
                    MainActivity.this.retryAutoPrepare();
                }
            });
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, dp(44.0f));
            layoutParams3.topMargin = dp(10.0f);
            vertical5.addView(tonalButton, layoutParams3);
            linearLayout.addView(vertical5, margin(0, 0, 0, 14));
        }
    }

    private void startSetupFlow() {
        if (this.setupRunning) {
            return;
        }
        UsbDevice findConnectedPrinter = findConnectedPrinter();
        if (findConnectedPrinter == null) {
            lambda$append$117(getString(R.string.text_159));
            systemToast(getString(R.string.text_160), 1).show();
            return;
        }
        if (!this.usbManager.hasPermission(findConnectedPrinter)) {
            lambda$append$117(getString(R.string.text_161));
            systemToast(getString(R.string.text_162), 1).show();
            scanDevices();
            return;
        }
        this.setupRunning = true;
        this.setupDone = false;
        this.setupFinishedWithWarning = false;
        int[] iArr = this.setupSteps;
        iArr[0] = 1;
        iArr[1] = 0;
        iArr[2] = 0;
        renderApp();
        lambda$append$117(getString(R.string.text_163));
        loadBuiltInFirmware(new UsbResultCallback() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda50
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.UsbResultCallback
            public final void onResult(boolean z, String str) {
                MainActivity.this.lambda$startSetupFlow$47(z, str);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startSetupFlow$47(boolean z, String str) {
        if (!z) {
            this.setupSteps[0] = 4;
            this.setupRunning = false;
            this.setupFinishedWithWarning = true;
            lambda$append$117(getString(R.string.text_164) + str);
            renderApp();
            return;
        }
        int[] iArr = this.setupSteps;
        iArr[0] = 2;
        iArr[1] = 1;
        renderApp();
        verifyPrinterFirmware(new UsbResultCallback() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda17
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.UsbResultCallback
            public final void onResult(boolean z2, String str2) {
                MainActivity.this.lambda$startSetupFlow$46(z2, str2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startSetupFlow$46(boolean z, String str) {
        this.setupSteps[1] = z ? 2 : 3;
        this.setupRunning = false;
        if (z) {
            this.setupDone = true;
            this.setupFinishedWithWarning = false;
            this.prepareState = 2;
            this.prepareReadyBannerDismissed = true;
            lambda$append$117(getString(R.string.text_165));
        } else {
            this.setupFinishedWithWarning = true;
            lambda$append$117(getString(R.string.text_166) + str);
        }
        renderApp();
    }

    private void buildSettingsPage(LinearLayout linearLayout) {
        pageAppBar(linearLayout, getString(R.string.text_039));
        LinearLayout card = card();
        card.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        addOptionRow(card, getString(R.string.text_167), getString(isArabic() ? R.string.language_arabic : R.string.language_english), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda99
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showLanguageOptions();
            }
        });
        addSeparator(card);
        addOptionRow(card, getString(R.string.text_134), themeModeLabel(), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda108
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showThemeOptions();
            }
        });
        View sectionLabel = sectionLabel(getString(R.string.text_170));
        LinearLayout card2 = card();
        card2.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        LinearLayout horizontal = horizontal();
        horizontal.setPadding(0, dp(13.0f), 0, dp(13.0f));
        LinearLayout vertical = vertical();
        vertical.addView(label(getString(R.string.text_141), 16.0f, textPrimary(), true), margin(0, 0, 0, 4));
        vertical.addView(label(getString(R.string.text_171), 13.0f, textSecondary(), false));
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_chevron_left);
        imageView.setScaleX(isArabic() ? 1.0f : -1.0f);
        imageView.setColorFilter(textSecondary(), PorterDuff.Mode.SRC_IN);
        horizontal.addView(imageView, new LinearLayout.LayoutParams(dp(18.0f), dp(18.0f)));
        horizontal.setClickable(true);
        horizontal.setFocusable(true);
        horizontal.setHapticFeedbackEnabled(this.hapticsEnabled);
        horizontal.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda110
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildSettingsPage$48(view);
            }
        });
        card2.addView(horizontal);
        View sectionLabel2 = sectionLabel(getString(R.string.text_172));
        LinearLayout card3 = card();
        card3.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        addOptionRow(card3, getString(R.string.text_092), this.paperSize, new MainActivity$$ExternalSyntheticLambda111(this));
        addSeparator(card3);
        String string = getString(R.string.text_093);
        Resources resources = getResources();
        int i = R.plurals.copy_count;
        int i2 = this.copies;
        addOptionRow(card3, string, resources.getQuantityString(i, i2, Integer.valueOf(i2)), new MainActivity$$ExternalSyntheticLambda112(this));
        addSeparator(card3);
        addOptionRow(card3, getString(R.string.text_096), pageRangeSummaryAr(), new MainActivity$$ExternalSyntheticLambda113(this));
        addSeparator(card3);
        addOptionRow(card3, getString(R.string.text_097), duplexLabel(), new MainActivity$$ExternalSyntheticLambda114(this));
        addSeparator(card3);
        addOptionRow(card3, getString(R.string.text_100), layoutSummary(), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda115
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildSettingsPage$49();
            }
        });
        addSeparator(card3);
        addOptionRow(card3, getString(R.string.text_101), qualityLabel(), new MainActivity$$ExternalSyntheticLambda116(this));
        View sectionLabel3 = sectionLabel(getString(R.string.text_174));
        LinearLayout card4 = card();
        card4.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        addSettingToggle(card4, getString(R.string.text_175), getString(R.string.text_176), this.hapticsEnabled, new ToggleAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda117
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.ToggleAction
            public final void onToggle(boolean z) {
                MainActivity.this.lambda$buildSettingsPage$50(z);
            }
        });
        addSeparator(card4);
        addSettingToggle(card4, getString(R.string.text_177), getString(R.string.text_178), this.keepScreenAwake, new ToggleAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda100
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.ToggleAction
            public final void onToggle(boolean z) {
                MainActivity.this.lambda$buildSettingsPage$51(z);
            }
        });
        addSeparator(card4);
        addSettingToggle(card4, getString(R.string.text_179), getString(R.string.text_180), this.confirmFirmwareLoad, new ToggleAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda101
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.ToggleAction
            public final void onToggle(boolean z) {
                MainActivity.this.lambda$buildSettingsPage$52(z);
            }
        });
        View sectionLabel4 = sectionLabel(getString(R.string.text_064));
        LinearLayout card5 = card();
        card5.setPadding(dp(16.0f), dp(6.0f), dp(16.0f), dp(6.0f));
        addToolRow(card5, getString(R.string.text_181), getString(R.string.text_182), getString(R.string.text_183), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda102
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.verifyPrinterFirmware();
            }
        });
        addSeparator(card5);
        addToolRow(card5, getString(R.string.text_184), getString(R.string.text_185), getString(R.string.text_186), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda103
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.requestFirmwareLoad();
            }
        });
        addSeparator(card5);
        addToolRow(card5, getString(R.string.text_187), getString(R.string.text_188), getString(R.string.text_189), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda104
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.selectFirmwareFile();
            }
        });
        TextView label = label(getString(R.string.text_190), 13.0f, textSecondary(), false);
        label.setLineSpacing(dp(4.0f), 1.0f);
        View sectionLabel5 = sectionLabel(getString(R.string.text_191));
        LinearLayout card6 = card();
        card6.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        addOptionRow(card6, getString(R.string.text_192), getString(R.string.text_193), new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda105
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildSettingsPage$53();
            }
        });
        addSeparator(card6);
        addOptionRow(card6, getString(R.string.text_194), "", new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda106
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildSettingsPage$54();
            }
        });
        addSeparator(card6);
        addOptionRow(card6, getString(R.string.text_040), "", new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda107
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildSettingsPage$55();
            }
        });
        TextView label2 = label("brought to you by Instinct", 13.0f, textSecondary(), false);
        label2.setGravity(17);
        if (!wideLayout()) {
            linearLayout.addView(card, margin(0, 0, 0, 14));
            linearLayout.addView(sectionLabel, margin(2, 0, 0, 8));
            linearLayout.addView(card2, margin(0, 0, 0, 14));
            linearLayout.addView(sectionLabel2, margin(2, 0, 0, 8));
            linearLayout.addView(card3, margin(0, 0, 0, 14));
            linearLayout.addView(sectionLabel3, margin(2, 0, 0, 8));
            linearLayout.addView(card4, margin(0, 0, 0, 14));
            linearLayout.addView(sectionLabel4, margin(2, 0, 0, 9));
            linearLayout.addView(card5, margin(0, 0, 0, 8));
            linearLayout.addView(label, margin(3, 2, 3, 14));
            linearLayout.addView(sectionLabel5, margin(2, 0, 0, 8));
            linearLayout.addView(card6, margin(0, 0, 0, 18));
            linearLayout.addView(label2, margin(0, 4, 0, 10));
            return;
        }
        LinearLayout vertical2 = vertical();
        vertical2.addView(card, margin(0, 0, 0, 14));
        vertical2.addView(sectionLabel2, margin(2, 0, 0, 8));
        vertical2.addView(card3, margin(0, 0, 0, 14));
        LinearLayout vertical3 = vertical();
        vertical3.addView(sectionLabel, margin(2, 0, 0, 8));
        vertical3.addView(card2, margin(0, 0, 0, 14));
        vertical3.addView(sectionLabel3, margin(2, 0, 0, 8));
        vertical3.addView(card4, margin(0, 0, 0, 14));
        vertical3.addView(sectionLabel4, margin(2, 0, 0, 9));
        vertical3.addView(card5, margin(0, 0, 0, 8));
        vertical3.addView(label, margin(3, 2, 3, 14));
        addPair(linearLayout, vertical2, vertical3);
        linearLayout.addView(sectionLabel5, margin(2, 0, 0, 8));
        linearLayout.addView(card6, margin(0, 0, 0, 18));
        linearLayout.addView(label2, margin(0, 4, 0, 10));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$48(View view) {
        performTapFeedback(view);
        openPage("setup");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$49() {
        openPage("layout");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$50(boolean z) {
        this.hapticsEnabled = z;
        this.uiPreferences.edit().putBoolean("haptics", z).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$51(boolean z) {
        this.keepScreenAwake = z;
        this.uiPreferences.edit().putBoolean("keep_awake", z).apply();
        if (z) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$52(boolean z) {
        this.confirmFirmwareLoad = z;
        this.uiPreferences.edit().putBoolean("confirm_firmware", z).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$53() {
        openPage("activity");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$54() {
        openPage("help");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSettingsPage$55() {
        openPage("about");
    }

    private void addSettingToggle(LinearLayout linearLayout, String str, String str2, boolean z, final ToggleAction toggleAction) {
        LinearLayout horizontal = horizontal();
        horizontal.setPadding(0, dp(12.0f), 0, dp(12.0f));
        LinearLayout vertical = vertical();
        vertical.addView(label(str, 16.0f, textPrimary(), true), margin(0, 0, 0, 4));
        TextView label = label(str2, 13.0f, textSecondary(), false);
        label.setLineSpacing(dp(3.0f), 1.0f);
        vertical.addView(label);
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        final FrameLayout oneUiSwitch = oneUiSwitch(z);
        horizontal.addView(oneUiSwitch, new LinearLayout.LayoutParams(dp(52.0f), dp(31.0f)));
        horizontal.setClickable(true);
        horizontal.setFocusable(true);
        horizontal.setHapticFeedbackEnabled(this.hapticsEnabled);
        final boolean[] zArr = {z};
        horizontal.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda66
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$addSettingToggle$56(zArr, oneUiSwitch, toggleAction, view);
            }
        });
        linearLayout.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$addSettingToggle$56(boolean[] zArr, FrameLayout frameLayout, ToggleAction toggleAction, View view) {
        performTapFeedback(view);
        boolean z = !zArr[0];
        zArr[0] = z;
        updateOneUiSwitch(frameLayout, z);
        toggleAction.onToggle(zArr[0]);
    }

    private void updateOneUiSwitch(FrameLayout frameLayout, boolean z) {
        int i;
        final View childAt = frameLayout.getChildAt(0);
        final int left = childAt.getLeft();
        if (z) {
            i = -16747810;
        } else {
            i = this.darkMode ? -12961217 : -2500130;
        }
        frameLayout.setBackground(flat(i, 16));
        int i2 = -1;
        if (!z && this.darkMode) {
            i2 = -7434602;
        }
        childAt.setBackground(flat(i2, 13));
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
        layoutParams.gravity = (z ? GravityCompat.END : GravityCompat.START) | 16;
        childAt.setLayoutParams(layoutParams);
        frameLayout.post(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda51
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$updateOneUiSwitch$57(left, childAt);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$updateOneUiSwitch$57(int i, View view) {
        float left = i - view.getLeft();
        if (left == 0.0f || reducedMotion()) {
            return;
        }
        view.setTranslationX(left);
        view.animate().translationX(0.0f).setDuration(140L).setInterpolator(oneUiEase()).start();
    }

    private FrameLayout oneUiSwitch(boolean z) {
        int i;
        FrameLayout frameLayout = new FrameLayout(this);
        if (z) {
            i = -16747810;
        } else {
            i = this.darkMode ? -12961217 : -2500130;
        }
        frameLayout.setBackground(flat(i, 16));
        View view = new View(this);
        int i2 = -1;
        if (!z && this.darkMode) {
            i2 = -7434602;
        }
        view.setBackground(flat(i2, 13));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dp(25.0f), dp(25.0f), (z ? GravityCompat.END : GravityCompat.START) | 16);
        layoutParams.setMargins(dp(3.0f), 0, dp(3.0f), 0);
        frameLayout.addView(view, layoutParams);
        return frameLayout;
    }

    private void buildAboutPage(LinearLayout linearLayout) {
        pageAppBar(linearLayout, getString(R.string.text_040));
        LinearLayout card = card();
        card.setGravity(1);
        card.setPadding(dp(20.0f), dp(24.0f), dp(20.0f), dp(18.0f));
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.mipmap.ic_launcher);
        imageView.setContentDescription(getString(R.string.text_195));
        card.addView(imageView, new LinearLayout.LayoutParams(dp(64.0f), dp(64.0f)));
        card.addView(label("HP P1005", 20.0f, textPrimary(), true), margin(0, 12, 0, 4));
        card.addView(label(getString(R.string.text_196), 12.0f, textSecondary(), false), margin(0, 0, 0, 14));
        card.addView(label(getString(R.string.text_197), 10.5f, eyebrowColor(), true), margin(0, 0, 0, 3));
        card.addView(label(readAppVersion(), 14.0f, textPrimary(), true), margin(0, 0, 0, 12));
        card.addView(label(getString(R.string.text_198), 10.5f, eyebrowColor(), true), margin(0, 0, 0, 3));
        card.addView(label(BuildConfig.APPLICATION_ID, 12.5f, textSecondary(), false));
        linearLayout.addView(card, margin(0, 0, 0, 14));
        LinearLayout card2 = card();
        card2.addView(label(getString(R.string.text_199), 16.0f, textPrimary(), true), margin(0, 0, 0, 10));
        addBullet(card2, getString(R.string.text_200));
        addBullet(card2, getString(R.string.text_201));
        addBullet(card2, getString(R.string.text_202));
        addBullet(card2, getString(R.string.text_203));
        addBullet(card2, getString(R.string.text_204));
        addBullet(card2, getString(R.string.text_205));
        addBullet(card2, getString(R.string.text_206));
        linearLayout.addView(card2, margin(0, 0, 0, 14));
        LinearLayout card3 = card();
        card3.addView(label(getString(R.string.text_207), 16.0f, textPrimary(), true), margin(0, 0, 0, 8));
        TextView label = label(getString(R.string.text_208), 12.0f, textSecondary(), false);
        label.setLineSpacing(dp(4.0f), 1.0f);
        card3.addView(label);
        linearLayout.addView(card3, margin(0, 0, 0, 18));
        TextView label2 = label("brought to you by Instinct", 11.5f, textSecondary(), false);
        label2.setGravity(17);
        linearLayout.addView(label2, margin(0, 4, 0, 10));
    }

    private void addBullet(LinearLayout linearLayout, String str) {
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(48);
        horizontal.addView(label("•", 16.0f, primaryColor(), true), new LinearLayout.LayoutParams(dp(16.0f), -2));
        TextView label = label(str, 12.0f, textSecondary(), false);
        label.setLineSpacing(dp(3.0f), 1.0f);
        horizontal.addView(label, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout.addView(horizontal, margin(0, 0, 0, 8));
    }

    private void buildHelpPage(LinearLayout linearLayout) {
        pageAppBar(linearLayout, getString(R.string.text_194));
        LinearLayout card = card();
        card.setPadding(dp(16.0f), dp(8.0f), dp(16.0f), dp(8.0f));
        addHelpItem(card, "1", getString(R.string.text_209), getString(R.string.text_210));
        addSeparator(card);
        addHelpItem(card, "2", getString(R.string.text_211), getString(R.string.text_212));
        addSeparator(card);
        addHelpItem(card, "3", getString(R.string.text_213), getString(R.string.text_214));
        addSeparator(card);
        addHelpItem(card, "4", getString(R.string.text_215), getString(R.string.text_216));
        addSeparator(card);
        addHelpItem(card, "5", getString(R.string.text_217), getString(R.string.text_218));
        addSeparator(card);
        addHelpItem(card, "6", getString(R.string.text_219), getString(R.string.text_220));
        linearLayout.addView(card, margin(0, 0, 0, 15));
        Button button = tonalButton(getString(R.string.text_221), 14.5f);
        button.setMinHeight(dp(48.0f));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda64
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildHelpPage$58(view);
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-1, dp(48.0f)));
        Button button2 = tonalButton(getString(R.string.text_063), 14.5f);
        button2.setMinHeight(dp(48.0f));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda65
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildHelpPage$59(view);
            }
        });
        linearLayout.addView(button2, margin(0, 10, 0, 8));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildHelpPage$58(View view) {
        scanDevices();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildHelpPage$59(View view) {
        performTapFeedback(view);
        printBuiltInTestPage();
    }

    private void addHelpItem(LinearLayout linearLayout, String str, String str2, String str3) {
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(48);
        horizontal.setPadding(dp(2.0f), dp(11.0f), dp(2.0f), dp(11.0f));
        TextView label = label(str, 12.0f, tonalText(), true);
        label.setGravity(17);
        label.setBackground(flat(tonalBg(), 18));
        horizontal.addView(label, new LinearLayout.LayoutParams(dp(36.0f), dp(36.0f)));
        LinearLayout vertical = vertical();
        vertical.setPaddingRelative(dp(11.0f), 0, 0, 0);
        vertical.addView(label(str2, 14.0f, textPrimary(), true), margin(0, 0, 0, 4));
        TextView label2 = label(str3, 12.0f, textSecondary(), false);
        label2.setLineSpacing(dp(3.0f), 1.0f);
        vertical.addView(label2);
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
    }

    private void buildActivityPage(LinearLayout linearLayout) {
        pageAppBar(linearLayout, getString(R.string.text_192));
        LinearLayout horizontal = horizontal();
        Button button = tonalButton(getString(R.string.text_222), 13.0f);
        button.setMinHeight(dp(44.0f));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda56
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildActivityPage$60(view);
            }
        });
        horizontal.addView(button, new LinearLayout.LayoutParams(0, dp(44.0f), 1.0f));
        Button button2 = tonalButton(getString(R.string.text_223), 13.0f);
        button2.setMinHeight(dp(44.0f));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda57
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildActivityPage$61(view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(44.0f), 1.0f);
        layoutParams.setMarginStart(dp(9.0f));
        horizontal.addView(button2, layoutParams);
        linearLayout.addView(horizontal, margin(0, 0, 0, 13));
        LinearLayout card = card();
        LinearLayout horizontal2 = horizontal();
        horizontal2.addView(label(getString(R.string.text_225), 11.0f, eyebrowColor(), true), new LinearLayout.LayoutParams(0, -2, 1.0f));
        horizontal2.addView(statusPill(hasPrinterConnected()));
        card.addView(horizontal2, margin(0, 0, 0, 13));
        TextView label = label(this.activityLog.toString(), 12.0f, textPrimary(), false);
        this.output = label;
        label.setTextIsSelectable(true);
        this.output.setTypeface(Typeface.create("monospace", 0));
        this.output.setLineSpacing(dp(4.0f), 1.0f);
        card.addView(this.output, new LinearLayout.LayoutParams(-1, -2));
        linearLayout.addView(card, margin(0, 0, 0, 16));
        Button bigPrimaryButton = bigPrimaryButton(getString(R.string.text_221));
        bigPrimaryButton.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda58
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildActivityPage$62(view);
            }
        });
        linearLayout.addView(bigPrimaryButton, new LinearLayout.LayoutParams(-1, dp(52.0f)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildActivityPage$60(View view) {
        copyActivityLog();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildActivityPage$61(View view) {
        clearActivityLog();
        systemToast(getString(R.string.text_224), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildActivityPage$62(View view) {
        scanDevices();
        renderApp();
    }

    private void openDocumentPicker(boolean z) {
        try {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.setType(z ? "image/*" : "*/*");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", false);
            if (!z) {
                intent.putExtra("android.intent.extra.MIME_TYPES", new String[]{"application/pdf", "image/*", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/rtf", "text/plain", "text/csv", "text/html", "application/vnd.oasis.opendocument.text", "application/vnd.oasis.opendocument.spreadsheet", "application/vnd.oasis.opendocument.presentation"});
            }
            startActivityForResult(intent, 42);
        } catch (Exception e) {
            lambda$append$117(getString(R.string.text_226) + e.getMessage());
            systemToast(getString(R.string.text_227), 1).show();
        }
    }

    private void loadSelectedDocument(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, 1);
        } catch (Exception unused) {
        }
        try {
            String queryDisplayName = queryDisplayName(uri);
            String type = getContentResolver().getType(uri);
            if (type == null || type.trim().isEmpty()) {
                type = guessMimeFromName(queryDisplayName);
            }
            DocumentRenderer documentRenderer = new DocumentRenderer(this, uri, queryDisplayName, type);
            DocumentRenderer documentRenderer2 = this.documentRenderer;
            if (documentRenderer2 != null) {
                documentRenderer2.close();
            }
            this.selectedDocumentUri = uri;
            this.selectedDocumentName = queryDisplayName;
            if (type == null) {
                type = "";
            }
            this.selectedDocumentMime = type;
            this.documentRenderer = documentRenderer;
            this.currentPreviewPage = 0;
            Bitmap bitmap = this.currentPreviewBitmap;
            if (bitmap != null && !bitmap.isRecycled()) {
                this.currentPreviewBitmap.recycle();
                this.currentPreviewBitmap = null;
            }
            if (documentRenderer.isSupported()) {
                lambda$append$117(getString(R.string.text_228) + queryDisplayName + " (" + documentRenderer.getFormatLabel() + getString(R.string.text_229) + documentRenderer.getPageCount() + getString(R.string.text_230));
            } else {
                lambda$append$117(getString(R.string.text_231) + queryDisplayName + getString(R.string.text_232));
            }
            if (!"preview".equals(this.currentPage)) {
                saveScrollPosition();
                this.pageStack.push(this.currentPage);
                this.scrollPositions.remove("preview");
            }
            this.currentPage = "preview";
            swapToPage("preview", 3);
        } catch (Exception e) {
            if (debuggable()) {
                Log.w("HP1005-Share", "document load failed for " + uri, e);
            } else {
                Log.w("HP1005-Share", "document load failed: " + e.getClass().getSimpleName());
            }
            lambda$append$117(getString(R.string.text_233) + e.getClass().getSimpleName() + ": " + e.getMessage());
            systemToast(getString(R.string.text_234), 1).show();
        }
    }

    private String queryDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("_display_name");
                if (columnIndex >= 0) {
                    String string = cursor.getString(columnIndex);
                    if (string != null && !string.trim().isEmpty()) {
                        return string;
                    }
                }
            }
        } catch (Exception unused) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        String lastPathSegment = uri.getLastPathSegment();
        return (lastPathSegment == null || lastPathSegment.isEmpty()) ? getString(R.string.text_235) : lastPathSegment;
    }

    private String guessMimeFromName(String str) {
        String extensionOf = DocumentRenderer.extensionOf(str);
        extensionOf.hashCode();
        switch (extensionOf) {
            case "md":
            case "log":
            case "txt":
                return "text/plain";
            case "bmp":
                return "image/bmp";
            case "csv":
                return "text/csv";
            case "gif":
                return "image/gif";
            case "htm":
            case "html":
                return "text/html";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "pdf":
                return "application/pdf";
            case "png":
                return "image/png";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "webp":
                return "image/webp";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default:
                return "application/octet-stream";
        }
    }

    private void printSelectedDocument() {
        DocumentRenderer documentRenderer;
        String str;
        String string;
        if (this.printInProgress) {
            return;
        }
        if (this.selectedDocumentUri == null || (documentRenderer = this.documentRenderer) == null) {
            systemToast(getString(R.string.text_236), 0).show();
            openDocumentPicker(false);
            return;
        }
        if (!documentRenderer.isSupported()) {
            showNativeDialog(dialogBuilder().setTitle(getString(R.string.text_237)).setMessage(getString(R.string.text_238)).setPositiveButton(getString(R.string.text_091), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda85
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.lambda$printSelectedDocument$63(dialogInterface, i);
                }
            }).setNegativeButton(getString(R.string.text_111), (DialogInterface.OnClickListener) null).create());
            return;
        }
        try {
            ArrayList<Integer> resolvePageRange = resolvePageRange(this.pageRange, this.documentRenderer.getPageCount());
            UsbDevice findConnectedPrinter = findConnectedPrinter();
            if (findConnectedPrinter == null && !MainDebugHooks.enabled()) {
                lambda$append$117(getString(R.string.text_239));
                systemToast(getString(R.string.text_240), 1).show();
                return;
            }
            if (!MainDebugHooks.enabled() && !this.usbManager.hasPermission(findConnectedPrinter)) {
                lambda$append$117(getString(R.string.text_241));
                scanDevices();
                systemToast(getString(R.string.text_242), 1).show();
                return;
            }
            if ("booklet".equals(this.layoutMode)) {
                final PrintRequest printRequest = new PrintRequest(this);
                boolean isEmpty = PrintFlow.select(planSheets(resolvePageRange, this.documentRenderer, printRequest.spec, printRequest.paper, printRequest.overlapMm), "booklet-back").isEmpty();
                if (printRequest.copies <= 1) {
                    str = "";
                } else if (!isEmpty) {
                    str = getString(R.string.text_243) + printRequest.copies + getString(R.string.text_244);
                } else {
                    str = getString(R.string.text_243) + printRequest.copies + getString(R.string.text_245);
                }
                if (!isEmpty) {
                    string = getString(R.string.text_246);
                } else {
                    string = getString(R.string.text_247);
                }
                AlertDialog.Builder title = dialogBuilder().setTitle(getString(R.string.text_248));
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.text_249));
                sb.append(getString(printRequest.spec.binding == SheetComposer.Binding.RIGHT ? R.string.text_250 : R.string.text_251));
                sb.append(").");
                sb.append(string);
                sb.append(str);
                showNativeDialog(title.setMessage(sb.toString()).setNegativeButton(getString(R.string.text_111), (DialogInterface.OnClickListener) null).setPositiveButton(getString(R.string.text_252), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda86
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.lambda$printSelectedDocument$64(printRequest, dialogInterface, i);
                    }
                }).create());
                return;
            }
            boolean z = !DebugKt.DEBUG_PROPERTY_VALUE_OFF.equals(this.duplexMode) && planSheets(resolvePageRange, this.documentRenderer).size() > 1;
            if (z && this.copies > 1) {
                showNativeDialog(dialogBuilder().setTitle(getString(R.string.text_253)).setMessage(getString(R.string.text_254)).setNegativeButton(getString(R.string.text_111), (DialogInterface.OnClickListener) null).setPositiveButton(getString(R.string.text_255), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda87
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.lambda$printSelectedDocument$65(dialogInterface, i);
                    }
                }).create());
            } else if (z) {
                showNativeDialog(dialogBuilder().setTitle(getString(R.string.text_253)).setMessage(getString(R.string.text_256)).setNegativeButton(getString(R.string.text_111), (DialogInterface.OnClickListener) null).setPositiveButton(getString(R.string.text_257), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda88
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.lambda$printSelectedDocument$66(dialogInterface, i);
                    }
                }).create());
            } else {
                startDocumentPrintPass(false, false);
            }
        } catch (IllegalArgumentException e) {
            systemToast(e.getMessage(), 1).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$printSelectedDocument$63(DialogInterface dialogInterface, int i) {
        openSelectedFileExternally();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$printSelectedDocument$64(PrintRequest printRequest, DialogInterface dialogInterface, int i) {
        startPrintPass("booklet-front", 1, printRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$printSelectedDocument$65(DialogInterface dialogInterface, int i) {
        this.copies = 1;
        this.printPreferences.edit().putInt("copies", this.copies).apply();
        renderApp();
        startDocumentPrintPass(true, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$printSelectedDocument$66(DialogInterface dialogInterface, int i) {
        startDocumentPrintPass(true, false);
    }

    private void startDocumentPrintPass(boolean z, boolean z2) {
        String str;
        if (z) {
            str = "odd";
        } else {
            str = z2 ? "even" : "all";
        }
        startPrintPass(str, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    static final class PrintRequest {
        final int copies;
        final int density;
        final boolean draft;
        final String duplex;
        final String mime;
        final String name;
        final int overlapMm;
        final String paper;
        final String range;
        final int source;
        final SheetComposer.Spec spec;
        final Uri uri;

        PrintRequest(MainActivity mainActivity) {
            this.uri = mainActivity.selectedDocumentUri;
            this.name = mainActivity.selectedDocumentName;
            this.mime = mainActivity.selectedDocumentMime;
            this.paper = mainActivity.paperSize;
            this.duplex = mainActivity.duplexMode;
            this.range = mainActivity.pageRange;
            this.copies = mainActivity.copies;
            this.density = mainActivity.printDensity;
            this.draft = mainActivity.draftMode;
            this.source = mainActivity.resolveSourceCode();
            this.overlapMm = mainActivity.posterOverlapMm;
            this.spec = mainActivity.buildSheetSpec(1800);
        }
    }

    private void startPrintPass(String str, int i) {
        if (this.printInProgress || this.selectedDocumentUri == null) {
            return;
        }
        startPrintPass(str, i, new PrintRequest(this));
    }

    private void startPrintPass(final String str, final int i, final PrintRequest printRequest) {
        if (this.printInProgress) {
            return;
        }
        this.printInProgress = true;
        this.jobCancelRequested = false;
        final Uri uri = printRequest.uri;
        final String str2 = printRequest.name;
        final String str3 = printRequest.mime;
        final String str4 = printRequest.paper;
        final String str5 = printRequest.duplex;
        final String str6 = printRequest.range;
        final int i2 = printRequest.copies;
        final int i3 = printRequest.density;
        final boolean z = printRequest.draft;
        final int i4 = printRequest.source;
        new Thread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda119
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$startPrintPass$75(uri, str2, str3, str6, printRequest, str, i, i2, str4, str5, i3, z, i4);
            }
        }, "P1005-Document-Print").start();
    }

    /* Rebuilt by hand from bytecode (decompilers could not recover this method). */
    public /* synthetic */ void lambda$startPrintPass$75(Uri uri, String str, String str2, String str3, final PrintRequest printRequest, final String str4, final int i, final int i2, String str5, String str6, int i3, boolean z, int i4) {
        UsbPrinterIO.JOB_LOCK.lock();
        try {
            boolean hasBackSheets = false;
            boolean cancelled = false;
            int done = 0;
            int total = 0;
            String error = null;
            try {
                final DocumentRenderer documentRenderer = new DocumentRenderer(getApplicationContext(), uri, str, str2);
                try {
                    UsbDevice findConnectedPrinter = findConnectedPrinter();
                    if (findConnectedPrinter == null && !MainDebugHooks.enabled()) {
                        throw new IllegalStateException(getString(R.string.text_258));
                    }
                    if (!MainDebugHooks.enabled() && !this.usbManager.hasPermission(findConnectedPrinter)) {
                        throw new IllegalStateException(getString(R.string.text_259));
                    }
                    MainDebugHooks.validatePrinter();
                    ArrayList<Integer> resolvePageRange = resolvePageRange(str3, documentRenderer.getPageCount());
                    final SheetComposer.Spec spec = printRequest.spec;
                    if (MainDebugHooks.enabled()) {
                        StringBuilder sb = new StringBuilder("request pass=");
                        sb.append(str4);
                        sb.append(" copy=");
                        sb.append(i);
                        sb.append(" mode=");
                        sb.append(spec.mode);
                        sb.append(" binding=");
                        sb.append(spec.binding);
                        sb.append(" copies=");
                        sb.append(i2);
                        sb.append(" paper=");
                        sb.append(str5);
                        sb.append(" range=");
                        sb.append(str3);
                        Log.i("HPPrintFlow", sb.toString());
                    }
                    ArrayList<SheetJob> planSheets = planSheets(resolvePageRange, documentRenderer, spec, str5, printRequest.overlapMm);
                    ArrayList select = PrintFlow.select(planSheets, str4);
                    hasBackSheets = !PrintFlow.select(planSheets, "booklet-back").isEmpty();
                    boolean reverse = "booklet-back".equals(str4) || ("even".equals(str4) && "manual-long".equals(str6));
                    if (select.isEmpty()) {
                        throw new IllegalStateException(getString(R.string.text_260));
                    }
                    int passes = "all".equals(str4) ? Math.max(1, i2) : 1;
                    total = select.size() * passes;
                    this.currentJobName = str;
                    this.currentJobTotal = total;
                    this.currentJobDone = 0;
                    runOnUiThread(new Runnable() {
                        @Override // java.lang.Runnable
                        public final void run() {
                            MainActivity.this.renderApp();
                        }
                    });
                    final boolean rotatePages = spec.mode == SheetComposer.Mode.SIZE && spec.sheetWidthPx > spec.sheetHeightPx;
                    SheetComposer.PageSource pageSource = new SheetComposer.PageSource() {
                        @Override // com.mohamedelnaggar.hpp1005.SheetComposer.PageSource
                        public Bitmap page(int i8) throws Exception {
                            Bitmap renderPage = documentRenderer.renderPage(i8, spec.sheetWidthPx, spec.sheetHeightPx);
                            if (!rotatePages) {
                                return renderPage;
                            }
                            Bitmap rotate = Foo2XqxConverter.rotate(renderPage, 90.0f);
                            renderPage.recycle();
                            return rotate;
                        }

                        @Override // com.mohamedelnaggar.hpp1005.SheetComposer.PageSource
                        public void release(Bitmap bitmap) {
                            if (bitmap == null || bitmap.isRecycled()) {
                                return;
                            }
                            bitmap.recycle();
                        }

                        @Override // com.mohamedelnaggar.hpp1005.SheetComposer.PageSource
                        public float[] pagePhysicalMm(int i8) {
                            return documentRenderer.pagePhysicalMm(i8);
                        }
                    };
                    for (int pass = 1; pass <= passes && !cancelled; pass++) {
                        for (int idx = 0; idx < select.size(); idx++) {
                            if (this.jobCancelRequested) {
                                cancelled = true;
                                break;
                            }
                            SheetJob sheetJob = (SheetJob) select.get(idx);
                            Bitmap composed;
                            if (sheetJob.posterTile >= 0) {
                                Bitmap page = pageSource.page(sheetJob.pages[0]);
                                try {
                                    composed = SheetComposer.composePosterTile(spec, page, sheetJob.posterTile, sheetJob.posterCols, sheetJob.posterRows, posterBase100(spec, pageSource.pagePhysicalMm(sheetJob.pages[0]), page.getWidth(), page.getHeight()));
                                } finally {
                                    pageSource.release(page);
                                }
                            } else {
                                composed = SheetComposer.composeSheet(spec, sheetJob.pages, pageSource);
                            }
                            Foo2XqxConverter.Result result = null;
                            try {
                                result = Foo2XqxConverter.convert(getApplicationContext(), composed, str5, spec.sheetWidthPx > spec.sheetHeightPx, 1, i3, z, false, reverse, i4);
                                if (MainDebugHooks.enabled()) {
                                    MainDebugHooks.saveAndSend(this, composed, result.xqxFile, done + 1);
                                } else {
                                    sendXqxFileToPrinter(result.xqxFile, findConnectedPrinter);
                                }
                            } finally {
                                if (result != null && result.xqxFile.exists()) {
                                    result.xqxFile.delete();
                                }
                                if (composed != null && !composed.isRecycled()) {
                                    composed.recycle();
                                }
                            }
                            done++;
                            final String label = sheetJob.label;
                            this.currentJobDone = done;
                            final int passesF = passes;
                            final int passF = pass;
                            final int doneF = done;
                            final int totalF = total;
                            runOnUiThread(new Runnable() {
                                @Override // java.lang.Runnable
                                public final void run() {
                                    MainActivity.this.lambda$startPrintPass$67(label, passesF, passF, doneF, totalF);
                                }
                            });
                        }
                    }
                } finally {
                    documentRenderer.close();
                }
            } catch (Exception e) {
                error = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            final PrintFlow.Next after = PrintFlow.after(str4, hasBackSheets, i, i2, error == null, cancelled);
            final boolean cancelledF = cancelled;
            final int doneF2 = done;
            final int totalF2 = total;
            final String errorF = error;
            runOnUiThread(new Runnable() {
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$startPrintPass$74(cancelledF, doneF2, totalF2, errorF, after, printRequest, i, i2, str4);
                }
            });
        } finally {
            UsbPrinterIO.JOB_LOCK.unlock();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$67(String str, int i, int i2, int i3, int i4) {
        String str2;
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.text_261));
        sb.append(str);
        if (i > 1) {
            str2 = getString(R.string.text_262) + i2 + "/" + i;
        } else {
            str2 = "";
        }
        sb.append(str2);
        sb.append(" (");
        sb.append(i3);
        sb.append("/");
        sb.append(i4);
        sb.append(").");
        lambda$append$117(sb.toString());
        if ("queue".equals(this.currentPage) || "preview".equals(this.currentPage)) {
            renderApp();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$74(boolean z, int i, int i2, String str, PrintFlow.Next next, final PrintRequest printRequest, final int i3, final int i4, String str2) {
        String string;
        String str3;
        this.printInProgress = false;
        this.currentJobName = null;
        if (z) {
            lambda$append$117(getString(R.string.text_263) + i + getString(R.string.text_016) + i2 + getString(R.string.text_264));
            systemToast(getString(R.string.text_265), 1).show();
            renderApp();
            return;
        }
        if (str != null) {
            lambda$append$117(getString(R.string.text_266) + str);
            systemToast(getString(R.string.text_267) + str, 1).show();
            renderApp();
            return;
        }
        if (next == PrintFlow.Next.REINSERT_DUPLEX) {
            lambda$append$117(getString(R.string.text_268));
            renderApp();
            showNativeDialog(dialogBuilder().setTitle(getString(R.string.text_269)).setMessage(getString(R.string.text_270) + i + getString(R.string.text_271)).setNegativeButton(getString(R.string.text_272), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda67
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i5) {
                    MainActivity.this.lambda$startPrintPass$68(dialogInterface, i5);
                }
            }).setPositiveButton(getString(R.string.text_274), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda68
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i5) {
                    MainActivity.this.lambda$startPrintPass$69(printRequest, dialogInterface, i5);
                }
            }).create());
            return;
        }
        if (next == PrintFlow.Next.REINSERT_BOOKLET) {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.text_275));
            String str4 = "";
            if (i4 > 1) {
                str3 = getString(R.string.text_276) + i3 + "/" + i4;
            } else {
                str3 = "";
            }
            sb.append(str3);
            sb.append(getString(R.string.text_277));
            lambda$append$117(sb.toString());
            renderApp();
            AlertDialog.Builder title = dialogBuilder().setTitle(getString(R.string.text_278));
            StringBuilder sb2 = new StringBuilder();
            sb2.append(getString(R.string.text_270));
            sb2.append(i);
            sb2.append(getString(R.string.text_279));
            if (i4 > 1) {
                str4 = getString(R.string.text_280) + i3 + getString(R.string.text_016) + i4;
            }
            sb2.append(str4);
            sb2.append(getString(R.string.text_281));
            showNativeDialog(title.setMessage(sb2.toString()).setNegativeButton(getString(R.string.text_272), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda69
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i5) {
                    MainActivity.this.lambda$startPrintPass$70(dialogInterface, i5);
                }
            }).setPositiveButton(getString(R.string.text_274), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda70
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i5) {
                    MainActivity.this.lambda$startPrintPass$71(i3, printRequest, dialogInterface, i5);
                }
            }).create());
            return;
        }
        if (next == PrintFlow.Next.NEXT_COPY) {
            lambda$append$117(getString(R.string.text_283) + i3 + "/" + i4 + getString(R.string.text_284));
            renderApp();
            AlertDialog.Builder title2 = dialogBuilder().setTitle(getString(R.string.text_285));
            StringBuilder sb3 = new StringBuilder();
            sb3.append(getString(R.string.text_283));
            sb3.append(i3);
            sb3.append(getString(R.string.text_016));
            sb3.append(i4);
            sb3.append(getString(R.string.text_286));
            int i5 = i3 + 1;
            sb3.append(i5);
            sb3.append(".");
            AlertDialog.Builder negativeButton = title2.setMessage(sb3.toString()).setNegativeButton(getString(R.string.text_287) + i3 + getString(R.string.text_173), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda71
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i6) {
                    MainActivity.this.lambda$startPrintPass$72(i3, i4, dialogInterface, i6);
                }
            });
            StringBuilder sb4 = new StringBuilder();
            sb4.append(getString(R.string.text_290));
            sb4.append(i5);
            showNativeDialog(negativeButton.setPositiveButton(sb4.toString(), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda72
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i6) {
                    MainActivity.this.lambda$startPrintPass$73(i3, printRequest, dialogInterface, i6);
                }
            }).create());
            return;
        }
        if ("booklet-front".equals(str2)) {
            lambda$append$117(getString(R.string.text_291));
        } else {
            lambda$append$117(getString(R.string.text_292) + i + getString(R.string.text_293));
        }
        if ("even".equals(str2) || "booklet-back".equals(str2)) {
            string = getString(R.string.text_294);
        } else {
            string = getString(R.string.text_295);
        }
        systemToast(string, 1).show();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$68(DialogInterface dialogInterface, int i) {
        systemToast(getString(R.string.text_273), 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$69(PrintRequest printRequest, DialogInterface dialogInterface, int i) {
        startPrintPass("even", 1, printRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$70(DialogInterface dialogInterface, int i) {
        systemToast(getString(R.string.text_282), 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$71(int i, PrintRequest printRequest, DialogInterface dialogInterface, int i2) {
        startPrintPass("booklet-back", i, printRequest);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$72(int i, int i2, DialogInterface dialogInterface, int i3) {
        systemToast(getString(R.string.text_288) + i + getString(R.string.text_016) + i2 + getString(R.string.text_289), 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startPrintPass$73(int i, PrintRequest printRequest, DialogInterface dialogInterface, int i2) {
        startPrintPass("booklet-front", i + 1, printRequest);
    }

    private static final class SheetJob extends PrintFlow.Sheet {
        private SheetJob() {
        }
    }

    private SheetComposer.Mode currentLayoutMode() {
        String str = this.layoutMode;
        str.hashCode();
        switch (str) {
            case "poster":
                return SheetComposer.Mode.POSTER;
            case "booklet":
                return SheetComposer.Mode.BOOKLET;
            case "multiple":
                return SheetComposer.Mode.MULTIPLE;
            default:
                return SheetComposer.Mode.SIZE;
        }
    }

    private SheetComposer.Binding currentBinding() {
        return "left".equals(this.bookletBinding) ? SheetComposer.Binding.LEFT : SheetComposer.Binding.RIGHT;
    }

    private SheetComposer.ScaleMode currentScaleMode() {
        String str = this.scaleModePref;
        str.hashCode();
        switch (str) {
            case "actual":
                return SheetComposer.ScaleMode.ACTUAL;
            case "custom":
                return SheetComposer.ScaleMode.CUSTOM;
            case "shrink":
                return SheetComposer.ScaleMode.SHRINK;
            default:
                return SheetComposer.ScaleMode.FIT;
        }
    }

    private boolean documentIsLandscape() {
        DocumentRenderer documentRenderer = this.documentRenderer;
        if (documentRenderer != null && documentRenderer.isSupported()) {
            try {
                Bitmap renderPage = this.documentRenderer.renderPage(0, 64, 64);
                boolean z = renderPage.getWidth() > renderPage.getHeight();
                renderPage.recycle();
                return z;
            } catch (Exception unused) {
            }
        }
        return false;
    }

    private boolean resolveSheetLandscape(SheetComposer.Mode mode) {
        if (mode == SheetComposer.Mode.BOOKLET || "landscape".equals(this.layoutOrientation)) {
            return true;
        }
        if ("portrait".equals(this.layoutOrientation)) {
            return false;
        }
        return mode == SheetComposer.Mode.SIZE && documentIsLandscape();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SheetComposer.Spec buildSheetSpec(int i) {
        SheetComposer.Mode currentLayoutMode = currentLayoutMode();
        boolean resolveSheetLandscape = resolveSheetLandscape(currentLayoutMode);
        int[] sheetDims = SheetComposer.sheetDims(this.paperSize, resolveSheetLandscape, i);
        SheetComposer.Spec spec = new SheetComposer.Spec();
        spec.mode = currentLayoutMode;
        spec.scaleMode = currentScaleMode();
        spec.customScalePercent = this.customScalePercent;
        spec.pagesPerSheet = this.pagesPerSheet;
        spec.sheetWidthPx = sheetDims[0];
        spec.sheetHeightPx = sheetDims[1];
        spec.marginPx = SheetComposer.marginPx(this.paperSize, resolveSheetLandscape, i);
        float[] paperMm = SheetComposer.paperMm(this.paperSize);
        spec.paperWidthMm = resolveSheetLandscape ? paperMm[1] : paperMm[0];
        spec.rtl = !"ltr".equals(this.readingOrder);
        spec.binding = currentBinding();
        spec.posterScalePercent = this.posterScalePercent;
        spec.overlapPx = Math.max(0, Math.round((this.posterOverlapMm * i) / spec.paperWidthMm));
        spec.cutMarks = this.posterCutMarks;
        return spec;
    }

    private int[] posterGridFor(SheetComposer.Spec spec, DocumentRenderer documentRenderer, int i) throws Exception {
        return posterGridFor(spec, documentRenderer, i, this.paperSize, this.posterOverlapMm);
    }

    private int[] posterGridFor(SheetComposer.Spec spec, DocumentRenderer documentRenderer, int i, String str, int i2) throws Exception {
        float f;
        float f2;
        float[] paperMm = SheetComposer.paperMm(str);
        boolean z = spec.sheetWidthPx > spec.sheetHeightPx;
        float f3 = (z ? paperMm[1] : paperMm[0]) - 8.0f;
        float f4 = (z ? paperMm[0] : paperMm[1]) - 8.0f;
        float[] pagePhysicalMm = documentRenderer.pagePhysicalMm(i);
        if (pagePhysicalMm != null) {
            f2 = pagePhysicalMm[0];
            f = pagePhysicalMm[1];
        } else {
            float pageAspect = pageAspect(documentRenderer, i);
            float min = Math.min(f3, f4 * pageAspect);
            f = min / pageAspect;
            f2 = min;
        }
        return SheetComposer.posterGridMm(f3, f4, i2, f2, f, spec.posterScalePercent);
    }

    private float pageAspect(DocumentRenderer documentRenderer, int i) throws Exception {
        String str = System.identityHashCode(documentRenderer) + "#" + i;
        Float f = this.imageAspectCache.get(str);
        if (f != null) {
            return f.floatValue();
        }
        Bitmap renderPage = documentRenderer.renderPage(i, 256, 256);
        float width = renderPage.getWidth() / (float) Math.max(1, renderPage.getHeight());
        renderPage.recycle();
        this.imageAspectCache.put(str, Float.valueOf(width));
        return width;
    }

    private static float posterBase100(SheetComposer.Spec spec, float[] fArr, int i, int i2) {
        if (fArr != null) {
            return SheetComposer.actualScale(fArr[0], spec.paperWidthMm, spec.sheetWidthPx, i);
        }
        return Math.min((spec.sheetWidthPx - (spec.marginPx * 2)) / (float) i, (spec.sheetHeightPx - (spec.marginPx * 2)) / (float) i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int resolveSourceCode() {
        if ("main".equals(this.paperSource)) {
            return 1;
        }
        if ("priority".equals(this.paperSource)) {
            return 4;
        }
        if (this.paperSize == null || this.sourceMapMain.isEmpty() || !this.paperSize.equalsIgnoreCase(this.sourceMapMain)) {
            return (this.paperSize == null || this.sourceMapPriority.isEmpty() || !this.paperSize.equalsIgnoreCase(this.sourceMapPriority)) ? 7 : 4;
        }
        return 1;
    }

    private ArrayList<SheetJob> planSheets(ArrayList<Integer> arrayList, DocumentRenderer documentRenderer) {
        return planSheets(arrayList, documentRenderer, buildSheetSpec(1000), this.paperSize, this.posterOverlapMm);
    }

    private ArrayList<SheetJob> planSheets(ArrayList<Integer> arrayList, DocumentRenderer documentRenderer, SheetComposer.Spec spec, String str, int i) {
        int[] iArr;
        SheetComposer.Mode mode = spec.mode;
        ArrayList<SheetJob> arrayList2 = new ArrayList<>();
        if (mode == SheetComposer.Mode.MULTIPLE) {
            int max = Math.max(2, spec.pagesPerSheet);
            for (int i2 = 0; i2 < arrayList.size(); i2 += max) {
                SheetJob sheetJob = new SheetJob();
                int min = Math.min(max, arrayList.size() - i2);
                sheetJob.pages = new int[min];
                StringBuilder sb = new StringBuilder(getString(R.string.text_296));
                for (int i3 = 0; i3 < min; i3++) {
                    int i4 = i2 + i3;
                    sheetJob.pages[i3] = arrayList.get(i4).intValue();
                    if (i3 > 0) {
                        sb.append(getString(R.string.text_229));
                    }
                    sb.append(arrayList.get(i4).intValue() + 1);
                }
                sheetJob.label = sb.toString();
                arrayList2.add(sheetJob);
            }
        } else if (mode == SheetComposer.Mode.POSTER) {
            Iterator<Integer> it = arrayList.iterator();
            while (it.hasNext()) {
                int intValue = it.next().intValue();
                try {
                    iArr = posterGridFor(spec, documentRenderer, intValue, str, i);
                } catch (Exception unused) {
                    iArr = new int[]{2, 2};
                }
                int i5 = iArr[0] * iArr[1];
                int i6 = 0;
                while (i6 < i5) {
                    SheetJob sheetJob2 = new SheetJob();
                    sheetJob2.pages = new int[]{intValue};
                    sheetJob2.posterTile = i6;
                    sheetJob2.posterCols = iArr[0];
                    sheetJob2.posterRows = iArr[1];
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(getString(R.string.text_297));
                    i6++;
                    sb2.append(i6);
                    sb2.append("/");
                    sb2.append(i5);
                    sb2.append(getString(R.string.text_298));
                    sb2.append(intValue + 1);
                    sheetJob2.label = sb2.toString();
                    arrayList2.add(sheetJob2);
                }
            }
        } else if (mode == SheetComposer.Mode.BOOKLET) {
            Iterator<PrintFlow.Sheet> it2 = PrintFlow.booklet(arrayList, spec.binding).iterator();
            while (it2.hasNext()) {
                PrintFlow.Sheet next = it2.next();
                SheetJob sheetJob3 = new SheetJob();
                sheetJob3.pages = next.pages;
                sheetJob3.bookletBack = next.bookletBack;
                StringBuilder sb3 = new StringBuilder();
                sb3.append(getString(R.string.text_441));
                sb3.append(getString(next.bookletBack ? R.string.text_442 : R.string.text_443));
                sb3.append(getString(R.string.text_444));
                sb3.append((arrayList2.size() / 2) + 1);
                sheetJob3.label = sb3.toString();
                arrayList2.add(sheetJob3);
            }
        } else {
            Iterator<Integer> it3 = arrayList.iterator();
            while (it3.hasNext()) {
                int intValue2 = it3.next().intValue();
                SheetJob sheetJob4 = new SheetJob();
                sheetJob4.pages = new int[]{intValue2};
                sheetJob4.label = getString(R.string.text_084) + (intValue2 + 1);
                arrayList2.add(sheetJob4);
            }
        }
        return arrayList2;
    }

    private ArrayList<SheetJob> previewPlan() {
        ArrayList<Integer> resolvePageRange;
        int pageCount = this.documentRenderer.getPageCount();
        try {
            resolvePageRange = resolvePageRange(this.pageRange, pageCount);
        } catch (IllegalArgumentException unused) {
            resolvePageRange = resolvePageRange("all", pageCount);
        }
        return planSheets(resolvePageRange, this.documentRenderer);
    }

    private Bitmap renderPreviewSheet(SheetComposer.Spec spec, SheetJob sheetJob) throws Exception {
        return renderPreviewSheet(spec, sheetJob, this.documentRenderer);
    }

    private Bitmap renderPreviewSheet(final SheetComposer.Spec spec, SheetJob sheetJob, final DocumentRenderer documentRenderer) throws Exception {
        final boolean z = spec.mode == SheetComposer.Mode.SIZE && spec.sheetWidthPx > spec.sheetHeightPx;
        SheetComposer.PageSource pageSource = new SheetComposer.PageSource() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.7
            @Override // com.mohamedelnaggar.hpp1005.SheetComposer.PageSource
            public Bitmap page(int i) throws Exception {
                Bitmap renderPage = documentRenderer.renderPage(i, spec.sheetWidthPx, spec.sheetHeightPx);
                if (!z) {
                    return renderPage;
                }
                Bitmap rotate = Foo2XqxConverter.rotate(renderPage, 90.0f);
                renderPage.recycle();
                return rotate;
            }

            @Override // com.mohamedelnaggar.hpp1005.SheetComposer.PageSource
            public void release(Bitmap bitmap) {
                if (bitmap == null || bitmap.isRecycled()) {
                    return;
                }
                bitmap.recycle();
            }

            @Override // com.mohamedelnaggar.hpp1005.SheetComposer.PageSource
            public float[] pagePhysicalMm(int i) {
                return documentRenderer.pagePhysicalMm(i);
            }
        };
        if (sheetJob.posterTile >= 0) {
            Bitmap page = pageSource.page(sheetJob.pages[0]);
            try {
                return SheetComposer.composePosterTile(spec, page, sheetJob.posterTile, sheetJob.posterCols, sheetJob.posterRows, posterBase100(spec, pageSource.pagePhysicalMm(sheetJob.pages[0]), page.getWidth(), page.getHeight()));
            } finally {
                pageSource.release(page);
            }
        }
        return SheetComposer.composeSheet(spec, sheetJob.pages, pageSource);
    }

    private String layoutSummary() {
        String str = this.layoutMode;
        str.hashCode();
        switch (str) {
            case "poster":
                return getString(R.string.text_299) + ((int) this.posterScalePercent) + getString(R.string.text_300);
            case "booklet":
                return getString(R.string.text_302) + bindingLabel();
            case "multiple":
                return getString(R.string.text_301) + this.pagesPerSheet;
            default:
                return getString(R.string.text_303) + scaleModeLabel();
        }
    }

    private String modeLabel() {
        String str = this.layoutMode;
        str.hashCode();
        switch (str) {
            case "poster":
                return getString(R.string.text_304);
            case "booklet":
                return getString(R.string.text_306);
            case "multiple":
                return getString(R.string.text_305);
            default:
                return getString(R.string.text_307);
        }
    }

    private String scaleModeLabel() {
        String str = this.scaleModePref;
        str.hashCode();
        switch (str) {
            case "actual":
                return getString(R.string.text_308);
            case "custom":
                return getString(R.string.text_310) + this.customScalePercent + getString(R.string.text_300);
            case "shrink":
                return getString(R.string.text_309);
            default:
                return getString(R.string.text_311);
        }
    }

    private String layoutOrientationLabel() {
        return "landscape".equals(this.layoutOrientation) ? getString(R.string.text_312) : "portrait".equals(this.layoutOrientation) ? getString(R.string.text_313) : getString(R.string.text_314);
    }

    private String paperSourceLabel() {
        return "main".equals(this.paperSource) ? getString(R.string.text_315) : "priority".equals(this.paperSource) ? getString(R.string.text_316) : getString(R.string.text_314);
    }

    private String bindingLabel() {
        return getString("left".equals(this.bookletBinding) ? R.string.text_251 : R.string.text_250);
    }

    private String readingOrderLabel() {
        return getString("ltr".equals(this.readingOrder) ? R.string.text_317 : R.string.text_318);
    }

    private String sourceMapSummary() {
        if (this.sourceMapMain.isEmpty() && this.sourceMapPriority.isEmpty()) {
            return getString(R.string.text_319);
        }
        StringBuilder sb = new StringBuilder();
        if (!this.sourceMapMain.isEmpty()) {
            sb.append(getString(R.string.text_320));
            sb.append(this.sourceMapMain);
        }
        if (!this.sourceMapPriority.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(getString(R.string.text_321));
            sb.append(this.sourceMapPriority);
        }
        return sb.toString();
    }

    private int layoutModeIndex() {
        if ("poster".equals(this.layoutMode)) {
            return 1;
        }
        if ("multiple".equals(this.layoutMode)) {
            return 2;
        }
        return "booklet".equals(this.layoutMode) ? 3 : 0;
    }

    private LinearLayout buildModeSegment() {
        LinearLayout vertical = vertical();
        final FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackground(flat(this.darkMode ? 1031305344 : 527988864, 14));
        View view = new View(this);
        this.layoutModePill = view;
        view.setBackground(flat(this.darkMode ? -12961217 : -1, 10));
        frameLayout.addView(this.layoutModePill, new FrameLayout.LayoutParams(0, dp(36.0f)));
        LinearLayout horizontal = horizontal();
        String[] strArr = {"size", "poster", "multiple", "booklet"};
        String[] strArr2 = {getString(R.string.text_307), getString(R.string.text_304), getString(R.string.text_305), getString(R.string.text_306)};
        this.layoutModeLabels = new TextView[4];
        this.layoutModePosition = layoutModeIndex();
        for (int i = 0; i < 4; i++) {
            final String str = strArr[i];
            TextView label = label(strArr2[i], 12.0f, textSecondary(), true);
            label.setGravity(17);
            label.setClickable(true);
            label.setFocusable(true);
            label.setContentDescription(strArr2[i]);
            label.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda120
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    MainActivity.this.lambda$buildModeSegment$76(str, frameLayout, view2);
                }
            });
            this.layoutModeLabels[i] = label;
            horizontal.addView(label, new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f));
        }
        frameLayout.addView(horizontal, new FrameLayout.LayoutParams(-1, dp(40.0f)));
        frameLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda131
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view2, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
                MainActivity.this.lambda$buildModeSegment$77(frameLayout, view2, i2, i3, i4, i5, i6, i7, i8, i9);
            }
        });
        vertical.addView(frameLayout, new LinearLayout.LayoutParams(-1, dp(40.0f)));
        return vertical;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildModeSegment$76(String str, FrameLayout frameLayout, View view) {
        performTapFeedback(view);
        if (str.equals(this.layoutMode)) {
            return;
        }
        this.layoutMode = str;
        this.printPreferences.edit().putString("layout_mode", str).apply();
        this.currentPreviewPage = 0;
        animateLayoutMode(frameLayout);
        refreshLayoutSettings();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildModeSegment$77(FrameLayout frameLayout, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        placeLayoutPill(frameLayout);
    }

    private void placeLayoutPill(FrameLayout frameLayout) {
        float width = frameLayout.getWidth() / 4.0f;
        float f = this.layoutModePosition;
        float max = Math.max(0.0f, width - dp(4.0f)) * ((((float) Math.sin((f - ((float) Math.floor(f))) * 3.141592653589793d)) * 0.14f) + 1.0f);
        float width2 = isArabic() ? frameLayout.getWidth() - (width * (this.layoutModePosition + 0.5f)) : (this.layoutModePosition + 0.5f) * width;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.layoutModePill.getLayoutParams();
        int round = Math.round(max);
        if (layoutParams.width != round || layoutParams.height != dp(36.0f) || layoutParams.topMargin != dp(2.0f)) {
            layoutParams.width = round;
            layoutParams.height = dp(36.0f);
            layoutParams.gravity = 51;
            layoutParams.topMargin = dp(2.0f);
            this.layoutModePill.setLayoutParams(layoutParams);
        }
        this.layoutModePill.setTranslationX(width2 - (max / 2.0f));
        int i = 0;
        while (i < 4) {
            this.layoutModeLabels[i].setSelected(i == layoutModeIndex());
            this.layoutModeLabels[i].setTextColor(i == layoutModeIndex() ? textPrimary() : textSecondary());
            i++;
        }
    }

    private void animateLayoutMode(final FrameLayout frameLayout) {
        ValueAnimator valueAnimator = this.layoutModeAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (reducedMotion()) {
            this.layoutModePosition = layoutModeIndex();
            placeLayoutPill(frameLayout);
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.layoutModePosition, layoutModeIndex());
        this.layoutModeAnimator = ofFloat;
        ofFloat.setDuration(280L);
        this.layoutModeAnimator.setInterpolator(new PathInterpolator(0.22f, 0.25f, 0.0f, 1.0f));
        this.layoutModeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda83
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                MainActivity.this.lambda$animateLayoutMode$78(frameLayout, valueAnimator2);
            }
        });
        this.layoutModeAnimator.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$animateLayoutMode$78(FrameLayout frameLayout, ValueAnimator valueAnimator) {
        this.layoutModePosition = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        placeLayoutPill(frameLayout);
    }

    private void addLayoutRow(LinearLayout linearLayout, String str, String str2, String str3, boolean z, Runnable runnable) {
        addLayoutRow(linearLayout, str, str2, str3, z, runnable, null);
    }

    private void addLayoutRow(LinearLayout linearLayout, String str, String str2, String str3, boolean z, final Runnable runnable, String str4) {
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(16);
        horizontal.setPadding(0, dp(12.0f), 0, dp(12.0f));
        LinearLayout vertical = vertical();
        vertical.addView(label(str, 13.5f, textPrimary(), true), margin(0, 0, 0, 3));
        TextView label = label(str2, 11.0f, textSecondary(), false);
        label.setLineSpacing(dp(2.0f), 1.0f);
        vertical.addView(label);
        horizontal.addView(vertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (str4 != null) {
            Button button = tonalButton(str4, 12.0f);
            button.setMinHeight(dp(34.0f));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, dp(36.0f));
            layoutParams.setMarginStart(dp(10.0f));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda30
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$addLayoutRow$79(runnable, view);
                }
            });
            horizontal.addView(button, layoutParams);
        } else {
            TextView label2 = label(str3, 12.5f, tonalText(), true);
            label2.setMaxLines(1);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.setMarginStart(dp(10.0f));
            horizontal.addView(label2, layoutParams2);
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.ic_chevron_left);
            imageView.setScaleX(isArabic() ? 1.0f : -1.0f);
            imageView.setColorFilter(textSecondary(), PorterDuff.Mode.SRC_IN);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(dp(18.0f), dp(18.0f));
            layoutParams3.setMarginStart(dp(6.0f));
            horizontal.addView(imageView, layoutParams3);
        }
        if (!z) {
            horizontal.setAlpha(0.42f);
        }
        horizontal.setClickable(true);
        horizontal.setFocusable(true);
        horizontal.setHapticFeedbackEnabled(this.hapticsEnabled);
        horizontal.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda31
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$addLayoutRow$80(runnable, view);
            }
        });
        linearLayout.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$addLayoutRow$79(Runnable runnable, View view) {
        performTapFeedback(view);
        runnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$addLayoutRow$80(Runnable runnable, View view) {
        performTapFeedback(view);
        runnable.run();
    }

    private void buildLayoutPage(LinearLayout linearLayout) {
        pageAppBar(linearLayout, getString(R.string.text_322));
        LinearLayout vertical = vertical();
        vertical.addView(sectionLabel(getString(R.string.text_100)), margin(2, 0, 0, 8));
        vertical.addView(buildModeSegment(), margin(0, 0, 0, 12));
        LinearLayout vertical2 = vertical();
        this.layoutSettingsHost = vertical2;
        vertical.addView(vertical2);
        View buildLayoutPreviewCard = buildLayoutPreviewCard();
        if (wideLayout()) {
            addPair(linearLayout, vertical, buildLayoutPreviewCard);
        } else {
            linearLayout.addView(buildLayoutPreviewCard, margin(0, 0, 0, 20));
            linearLayout.addView(vertical);
        }
        refreshLayoutSettings();
    }

    private void refreshLayoutSettings() {
        LinearLayout linearLayout = this.layoutSettingsHost;
        if (linearLayout == null) {
            return;
        }
        linearLayout.removeAllViews();
        buildLayoutSettings(this.layoutSettingsHost);
        requestLayoutPreview();
    }

    private LinearLayout buildLayoutPreviewCard() {
        LinearLayout card = card();
        card.setPadding(dp(16.0f), dp(16.0f), dp(16.0f), dp(16.0f));
        card.addView(label(getString(R.string.text_323), 16.0f, textPrimary(), true));
        TextView label = label(this.selectedDocumentName.isEmpty() ? getString(R.string.text_324) : this.selectedDocumentName, 11.0f, textSecondary(), false);
        label.setMaxLines(2);
        label.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(label, margin(0, 5, 0, 12));
        FrameLayout frameLayout = new FrameLayout(this);
        this.layoutPreviewHost = frameLayout;
        frameLayout.setBackground(flat(this.darkMode ? -14013905 : -855051, 12));
        this.layoutPreviewHost.setPadding(dp(9.0f), dp(9.0f), dp(9.0f), dp(9.0f));
        ImageView imageView = new ImageView(this);
        this.layoutPreviewImage = imageView;
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.layoutPreviewImage.setContentDescription(getString(R.string.text_325));
        this.layoutPreviewHost.addView(this.layoutPreviewImage, new FrameLayout.LayoutParams(-1, -1));
        TextView label2 = label(getString(R.string.text_326), 13.0f, textPrimary(), false);
        this.layoutPreviewStatus = label2;
        label2.setGravity(17);
        this.layoutPreviewStatus.setPadding(dp(16.0f), dp(12.0f), dp(16.0f), dp(12.0f));
        this.layoutPreviewHost.addView(this.layoutPreviewStatus, new FrameLayout.LayoutParams(-1, -1));
        card.addView(this.layoutPreviewHost, new LinearLayout.LayoutParams(-1, dp(wideLayout() ? 350 : 240)));
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(16);
        this.layoutPrevious = tonalButton(getString(R.string.text_088), 12.0f);
        this.layoutNext = tonalButton(getString(R.string.text_089), 12.0f);
        TextView label3 = label("", 11.0f, textSecondary(), true);
        this.layoutPreviewCount = label3;
        label3.setGravity(17);
        this.layoutPrevious.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda98
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildLayoutPreviewCard$81(view);
            }
        });
        this.layoutNext.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda109
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$buildLayoutPreviewCard$82(view);
            }
        });
        horizontal.addView(this.layoutPrevious, new LinearLayout.LayoutParams(-2, dp(44.0f)));
        horizontal.addView(this.layoutPreviewCount, new LinearLayout.LayoutParams(0, -2, 1.0f));
        horizontal.addView(this.layoutNext, new LinearLayout.LayoutParams(-2, dp(44.0f)));
        card.addView(horizontal, margin(0, 12, 0, 0));
        return card;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutPreviewCard$81(View view) {
        this.currentPreviewPage = Math.max(0, this.currentPreviewPage - 1);
        requestLayoutPreview();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutPreviewCard$82(View view) {
        this.currentPreviewPage++;
        requestLayoutPreview();
    }

    private void setLayoutNavigation(boolean z, boolean z2) {
        this.layoutPrevious.setEnabled(z);
        this.layoutPrevious.setAlpha(z ? 1.0f : 0.38f);
        this.layoutNext.setEnabled(z2);
        this.layoutNext.setAlpha(z2 ? 1.0f : 0.38f);
    }

    private void layoutPreviewError(String str) {
        this.layoutPreviewImage.setImageDrawable(null);
        this.layoutPreviewStatus.setText(str);
        this.layoutPreviewStatus.setVisibility(0);
        this.layoutPreviewCount.setText("");
        setLayoutNavigation(false, false);
    }

    private void requestLayoutPreview() {
        DocumentRenderer documentRenderer;
        MainActivity mainActivity;
        final int i = this.layoutPreviewGeneration + 1;
        this.layoutPreviewGeneration = i;
        Future<?> future = this.layoutPreviewTask;
        if (future != null) {
            future.cancel(true);
        }
        ImageView imageView = this.layoutPreviewImage;
        if (imageView == null) {
            return;
        }
        imageView.setImageDrawable(null);
        this.layoutPreviewStatus.setText(getString(R.string.text_326));
        this.layoutPreviewStatus.setVisibility(0);
        this.layoutPreviewCount.setText("");
        setLayoutNavigation(false, false);
        if (this.selectedDocumentUri == null || (documentRenderer = this.documentRenderer) == null) {
            layoutPreviewError(getString(R.string.text_327));
            return;
        }
        if (!documentRenderer.isSupported()) {
            layoutPreviewError(getString(R.string.text_328));
            return;
        }
        try {
            final ArrayList<SheetJob> planSheets = planSheets(resolvePageRange(this.pageRange, this.documentRenderer.getPageCount()), this.documentRenderer);
            if (planSheets.isEmpty()) {
                mainActivity = this;
                try {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.text_329));
                } catch (Exception unused) {
                    mainActivity.layoutPreviewError(mainActivity.getString(R.string.text_330));
                    return;
                }
            }
            this.currentPreviewPage = Math.max(0, Math.min(this.currentPreviewPage, planSheets.size() - 1));
            final SheetComposer.Spec buildSheetSpec = buildSheetSpec(Math.min(1000, Math.max(480, getResources().getDisplayMetrics().widthPixels / (wideLayout() ? 2 : 1))));
            final int i2 = this.currentPreviewPage;
            final Uri uri = this.selectedDocumentUri;
            final String str = this.selectedDocumentName;
            final String str2 = this.selectedDocumentMime;
            final ImageView imageView2 = this.layoutPreviewImage;
            final TextView textView = this.layoutPreviewStatus;
            final TextView textView2 = this.layoutPreviewCount;
            final Button button = this.layoutPrevious;
            final Button button2 = this.layoutNext;
            this.layoutPreviewTask = this.layoutPreviewExecutor.submit(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda13
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$requestLayoutPreview$85(uri, str, str2, buildSheetSpec, planSheets, i2, i, imageView2, textView, textView2, button, button2);
                }
            });
        } catch (Exception unused2) {
            mainActivity = this;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$requestLayoutPreview$85(Uri uri, String str, String str2, final SheetComposer.Spec spec, final ArrayList arrayList, final int i, final int i2, final ImageView imageView, final TextView textView, final TextView textView2, final Button button, final Button button2) {
        try {
            DocumentRenderer documentRenderer = new DocumentRenderer(this, uri, str, str2);
            try {
                if (!Thread.currentThread().isInterrupted()) {
                    final Bitmap renderPreviewSheet = renderPreviewSheet(spec, (SheetJob) arrayList.get(i), documentRenderer);
                    runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda130
                        @Override // java.lang.Runnable
                        public final void run() {
                            MainActivity.this.lambda$requestLayoutPreview$83(i2, imageView, renderPreviewSheet, textView, textView2, spec, i, arrayList, button, button2);
                        }
                    });
                    documentRenderer.close();
                    return;
                }
                documentRenderer.close();
            } finally {
            }
        } catch (Exception unused) {
            runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda132
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$requestLayoutPreview$84(i2, imageView);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$requestLayoutPreview$83(int i, ImageView imageView, Bitmap bitmap, TextView textView, TextView textView2, SheetComposer.Spec spec, int i2, ArrayList arrayList, Button button, Button button2) {
        if (i != this.layoutPreviewGeneration || isFinishing() || isDestroyed() || !"layout".equals(this.currentPage) || !imageView.isAttachedToWindow()) {
            bitmap.recycle();
            return;
        }
        Bitmap bitmap2 = this.layoutPreviewBitmap;
        this.layoutPreviewBitmap = bitmap;
        imageView.setImageBitmap(bitmap);
        textView.setVisibility(8);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(spec.mode == SheetComposer.Mode.SIZE ? R.string.text_084 : R.string.text_331));
        int i3 = i2 + 1;
        sb.append(i3);
        sb.append(getString(R.string.text_016));
        sb.append(arrayList.size());
        textView2.setText(sb.toString());
        button.setEnabled(i2 > 0);
        button.setAlpha(i2 > 0 ? 1.0f : 0.38f);
        button2.setEnabled(i3 < arrayList.size());
        button2.setAlpha(i3 >= arrayList.size() ? 0.38f : 1.0f);
        if (bitmap2 == null || bitmap2 == bitmap || bitmap2.isRecycled()) {
            return;
        }
        bitmap2.recycle();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$requestLayoutPreview$84(int i, ImageView imageView) {
        if (i == this.layoutPreviewGeneration && imageView.isAttachedToWindow()) {
            layoutPreviewError(getString(R.string.text_332));
        }
    }

    private void buildLayoutSettings(LinearLayout linearLayout) {
        String string;
        Runnable runnable;
        Runnable runnable2;
        boolean equals = "size".equals(this.layoutMode);
        boolean equals2 = "poster".equals(this.layoutMode);
        boolean equals3 = "multiple".equals(this.layoutMode);
        boolean equals4 = "booklet".equals(this.layoutMode);
        LinearLayout card = card();
        card.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        String string2 = getString(R.string.text_333);
        if (equals) {
            string = getString(R.string.text_334);
        } else {
            string = getString(R.string.text_335);
        }
        addLayoutRow(card, string2, string, scaleModeLabel(), equals, equals ? new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda134
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showScaleModeOptions();
            }
        } : new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda140
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildLayoutSettings$86();
            }
        });
        addSeparator(card);
        addLayoutRow(card, getString(R.string.text_337), getString(R.string.text_338), this.customScalePercent + getString(R.string.text_300), equals && "custom".equals(this.scaleModePref), (equals && "custom".equals(this.scaleModePref)) ? new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda141
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showCustomScaleOptions();
            }
        } : new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildLayoutSettings$87();
            }
        });
        addSeparator(card);
        String string3 = getString(R.string.text_340);
        String string4 = getString(equals4 ? R.string.text_341 : R.string.text_342);
        String string5 = equals4 ? getString(R.string.text_312) : layoutOrientationLabel();
        boolean z = !equals4;
        if (!equals4) {
            runnable = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.showLayoutOrientationOptions();
                }
            };
        } else {
            runnable = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$buildLayoutSettings$88();
                }
            };
        }
        addLayoutRow(card, string3, string4, string5, z, runnable);
        addSeparator(card);
        String string6 = getString(R.string.text_344);
        String string7 = getString(R.string.text_345);
        String valueOf = String.valueOf(this.pagesPerSheet);
        if (equals3) {
            runnable2 = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda8
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.showPagesPerSheetOptions();
                }
            };
        } else {
            runnable2 = new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda9
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$buildLayoutSettings$89();
                }
            };
        }
        addLayoutRow(card, string6, string7, valueOf, equals3, runnable2);
        if (equals3 || equals2) {
            addSeparator(card);
            addLayoutRow(card, getString(R.string.text_347), getString(R.string.text_348), readingOrderLabel(), true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda10
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.showReadingOrderOptions();
                }
            });
        }
        if (equals2) {
            addSeparator(card);
            addLayoutRow(card, getString(R.string.text_349), getString(R.string.text_350), ((int) this.posterScalePercent) + getString(R.string.text_300), true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda11
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.showPosterScaleOptions();
                }
            });
            addSeparator(card);
            addSettingToggle(card, getString(R.string.text_351), getString(R.string.text_352), this.posterCutMarks, new ToggleAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda135
                @Override // com.mohamedelnaggar.hpp1005.MainActivity.ToggleAction
                public final void onToggle(boolean z2) {
                    MainActivity.this.lambda$buildLayoutSettings$90(z2);
                }
            });
            addSeparator(card);
            addLayoutRow(card, getString(R.string.text_353), getString(R.string.text_354), this.posterOverlapMm + getString(R.string.text_355), true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda136
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.showPosterOverlapOptions();
                }
            });
        }
        if (equals4) {
            addSeparator(card);
            addLayoutRow(card, getString(R.string.text_356), getString(R.string.text_357), bindingLabel(), true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda137
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.showBindingOptions();
                }
            });
        }
        linearLayout.addView(card, margin(0, 0, 0, 14));
        linearLayout.addView(sectionLabel(getString(R.string.text_358)), margin(2, 0, 0, 8));
        LinearLayout card2 = card();
        card2.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        addLayoutRow(card2, getString(R.string.text_359), getString(R.string.text_360), paperSourceLabel(), true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda138
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showPaperSourceOptions();
            }
        });
        addSeparator(card2);
        addLayoutRow(card2, getString(R.string.text_361), getString(R.string.text_362), sourceMapSummary(), true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda139
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildLayoutSettings$91();
            }
        }, getString(R.string.text_189));
        linearLayout.addView(card2, margin(0, 0, 0, 12));
        if (equals4) {
            TextView label = label(getString(R.string.text_363), 11.5f, textSecondary(), false);
            label.setLineSpacing(dp(3.0f), 1.0f);
            linearLayout.addView(label, margin(3, 0, 3, 10));
        }
        TextView label2 = label(getString(R.string.text_364), 11.5f, textSecondary(), false);
        label2.setLineSpacing(dp(3.0f), 1.0f);
        linearLayout.addView(label2, margin(3, 0, 3, 12));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutSettings$86() {
        systemToast(getString(R.string.text_336), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutSettings$87() {
        systemToast(getString(R.string.text_339), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutSettings$88() {
        systemToast(getString(R.string.text_343), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutSettings$89() {
        systemToast(getString(R.string.text_346), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutSettings$90(boolean z) {
        this.posterCutMarks = z;
        this.printPreferences.edit().putBoolean("poster_cut_marks", z).apply();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildLayoutSettings$91() {
        openPage("sourcemap");
    }

    private void buildSourceMapPage(LinearLayout linearLayout) {
        pageAppBar(linearLayout, getString(R.string.text_361));
        TextView label = label(getString(R.string.text_365), 12.0f, textSecondary(), false);
        label.setLineSpacing(dp(3.0f), 1.0f);
        linearLayout.addView(label, margin(3, 0, 3, 12));
        LinearLayout card = card();
        card.setPadding(dp(16.0f), dp(5.0f), dp(16.0f), dp(5.0f));
        addLayoutRow(card, getString(R.string.text_315), getString(R.string.text_366), this.sourceMapMain.isEmpty() ? getString(R.string.text_367) : this.sourceMapMain, true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda18
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildSourceMapPage$92();
            }
        });
        addSeparator(card);
        addLayoutRow(card, getString(R.string.text_316), getString(R.string.text_368), this.sourceMapPriority.isEmpty() ? getString(R.string.text_367) : this.sourceMapPriority, true, new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda19
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildSourceMapPage$93();
            }
        });
        linearLayout.addView(card, margin(0, 0, 0, 12));
        TextView label2 = label(getString(R.string.text_369), 11.5f, textSecondary(), false);
        label2.setLineSpacing(dp(3.0f), 1.0f);
        linearLayout.addView(label2, margin(3, 0, 3, 12));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSourceMapPage$92() {
        showSourceTrayOptions(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildSourceMapPage$93() {
        showSourceTrayOptions(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showScaleModeOptions() {
        String[] strArr = {getString(R.string.text_311), getString(R.string.text_308), getString(R.string.text_309), getString(R.string.text_370)};
        final String[] strArr2 = {"fit", "actual", "shrink", "custom"};
        int i = 0;
        for (int i2 = 0; i2 < 4; i2++) {
            if (strArr2[i2].equals(this.scaleModePref)) {
                i = i2;
            }
        }
        showSelectionDialog(getString(R.string.text_333), strArr, i, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda53
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i3) {
                MainActivity.this.lambda$showScaleModeOptions$94(strArr2, i3);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showScaleModeOptions$94(String[] strArr, int i) {
        this.scaleModePref = strArr[i];
        this.printPreferences.edit().putString("scale_mode", this.scaleModePref).apply();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showCustomScaleOptions() {
        LinearLayout vertical = vertical();
        vertical.setPadding(dp(14.0f), dp(0.0f), dp(14.0f), dp(0.0f));
        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setTextSize(15.0f);
        editText.setGravity(17);
        editText.setInputType(2);
        editText.setHint(getString(R.string.text_371));
        editText.setHintTextColor(textSecondary());
        editText.setTextColor(textPrimary());
        editText.setBackground(flat(this.darkMode ? -13552839 : -986892, 14));
        editText.setText(String.valueOf(this.customScalePercent));
        editText.setSelection(editText.getText().length());
        vertical.addView(editText, new LinearLayout.LayoutParams(-1, dp(52.0f)));
        int[] iArr = {25, 50, 75, 100, 150, 200};
        LinearLayout horizontal = horizontal();
        horizontal.setGravity(17);
        int i = 0;
        for (int i2 = 6; i < i2; i2 = 6) {
            final int i3 = iArr[i];
            TextView label = label(i3 + getString(R.string.text_300), 13.0f, this.darkMode ? -11756801 : -16747810, true);
            label.setGravity(17);
            label.setBackground(flat(this.darkMode ? -13552839 : -986892, 14));
            label.setClickable(true);
            label.setFocusable(true);
            label.setHapticFeedbackEnabled(this.hapticsEnabled);
            label.setOnClickListener(new View.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda126
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.lambda$showCustomScaleOptions$95(editText, i3, view);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(38.0f), 1.0f);
            layoutParams.setMarginStart(dp(6.0f));
            horizontal.addView(label, layoutParams);
            i++;
        }
        vertical.addView(horizontal, margin(0, 10, 0, 2));
        oneUIFormDialog(getString(R.string.text_337), getString(R.string.text_372), vertical, new FormApply() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda127
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.FormApply
            public final boolean apply() {
                boolean lambda$showCustomScaleOptions$96;
                lambda$showCustomScaleOptions$96 = MainActivity.this.lambda$showCustomScaleOptions$96(editText);
                return lambda$showCustomScaleOptions$96;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showCustomScaleOptions$95(EditText editText, int i, View view) {
        performTapFeedback(view);
        editText.setText(String.valueOf(i));
        editText.setSelection(editText.getText().length());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$showCustomScaleOptions$96(EditText editText) {
        try {
            applyCustomScalePercent(Integer.parseInt(editText.getText().toString().trim()));
            this.currentPreviewPage = 0;
            renderApp();
            return true;
        } catch (NumberFormatException unused) {
            systemToast(getString(R.string.text_373), 0).show();
            return false;
        }
    }

    private void applyCustomScalePercent(int i) {
        int max = Math.max(10, Math.min(400, i));
        this.customScalePercent = max;
        this.printPreferences.edit().putInt("custom_scale", max).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLayoutOrientationOptions() {
        String[] strArr = {getString(R.string.text_314), getString(R.string.text_313), getString(R.string.text_312)};
        final String[] strArr2 = {DebugKt.DEBUG_PROPERTY_VALUE_AUTO, "portrait", "landscape"};
        int i = 0;
        for (int i2 = 0; i2 < 3; i2++) {
            if (strArr2[i2].equals(this.layoutOrientation)) {
                i = i2;
            }
        }
        showSelectionDialog(getString(R.string.text_340), strArr, i, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda25
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i3) {
                MainActivity.this.lambda$showLayoutOrientationOptions$97(strArr2, i3);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showLayoutOrientationOptions$97(String[] strArr, int i) {
        this.layoutOrientation = strArr[i];
        this.printPreferences.edit().putString("layout_orientation", this.layoutOrientation).apply();
        this.currentPreviewPage = 0;
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPagesPerSheetOptions() {
        final String[] strArr = {"2", "4", "6", "9", "16"};
        int i = 0;
        for (int i2 = 0; i2 < 5; i2++) {
            if (Integer.parseInt(strArr[i2]) == this.pagesPerSheet) {
                i = i2;
            }
        }
        showSelectionDialog(getString(R.string.text_344), strArr, i, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda52
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i3) {
                MainActivity.this.lambda$showPagesPerSheetOptions$98(strArr, i3);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPagesPerSheetOptions$98(String[] strArr, int i) {
        this.pagesPerSheet = Integer.parseInt(strArr[i]);
        this.printPreferences.edit().putInt("pages_per_sheet", this.pagesPerSheet).apply();
        this.currentPreviewPage = 0;
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPaperSourceOptions() {
        String[] strArr = {getString(R.string.text_314), getString(R.string.text_315), getString(R.string.text_316)};
        final String[] strArr2 = {DebugKt.DEBUG_PROPERTY_VALUE_AUTO, "main", "priority"};
        int i = 0;
        for (int i2 = 0; i2 < 3; i2++) {
            if (strArr2[i2].equals(this.paperSource)) {
                i = i2;
            }
        }
        showSelectionDialog(getString(R.string.text_359), strArr, i, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda84
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i3) {
                MainActivity.this.lambda$showPaperSourceOptions$99(strArr2, i3);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPaperSourceOptions$99(String[] strArr, int i) {
        this.paperSource = strArr[i];
        this.printPreferences.edit().putString("paper_source", this.paperSource).apply();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPosterOverlapOptions() {
        int i = 2;
        final int[] iArr = {0, 2, 5, 8, 10};
        String[] strArr = {getString(R.string.text_374), getString(R.string.text_375), getString(R.string.text_376), getString(R.string.text_377), getString(R.string.text_378)};
        for (int i2 = 0; i2 < 5; i2++) {
            if (iArr[i2] == this.posterOverlapMm) {
                i = i2;
            }
        }
        showSelectionDialog(getString(R.string.text_353), strArr, i, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda97
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i3) {
                MainActivity.this.lambda$showPosterOverlapOptions$100(iArr, i3);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPosterOverlapOptions$100(int[] iArr, int i) {
        this.posterOverlapMm = iArr[i];
        this.printPreferences.edit().putInt("poster_overlap_mm", this.posterOverlapMm).apply();
        this.currentPreviewPage = 0;
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showReadingOrderOptions() {
        String[] strArr = {getString(R.string.text_379), getString(R.string.text_380)};
        final String[] strArr2 = {"rtl", "ltr"};
        boolean equals = "ltr".equals(this.readingOrder);
        showNativeDialog(dialogBuilder().setTitle(getString(R.string.text_347)).setSingleChoiceItems(strArr, equals ? 1 : 0, new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda24
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$showReadingOrderOptions$101(strArr2, dialogInterface, i);
            }
        }).setNegativeButton(getString(R.string.text_111), (DialogInterface.OnClickListener) null).create());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showReadingOrderOptions$101(String[] strArr, DialogInterface dialogInterface, int i) {
        this.readingOrder = strArr[i];
        this.printPreferences.edit().putString("reading_order", this.readingOrder).apply();
        dialogInterface.dismiss();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPosterScaleOptions() {
        String[] strArr = {getString(R.string.text_381), getString(R.string.text_382), getString(R.string.text_383), getString(R.string.text_384), getString(R.string.text_385), getString(R.string.text_386)};
        final float[] fArr = {100.0f, 150.0f, 200.0f, 250.0f, 300.0f, 400.0f};
        int i = 1;
        for (int i2 = 0; i2 < 6; i2++) {
            if (Math.abs(fArr[i2] - this.posterScalePercent) < 0.5f) {
                i = i2;
            }
        }
        showNativeDialog(dialogBuilder().setTitle(getString(R.string.text_349)).setSingleChoiceItems(strArr, i, new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda80
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i3) {
                MainActivity.this.lambda$showPosterScaleOptions$102(fArr, dialogInterface, i3);
            }
        }).setNegativeButton(getString(R.string.text_111), (DialogInterface.OnClickListener) null).create());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPosterScaleOptions$102(float[] fArr, DialogInterface dialogInterface, int i) {
        this.posterScalePercent = fArr[i];
        this.printPreferences.edit().putFloat("poster_scale_percent", this.posterScalePercent).apply();
        dialogInterface.dismiss();
        renderApp();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showBindingOptions() {
        String[] strArr = {getString(R.string.text_387), getString(R.string.text_251)};
        final String[] strArr2 = {"right", "left"};
        boolean equals = "left".equals(this.bookletBinding);
        showSelectionDialog(getString(R.string.text_356), strArr, equals ? 1 : 0, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda82
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i) {
                MainActivity.this.lambda$showBindingOptions$103(strArr2, i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showBindingOptions$103(String[] strArr, int i) {
        this.bookletBinding = strArr[i];
        this.printPreferences.edit().putString("booklet_binding", this.bookletBinding).apply();
        this.currentPreviewPage = 0;
        renderApp();
    }

    private void showSourceTrayOptions(final boolean z) {
        int length = this.paperSizes.length + 1;
        final String[] strArr = new String[length];
        int i = 0;
        strArr[0] = getString(R.string.text_367);
        String[] strArr2 = this.paperSizes;
        System.arraycopy(strArr2, 0, strArr, 1, strArr2.length);
        String str = z ? this.sourceMapMain : this.sourceMapPriority;
        for (int i2 = 1; i2 < length; i2++) {
            if (strArr[i2].equals(str)) {
                i = i2;
            }
        }
        showSelectionDialog(getString(z ? R.string.text_388 : R.string.text_389), strArr, i, new IndexAction() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda36
            @Override // com.mohamedelnaggar.hpp1005.MainActivity.IndexAction
            public final void onChosen(int i3) {
                MainActivity.this.lambda$showSourceTrayOptions$104(strArr, z, i3);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showSourceTrayOptions$104(String[] strArr, boolean z, int i) {
        String str = i == 0 ? "" : strArr[i];
        if (z) {
            this.sourceMapMain = str;
            this.printPreferences.edit().putString("source_map_main", str).apply();
        } else {
            this.sourceMapPriority = str;
            this.printPreferences.edit().putString("source_map_priority", str).apply();
        }
        renderApp();
    }

    private UsbDevice findConnectedPrinter() {
        return UsbPrinterIO.findPrinter(this.usbManager);
    }

    private void sendXqxFileToPrinter(File file, UsbDevice usbDevice) throws Exception {
        lambda$append$117(getString(R.string.text_390) + UsbPrinterIO.sendXqx(this.usbManager, file, usbDevice) + getString(R.string.text_391));
    }

    private boolean hasPrinterConnected() {
        Iterator<UsbDevice> it = this.usbManager.getDeviceList().values().iterator();
        while (it.hasNext()) {
            if (UsbPrinterIO.isP1005(it.next())) {
                return true;
            }
        }
        return false;
    }

    private void scanDevices() {
        clearLogWithHeader(getString(R.string.text_392));
        HashMap<String, UsbDevice> deviceList = this.usbManager.getDeviceList();
        lambda$append$117(getString(R.string.text_393) + deviceList.size());
        if (deviceList.isEmpty()) {
            lambda$append$117(getString(R.string.text_394));
            return;
        }
        for (UsbDevice usbDevice : deviceList.values()) {
            lambda$append$117(getString(R.string.text_395) + describe(usbDevice));
            if (this.usbManager.hasPermission(usbDevice)) {
                lambda$append$117(getString(R.string.text_396));
                inspectDevice(usbDevice);
            } else {
                lambda$append$117(getString(R.string.text_397));
                this.usbManager.requestPermission(usbDevice, this.permissionIntent);
            }
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 42 && i2 == -1 && intent != null && intent.getData() != null) {
            loadSelectedDocument(intent.getData());
            return;
        }
        if (i != 41 || i2 != -1 || intent == null || intent.getData() == null) {
            return;
        }
        lambda$append$117(getString(R.string.text_398) + intent.getData().getLastPathSegment());
        lambda$append$117(getString(R.string.text_399));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void verifyPrinterFirmware() {
        verifyPrinterFirmware(null);
    }

    private void verifyPrinterFirmware(final UsbResultCallback usbResultCallback) {
        UsbDevice foundDevice = null;
        for (UsbDevice candidate : this.usbManager.getDeviceList().values()) {
            if (candidate.getVendorId() == 1008 && candidate.getProductId() == 15639) {
                foundDevice = candidate;
                break;
            }
        }
        final UsbDevice usbDevice = foundDevice;
        if (usbDevice == null) {
            lambda$append$117(getString(R.string.text_400));
            if (usbResultCallback != null) {
                usbResultCallback.onResult(false, getString(R.string.text_401));
                return;
            }
            return;
        }
        if (!this.usbManager.hasPermission(usbDevice)) {
            lambda$append$117(getString(R.string.text_402));
            if (usbResultCallback != null) {
                usbResultCallback.onResult(false, getString(R.string.text_403));
                return;
            }
            return;
        }
        new Thread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda78
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$verifyPrinterFirmware$107(usbDevice, usbResultCallback);
            }
        }, "P1005-Firmware-Verify").start();
        lambda$append$117(getString(R.string.text_412));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$verifyPrinterFirmware$107(UsbDevice usbDevice, final UsbResultCallback usbResultCallback) {
        int i;
        String str;
        UsbDeviceConnection usbDeviceConnection = null;
        try {
            try {
                usbDeviceConnection = this.usbManager.openDevice(usbDevice);
            } catch (Exception e) {
                final String str2 = e.getClass().getSimpleName() + ": " + e.getMessage();
                runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda129
                    @Override // java.lang.Runnable
                    public final void run() {
                        MainActivity.this.lambda$verifyPrinterFirmware$106(str2, usbResultCallback);
                    }
                });
                if (0 == 0) {
                    return;
                }
            }
            if (usbDeviceConnection == null) {
                throw new IllegalStateException(getString(R.string.text_404));
            }
            int i2 = 0;
            while (true) {
                if (i2 >= usbDevice.getInterfaceCount()) {
                    i = -1;
                    break;
                } else {
                    if (usbDevice.getInterface(i2).getInterfaceClass() == 7) {
                        i = usbDevice.getInterface(i2).getId();
                        break;
                    }
                    i2++;
                }
            }
            int i3 = i;
            if (i3 < 0) {
                throw new IllegalStateException(getString(R.string.text_405));
            }
            byte[] bArr = new byte[1024];
            int controlTransfer = usbDeviceConnection.controlTransfer(161, 0, 0, i3, bArr, 1024, 3000);
            if (controlTransfer < 2) {
                throw new IllegalStateException(getString(R.string.text_406) + controlTransfer + ").");
            }
            final String str3 = new String(bArr, 2, Math.min(Math.max(0, (((bArr[0] & UByte.MAX_VALUE) << 8) | (bArr[1] & UByte.MAX_VALUE)) - 2), controlTransfer - 2), StandardCharsets.US_ASCII);
            final boolean contains = str3.contains("FWVER:");
            if (contains) {
                str = getString(R.string.text_407) + str3 + getString(R.string.text_408);
            } else {
                str = getString(R.string.text_409) + str3 + getString(R.string.text_410);
            }
            final String str4 = str;
            runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda128
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$verifyPrinterFirmware$105(str4, usbResultCallback, contains, str3);
                }
            });
            if (usbDeviceConnection != null) {
                usbDeviceConnection.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                usbDeviceConnection.close();
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$verifyPrinterFirmware$105(String str, UsbResultCallback usbResultCallback, boolean z, String str2) {
        lambda$append$117(str);
        if (usbResultCallback != null) {
            usbResultCallback.onResult(z, str2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$verifyPrinterFirmware$106(String str, UsbResultCallback usbResultCallback) {
        lambda$append$117(getString(R.string.text_411) + str);
        if (usbResultCallback != null) {
            usbResultCallback.onResult(false, str);
        }
    }

    private void printBuiltInTestPage() {
        UsbDevice foundDevice = null;
        for (UsbDevice candidate : this.usbManager.getDeviceList().values()) {
            if (candidate.getVendorId() == 1008 && candidate.getProductId() == 15639) {
                foundDevice = candidate;
                break;
            }
        }
        final UsbDevice usbDevice = foundDevice;
        if (usbDevice == null) {
            lambda$append$117(getString(R.string.text_413));
        } else if (!this.usbManager.hasPermission(usbDevice)) {
            lambda$append$117(getString(R.string.text_414));
        } else {
            new Thread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda79
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$printBuiltInTestPage$110(usbDevice);
                }
            }, "P1005-Test-Print").start();
            lambda$append$117(getString(R.string.text_422));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$printBuiltInTestPage$110(UsbDevice usbDevice) {
        UsbDeviceConnection connection = null;
        UsbInterface printerInterface = null;
        boolean claimed = false;
        try {
            InputStream openRawResource = getResources().openRawResource(R.raw.p1005_test_page);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[8192];
            while (true) {
                int read = openRawResource.read(bArr);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
            openRawResource.close();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (byteArray.length < 16) {
                throw new IllegalStateException(getString(R.string.text_415));
            }
            connection = this.usbManager.openDevice(usbDevice);
            if (connection == null) {
                throw new IllegalStateException(getString(R.string.text_416));
            }
            for (int i2 = 0; i2 < usbDevice.getInterfaceCount(); i2++) {
                UsbInterface candidate = usbDevice.getInterface(i2);
                if (candidate.getInterfaceClass() == 7) {
                    printerInterface = candidate;
                    break;
                }
            }
            if (printerInterface == null) {
                throw new IllegalStateException(getString(R.string.text_001));
            }
            claimed = connection.claimInterface(printerInterface, true);
            if (!claimed) {
                throw new IllegalStateException(getString(R.string.text_002));
            }
            UsbEndpoint usbEndpoint = null;
            for (int i3 = 0; i3 < printerInterface.getEndpointCount(); i3++) {
                UsbEndpoint endpoint = printerInterface.getEndpoint(i3);
                if (endpoint.getDirection() == 0 && endpoint.getType() == 2) {
                    usbEndpoint = endpoint;
                    break;
                }
            }
            if (usbEndpoint == null) {
                throw new IllegalStateException(getString(R.string.text_003));
            }
            int i = 0;
            while (i < byteArray.length) {
                int bulkTransfer = connection.bulkTransfer(usbEndpoint, byteArray, i, Math.min(16384, byteArray.length - i), 10000);
                if (bulkTransfer <= 0) {
                    throw new IllegalStateException(getString(R.string.text_417) + i + getString(R.string.text_016) + byteArray.length + getString(R.string.text_418) + bulkTransfer + ").");
                }
                i += bulkTransfer;
            }
            final int sentTotal = i;
            runOnUiThread(new Runnable() {
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$printBuiltInTestPage$108(sentTotal);
                }
            });
        } catch (Exception e) {
            final String str = e.getClass().getSimpleName() + ": " + e.getMessage();
            runOnUiThread(new Runnable() {
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$printBuiltInTestPage$109(str);
                }
            });
        } finally {
            if (connection != null) {
                if (claimed && printerInterface != null) {
                    try {
                        connection.releaseInterface(printerInterface);
                    } catch (Exception unused) {
                    }
                }
                connection.close();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$printBuiltInTestPage$108(int i) {
        lambda$append$117(getString(R.string.text_419) + i + getString(R.string.text_420));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$printBuiltInTestPage$109(String str) {
        lambda$append$117(getString(R.string.text_421) + str);
    }

    private void loadBuiltInFirmware() {
        loadBuiltInFirmware(null);
    }

    private void loadBuiltInFirmware(final UsbResultCallback usbResultCallback) {
        UsbDevice foundDevice = null;
        for (UsbDevice candidate : this.usbManager.getDeviceList().values()) {
            if (candidate.getVendorId() == 1008 && candidate.getProductId() == 15639) {
                foundDevice = candidate;
                break;
            }
        }
        final UsbDevice usbDevice = foundDevice;
        if (usbDevice == null) {
            lambda$append$117(getString(R.string.text_423));
            if (usbResultCallback != null) {
                usbResultCallback.onResult(false, getString(R.string.text_401));
                return;
            }
            return;
        }
        if (!this.usbManager.hasPermission(usbDevice)) {
            lambda$append$117(getString(R.string.text_424));
            if (usbResultCallback != null) {
                usbResultCallback.onResult(false, getString(R.string.text_403));
                return;
            }
            return;
        }
        new Thread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda81
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$loadBuiltInFirmware$113(usbDevice, usbResultCallback);
            }
        }, "P1005-Firmware-Transfer").start();
        lambda$append$117(getString(R.string.text_429));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadBuiltInFirmware$113(UsbDevice usbDevice, final UsbResultCallback usbResultCallback) {
        try {
            final int sendBuiltInFirmware = sendBuiltInFirmware(usbDevice);
            runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.8
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$loadBuiltInFirmware$111(sendBuiltInFirmware, usbResultCallback);
                }
            });
        } catch (Exception e) {
            final String str = e.getClass().getSimpleName() + ": " + e.getMessage();
            runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity.9
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$loadBuiltInFirmware$112(str, usbResultCallback);
                }
            });
        }
    }

    private int sendBuiltInFirmware(UsbDevice usbDevice) throws Exception {
        return PrinterFirmware.send(this, this.usbManager, usbDevice);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadBuiltInFirmware$111(int i, UsbResultCallback usbResultCallback) {
        lambda$append$117(getString(R.string.text_426) + i + getString(R.string.text_427));
        if (usbResultCallback != null) {
            usbResultCallback.onResult(true, String.valueOf(i));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadBuiltInFirmware$112(String str, UsbResultCallback usbResultCallback) {
        lambda$append$117(getString(R.string.text_428) + str);
        if (usbResultCallback != null) {
            usbResultCallback.onResult(false, str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectFirmwareFile() {
        try {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.setType("*/*");
            intent.addCategory("android.intent.category.OPENABLE");
            startActivityForResult(intent, 41);
        } catch (Exception e) {
            lambda$append$117(getString(R.string.text_430) + e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestFirmwareLoad() {
        if (!this.confirmFirmwareLoad) {
            loadBuiltInFirmware();
        } else {
            showNativeDialog(dialogBuilder().setTitle(getString(R.string.text_431)).setMessage(getString(R.string.text_432)).setNegativeButton(getString(R.string.text_111), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda22
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).setPositiveButton(getString(R.string.text_433), new DialogInterface.OnClickListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda23
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.lambda$requestFirmwareLoad$115(dialogInterface, i);
                }
            }).create());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$requestFirmwareLoad$115(DialogInterface dialogInterface, int i) {
        loadBuiltInFirmware();
    }

    private void showNativeDialog(final AlertDialog alertDialog) {
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.getDecorView().setLayoutDirection(appDirection());
            window.addFlags(2);
            window.setDimAmount(0.45f);
            window.setBackgroundDrawable(new ColorDrawable(this.darkMode ? -232973023 : -218103809));
        }
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda125
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                MainActivity.this.lambda$showNativeDialog$116(alertDialog, dialogInterface);
            }
        });
        alertDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showNativeDialog$116(AlertDialog alertDialog, DialogInterface dialogInterface) {
        int primaryColor = primaryColor();
        alertDialog.getButton(-1).setTextColor(primaryColor);
        alertDialog.getButton(-2).setTextColor(primaryColor);
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextColor(textPrimary());
        }
        int identifier = getResources().getIdentifier("alertTitle", "id", "android");
        TextView textView2 = identifier == 0 ? null : (TextView) alertDialog.findViewById(identifier);
        if (textView2 != null) {
            textView2.setTextColor(textPrimary());
        }
    }

    private void copyActivityLog() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService("clipboard");
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(getString(R.string.text_050), this.activityLog.toString()));
            systemToast(getString(R.string.text_434), 0).show();
        }
    }

    private void clearActivityLog() {
        this.activityLog.setLength(0);
        this.activityLog.append(getString(R.string.text_435));
        TextView textView = this.output;
        if (textView != null) {
            textView.setText(this.activityLog.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String describe(UsbDevice usbDevice) {
        return usbDevice.getDeviceName() + " | VID=0x" + hex(usbDevice.getVendorId()) + " PID=0x" + hex(usbDevice.getProductId()) + " | class=" + usbDevice.getDeviceClass() + " subclass=" + usbDevice.getDeviceSubclass() + " protocol=" + usbDevice.getDeviceProtocol() + " | interfaces=" + usbDevice.getInterfaceCount();
    }

    private String hex(int i) {
        return String.format("%04X", Integer.valueOf(i));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void inspectDevice(UsbDevice usbDevice) {
        String str;
        lambda$append$117(getString(R.string.text_436));
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
            lambda$append$117(getString(R.string.text_437) + i + ": class=" + usbInterface.getInterfaceClass() + " subclass=" + usbInterface.getInterfaceSubclass() + " protocol=" + usbInterface.getInterfaceProtocol() + " endpoints=" + usbInterface.getEndpointCount());
            for (int i2 = 0; i2 < usbInterface.getEndpointCount(); i2++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i2);
                if (endpoint.getDirection() == 0) {
                    str = "OUT";
                } else {
                    str = endpoint.getDirection() == 128 ? "IN" : "OTHER";
                }
                lambda$append$117(getString(R.string.text_438) + i2 + ": address=0x" + hex(endpoint.getAddress()) + " direction=" + str + " type=" + endpoint.getType() + " maxPacket=" + endpoint.getMaxPacketSize());
            }
        }
        lambda$append$117(getString(R.string.text_439));
        maybeAutoPrepare();
    }

    private void clearLogWithHeader(String str) {
        this.activityLog.setLength(0);
        StringBuilder sb = this.activityLog;
        sb.append(str);
        sb.append("\n");
        TextView textView = this.output;
        if (textView != null) {
            textView.setText(this.activityLog.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: append, reason: merged with bridge method [inline-methods] */
    public void lambda$append$117(final String str) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(new Runnable() { // from class: com.mohamedelnaggar.hpp1005.MainActivity$$ExternalSyntheticLambda123
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.lambda$append$117(str);
                }
            });
            return;
        }
        StringBuilder sb = this.activityLog;
        sb.append(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
        sb.append("  ");
        sb.append(str);
        sb.append("\n");
        TextView textView = this.output;
        if (textView != null) {
            textView.setText(this.activityLog.toString());
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        MainDebugHooks.unregister(this);
        this.layoutPreviewGeneration++;
        Future<?> future = this.layoutPreviewTask;
        if (future != null) {
            future.cancel(true);
        }
        this.layoutPreviewExecutor.shutdownNow();
        ValueAnimator valueAnimator = this.layoutModeAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ImageView imageView = this.layoutPreviewImage;
        if (imageView != null) {
            imageView.setImageDrawable(null);
        }
        Bitmap bitmap = this.layoutPreviewBitmap;
        if (bitmap != null && !bitmap.isRecycled()) {
            this.layoutPreviewBitmap.recycle();
        }
        dismissCapsuleOverlay();
        this.autoPrepareHandler.removeCallbacks(this.autoPrepareRunnable);
        try {
            unregisterReceiver(this.usbReceiver);
        } catch (IllegalArgumentException unused) {
        }
        DocumentRenderer documentRenderer = this.documentRenderer;
        if (documentRenderer != null) {
            documentRenderer.close();
            this.documentRenderer = null;
        }
        Bitmap bitmap2 = this.currentPreviewBitmap;
        if (bitmap2 != null && !bitmap2.isRecycled()) {
            this.currentPreviewBitmap.recycle();
            this.currentPreviewBitmap = null;
        }
        super.onDestroy();
    }
}
