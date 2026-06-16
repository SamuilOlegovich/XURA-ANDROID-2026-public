package com.samuilolegovich;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.InactivityGuard;
import com.samuilolegovich.utils.PrefsHelper;

import java.util.Locale;

import dagger.hilt.android.HiltAndroidApp;



/**
 * @author Samuil Olegovich
 * @since 2022
 */
@HiltAndroidApp
public class XuraApp extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(new ForegroundTracker());
    }

    public static Context get() {
        return instance;
    }

    // Считает запущенные Activity: 0 → приложение в фоне, 1+ → на экране
    private static class ForegroundTracker implements ActivityLifecycleCallbacks {
        private int started = 0;

        @Override public void onActivityStarted(Activity a) {
            if (++started == 1) InactivityGuard.onForeground();
        }
        @Override public void onActivityStopped(Activity a) {
            if (--started == 0) InactivityGuard.onBackground();
        }
        @Override public void onActivityCreated(Activity a, Bundle b) {}
        @Override public void onActivityResumed(Activity a) {}
        @Override public void onActivityPaused(Activity a) {}
        @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
        @Override public void onActivityDestroyed(Activity a) {}
    }

    // Ресурсы с локалью из SharedPreferences — безопасно вызывать из фоновых потоков
    public static Resources getLocalizedResources() {
        SharedPreferences prefs = PrefsHelper.get(instance);
        String lang = prefs.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
        Configuration config = new Configuration(instance.getResources().getConfiguration());
        config.setLocale(new Locale(lang));
        return instance.createConfigurationContext(config).getResources();
    }
}