package com.samuilolegovich.view;

import static com.samuilolegovich.view.SelectLanguage.SELECT_LANGUAGE_CLASS;
import static com.samuilolegovich.view.SettingsSetPasswordForApp.SETTINGS_SET_PASSWORD_FOR_APP_CLASS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.BiometricHelper;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class Settings extends BaseActivity {
    public static final String SETTINGS_CLASS = ".Settings";

    @SuppressLint("StaticFieldLeak")
    public static volatile Settings SETTINGS_ACTIVITY;


    private Animation animTranslate;

    private TextView settingsSelectEnglishLinc;
    private TextView settingsSetPasswordLinc;
    private TextView settingsBiometricLinc;
    private TextView settingsTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        SETTINGS_ACTIVITY = this;
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        settingsSelectEnglishLinc = (TextView) findViewById(R.id.settings_select_english_linc);
        settingsSetPasswordLinc = (TextView) findViewById(R.id.settings_set_password_linc);
        settingsBiometricLinc = (TextView) findViewById(R.id.settings_biometric_linc);
        settingsTextView = (TextView) findViewById(R.id.settings_text_view);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        settingsSelectEnglishLinc.setText(R.string.settings_select_language);
        settingsTextView.setText(R.string.settings_text);
        settingsSetPasswordLinc.setText(R.string.settings_set_password);
        updateBiometricButton();
    }


    private void updateBiometricButton() {
        String state = isBiometricEnabled() ? "  ●  ON" : "  ○  OFF";
        settingsBiometricLinc.setText(getString(R.string.settings_biometric) + state);
    }

    private boolean isBiometricEnabled() {
        return "true".equalsIgnoreCase(
                PrefsHelper.get(this).getString(
                        StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(), "false"));
    }

    private void saveBiometricEnabled(boolean enabled) {
        PrefsHelper.get(this).edit()
                .putString(StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(),
                        enabled ? "true" : "false")
                .apply();
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        settingsSetPasswordLinc.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(SETTINGS_SET_PASSWORD_FOR_APP_CLASS);
        });

        settingsSelectEnglishLinc.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(SELECT_LANGUAGE_CLASS);
        });

        settingsBiometricLinc.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            handleBiometricToggle();
        });
    }

    private void handleBiometricToggle() {
        if (!BiometricHelper.isAvailable(this)) {
            Toast.makeText(this,
                    "Biometrics not set up on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBiometricEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settings_biometric))
                    .setMessage("Disable biometrics?")
                    .setPositiveButton("DISABLE", (d, w) -> {
                        saveBiometricEnabled(false);
                        updateBiometricButton();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        } else {
            BiometricHelper.prompt(this,
                    getString(R.string.biometric_prompt_title),
                    getString(R.string.biometric_prompt_subtitle),
                    new BiometricHelper.Callback() {
                        @Override public void onSuccess() {
                            saveBiometricEnabled(true);
                            updateBiometricButton();
                        }
                        @Override public void onFallback() { /* отменил */ }
                        @Override public void onError(String message) { /* ошибка */ }
                    });
        }
    }

    public void setLanguageThread() {
        runOnUiThread(this::recreate);
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SETTINGS_ACTIVITY = null;
    }

}
