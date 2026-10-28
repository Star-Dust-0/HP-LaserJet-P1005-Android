package com.mohamedelnaggar.hpp1005;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

/* loaded from: classes.dex */
final class AppLanguage {
    static final String KEY = "app_language";

    AppLanguage() {
    }

    static String selected(Context context) {
        return "ar".equals(context.getSharedPreferences("hp_p1005_preferences", 0).getString(KEY, "en")) ? "ar" : "en";
    }

    static Context wrap(Context context, String str) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        Locale locale = new Locale("ar".equals(str) ? "ar" : "en");
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return context.createConfigurationContext(configuration);
    }
}
