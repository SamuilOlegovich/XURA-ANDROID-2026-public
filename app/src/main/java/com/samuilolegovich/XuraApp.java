package com.samuilolegovich;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.samuilolegovich.enums.StringEnum;

import java.util.Locale;

import dagger.hilt.android.HiltAndroidApp;



@HiltAndroidApp
public class XuraApp extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context get() {
        return instance;
    }

    // Ресурсы с локалью из SharedPreferences — безопасно вызывать из фоновых потоков
    public static Resources getLocalizedResources() {
        SharedPreferences prefs = instance.getSharedPreferences(
                StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        String lang = prefs.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
        Configuration config = new Configuration(instance.getResources().getConfiguration());
        config.setLocale(new Locale(lang));
        return instance.createConfigurationContext(config).getResources();
    }
}