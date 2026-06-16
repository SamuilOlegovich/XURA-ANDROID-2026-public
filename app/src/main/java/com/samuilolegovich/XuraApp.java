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
 * Класс Application — точка входа Hilt DI-графа и держатель глобального состояния приложения:
 * статической ссылки на контекст приложения и трекера перехода между foreground/background,
 * который управляет автоблокировкой по неактивности (InactivityGuard).
 *
 * @author Samuil Olegovich
 * @since 2022
 */
@HiltAndroidApp
public class XuraApp extends Application {
    private static Application instance;

    /** Сохраняет глобальную ссылку на приложение и регистрирует трекер видимости Activity для автоблокировки. */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(new ForegroundTracker());
    }

    /** Возвращает контекст приложения для кода, у которого нет доступа к Activity/Context напрямую. */
    public static Context get() {
        return instance;
    }

    /** Считает запущенные Activity: переход 0→1 — приложение вышло на передний план, 1→0 — ушло в фон. */
    private static class ForegroundTracker implements ActivityLifecycleCallbacks {
        private int started = 0;

        /** При старте первой Activity сообщает InactivityGuard, что приложение видно пользователю. */
        @Override public void onActivityStarted(Activity a) {
            if (++started == 1) InactivityGuard.onForeground();
        }
        /** При остановке последней Activity сообщает InactivityGuard, что приложение ушло в фон (запускается отсчёт неактивности). */
        @Override public void onActivityStopped(Activity a) {
            if (--started == 0) InactivityGuard.onBackground();
        }
        @Override public void onActivityCreated(Activity a, Bundle b) {}
        @Override public void onActivityResumed(Activity a) {}
        @Override public void onActivityPaused(Activity a) {}
        @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
        @Override public void onActivityDestroyed(Activity a) {}
    }

    /** Возвращает Resources с языком из SharedPreferences; безопасно вызывать из фоновых потоков без Activity-контекста. */
    public static Resources getLocalizedResources() {
        SharedPreferences prefs = PrefsHelper.get(instance);
        String lang = prefs.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
        Configuration config = new Configuration(instance.getResources().getConfiguration());
        config.setLocale(new Locale(lang));
        return instance.createConfigurationContext(config).getResources();
    }
}