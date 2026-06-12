package com.samuilolegovich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.InactivityGuard;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.view.SelectGame;
import com.samuilolegovich.view.Settings;

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
        syncBottomNavSelection();
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

    // Вызывать из onCreate после setContentView в MainActivity, SelectGame, Settings
    protected void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav == null) return;

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_wallet && !(this instanceof MainActivity)) {
                navigateToTab(MainActivity.MAIN_ACTIVITY_CLASS);
            } else if (id == R.id.nav_games && !(this instanceof SelectGame)) {
                navigateToTab(SelectGame.SELECT_GAME_CLASS);
            } else if (id == R.id.nav_settings && !(this instanceof Settings)) {
                navigateToTab(Settings.SETTINGS_CLASS);
            }
            return true;
        });
    }

    // Вызывается в onResume — после onRestoreInstanceState, поэтому не перебивается
    private void syncBottomNavSelection() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav == null) return;

        int targetId;
        if (this instanceof MainActivity) {
            targetId = R.id.nav_wallet;
        } else if (this instanceof SelectGame) {
            targetId = R.id.nav_games;
        } else if (this instanceof Settings) {
            targetId = R.id.nav_settings;
        } else {
            return;
        }

        if (nav.getSelectedItemId() != targetId) {
            nav.setSelectedItemId(targetId);
        }
    }

    private void navigateToTab(String action) {
        Intent intent = new Intent(action);
        // Переиспользуем существующую Activity из стека без пересоздания
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        // Убираем анимацию перехода — это смена вкладки, не навигация вглубь
        overridePendingTransition(0, 0);
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
