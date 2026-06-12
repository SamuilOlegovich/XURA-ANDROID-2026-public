package com.samuilolegovich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.InactivityGuard;
import com.samuilolegovich.utils.PrefsHelper;

import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applyLocale();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAutoLock();
    }

    // Экраны ввода/смены пароля переопределяют это чтобы не попасть в петлю блокировки
    protected boolean isLockExempt() {
        return false;
    }

    private void checkAutoLock() {
        if (isLockExempt() || !InactivityGuard.isLockRequired()) return;

        SharedPreferences prefs = PrefsHelper.get(this);
        String password = prefs.getString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
        boolean hasPassword = !password.isEmpty()
                && !password.equalsIgnoreCase(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
        if (!hasPassword) return;

        InactivityGuard.reset();
        MainActivity.START_FLAG = true;
        startActivity(new Intent(".EnterApplicationPassword"));
    }

    private void applyLocale() {
        SharedPreferences prefs = PrefsHelper.get(this);
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
