package com.samuilolegovich;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.enums.StringEnum;

import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applyLocale();
        super.onCreate(savedInstanceState);
    }

    private void applyLocale() {
        SharedPreferences prefs = getSharedPreferences(
                StringEnum.APP_PREFERENCES.getValue(), MODE_PRIVATE);
        String lang = prefs.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
        Locale locale = new Locale(lang);

        // Синхронизируем статическое поле для кода, который читает MainActivity.newLocale
        MainActivity.newLocale = locale;

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
