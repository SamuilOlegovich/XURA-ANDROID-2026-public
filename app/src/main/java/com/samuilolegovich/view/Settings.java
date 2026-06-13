package com.samuilolegovich.view;

import static com.samuilolegovich.view.BecomeReferral.BECOME_REFERRAL_CLASS;
import static com.samuilolegovich.view.InfoMain.INFO_MAIN_CLASS;
import static com.samuilolegovich.view.SelectLanguage.SELECT_LANGUAGE_CLASS;
import static com.samuilolegovich.view.SettingsSetPasswordForApp.SETTINGS_SET_PASSWORD_FOR_APP_CLASS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import androidx.core.content.ContextCompat;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.BiometricHelper;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.wallet.repository.WalletRepository;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class Settings extends BaseActivity {
    public static final String SETTINGS_CLASS = ".Settings";

    @SuppressLint("StaticFieldLeak")
    public static volatile Settings SETTINGS_ACTIVITY;

    @Inject WalletRepository repository;

    private View settingsSelectEnglishLinc;
    private View settingsSetPasswordLinc;
    private View settingsBiometricLinc;
    private TextView biometricTitleText;
    private android.widget.ImageView setPasswordIcon;
    private TextView settingsTextView;
    private MaterialCardView cardTestBalance;
    private TextView tvTestBalance;
    private MaterialButton btnResetTestBalance;
    private View btnGameMode;
    private TextView gameModeTitle;
    private android.widget.ImageView gameModeIcon;
    private View becomeReferralLinc;
    private View infoLinc;
    private View root;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        SETTINGS_ACTIVITY = this;
        root = findViewById(android.R.id.content);
        setButtons();
        setLanguage();
        listeners();
        setupBottomNav();
    }



    private void setButtons() {
        settingsSelectEnglishLinc = findViewById(R.id.settings_select_english_linc);
        settingsSetPasswordLinc = findViewById(R.id.settings_set_password_linc);
        settingsBiometricLinc = findViewById(R.id.settings_biometric_linc);
        biometricTitleText = findViewById(R.id.biometric_title);
        setPasswordIcon = findViewById(R.id.set_password_icon);
        settingsTextView = (TextView) findViewById(R.id.settings_text_view);
        cardTestBalance = findViewById(R.id.card_test_balance);
        tvTestBalance = findViewById(R.id.tv_test_balance);
        btnResetTestBalance = findViewById(R.id.btn_reset_test_balance);
        btnGameMode   = findViewById(R.id.settings_game_mode_linc);
        gameModeTitle = findViewById(R.id.game_mode_title);
        gameModeIcon  = findViewById(R.id.game_mode_icon);
        becomeReferralLinc = findViewById(R.id.become_referral_linc);
        infoLinc = findViewById(R.id.info_settings_linc);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        settingsTextView.setText(R.string.settings_text);
        updateBiometricButton();
        updateGameModeButton();
        updateTestBalanceCard();
        updatePasswordIcon();
    }

    @SuppressLint("SetTextI18n")
    private void updateGameModeButton() {
        boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
        String state = isReal ? "  ●  LIVE" : "  ○  TRIAL";
        gameModeTitle.setText(getString(R.string.settings_game_mode) + state);
        int bgRes   = isReal ? R.drawable.bg_card_gold      : R.drawable.bg_card_send;
        int colorRes = isReal ? R.color.xura_gold            : R.color.xura_pink;
        btnGameMode.setBackgroundResource(bgRes);
        int color = ContextCompat.getColor(this, colorRes);
        gameModeTitle.setTextColor(color);
        gameModeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void updateTestBalanceCard() {
        if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            cardTestBalance.setVisibility(View.VISIBLE);
            updateTestBalanceDisplay();
        } else {
            cardTestBalance.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTestBalanceDisplay() {
        java.math.BigDecimal balance = repository.getBalance();
        tvTestBalance.setText(balance.setScale(2, java.math.RoundingMode.DOWN).toPlainString() + " XRP");
    }


    private void updateBiometricButton() {
        String state = isBiometricEnabled() ? "  ●  ON" : "  ○  OFF";
        biometricTitleText.setText(getString(R.string.settings_biometric) + state);
    }

    private void updatePasswordIcon() {
        boolean hasPassword = isPasswordSet();
        setPasswordIcon.setImageResource(hasPassword ? R.drawable.ic_lock : R.drawable.ic_lock_open);
    }

    private boolean isPasswordSet() {
        String stored = PrefsHelper.get(this).getString(
                StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
        return stored != null
                && !stored.isEmpty()
                && !stored.equals(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
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

    private void saveGameMode(boolean isReal) {
        String value = isReal
                ? StringEnum.APP_GAME_MODE_REAL.getValue()
                : StringEnum.APP_GAME_MODE_TEST.getValue();
        PrefsHelper.get(this).edit()
                .putString(StringEnum.APP_GAME_MODE.getValue(), value)
                .apply();
        MainActivity.IS_REAL_GAME_MODE = isReal;
    }


    private void listeners() {
        settingsSetPasswordLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(SETTINGS_SET_PASSWORD_FOR_APP_CLASS);
        });

        settingsSelectEnglishLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(SELECT_LANGUAGE_CLASS);
        });

        settingsBiometricLinc.setOnClickListener(v -> {
            pulse(v);
            handleBiometricToggle();
        });

        btnGameMode.setOnClickListener(v -> {
            pulse(v);
            boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
            String title   = getString(isReal
                    ? R.string.settings_game_mode_switch_to_trial
                    : R.string.settings_game_mode_switch_to_live);
            String message = getString(isReal
                    ? R.string.settings_game_mode_trial_info
                    : R.string.settings_game_mode_live_warning);
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("CONFIRM", (d, w) -> {
                        saveGameMode(!isReal);
                        updateGameModeButton();
                        updateTestBalanceCard();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        });

        becomeReferralLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(BECOME_REFERRAL_CLASS);
        });

        infoLinc.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(INFO_MAIN_CLASS);
        });

        btnResetTestBalance.setOnClickListener(v -> {
            pulse(v);
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settings_test_mode_section))
                    .setMessage(getString(R.string.settings_reset_test_balance_confirm))
                    .setPositiveButton(getString(R.string.settings_reset_test_balance), (d, w) -> {
                        repository.resetTestBalance();
                        updateTestBalanceDisplay();
                        showSnackbar(root, getString(R.string.settings_reset_test_balance_done), SnackbarType.INFO);
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        });
    }

    private void handleBiometricToggle() {
        if (!BiometricHelper.isAvailable(this)) {
            showSnackbar(root, getString(R.string.biometrics_not_set_up), SnackbarType.ERROR);
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
