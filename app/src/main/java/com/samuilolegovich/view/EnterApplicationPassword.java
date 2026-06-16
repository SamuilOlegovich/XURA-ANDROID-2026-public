package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.BiometricHelper;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.utils.LegacyCipher;
import com.samuilolegovich.utils.PrefsHelper;


import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




// тут будет вводится и проверяться пароль от приложения для дальнейшего доступа к игре
@AndroidEntryPoint
public class EnterApplicationPassword extends BaseActivity {
    public static final String ENTER_APPLICATION_PASSWORD_CLASS = ".EnterApplicationPassword";

    private SharedPreferences preferences;

    private TextView settingsSetPasswordAppTextView;
    private TextInputLayout tilPassword;
    private EditText password;
    private View next;

    private boolean biometricPromptShown = false;



    @Override
    protected boolean isLockExempt() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.enter_application_password);
        preferences = PrefsHelper.get(this);

        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        settingsSetPasswordAppTextView = (TextView) findViewById(R.id.settings_set_password_app_text_view);
        tilPassword = findViewById(R.id.til_enter_application_password_field);
        password = (EditText) findViewById(R.id.enter_application_password_field);
        next = findViewById(R.id.enter_application_password_next_link);
    }


    private void setLanguage() {
        settingsSetPasswordAppTextView.setText(R.string.enter_password);
    }


    private void listeners() {
        next.setOnClickListener(v -> {
            pulse(v);
            String entered = password.getText().toString();

            if (verifyPassword(entered)) {
                MainActivity.START_FLAG = false;
                if (!preferences.contains(StringEnum.APP_PREFERENCES_SEED.getValue())) {
                    goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
                } else {
                    closeThisPage();
                }
            } else {
                tilPassword.setError(StringEnum.PASSWORD_DOES_NOT_MATCH.getValue());
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { tilPassword.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!biometricPromptShown) {
            biometricPromptShown = true;
            tryBiometric();
        }
    }

    private void tryBiometric() {
        boolean enabled = "true".equalsIgnoreCase(
                preferences.getString(StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(), "false"));
        if (!enabled || !BiometricHelper.isAvailable(this)) return;

        BiometricHelper.prompt(this,
                getString(R.string.biometric_prompt_title),
                getString(R.string.biometric_prompt_subtitle),
                new BiometricHelper.Callback() {
                    @Override public void onSuccess() { proceedAfterAuth(); }
                    @Override public void onFallback() { /* пользователь вводит пароль вручную */ }
                    @Override public void onError(String message) { /* вводит пароль вручную */ }
                });
    }

    private void proceedAfterAuth() {
        MainActivity.START_FLAG = false;
        if (!preferences.contains(StringEnum.APP_PREFERENCES_SEED.getValue())) {
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
        } else {
            closeThisPage();
        }
    }

    // true, если введённый пароль совпадает с сохранённым. Если сохранённый
    // хэш сделан старой (до PBKDF2) схемой, при совпадении пароль сразу
    // переписывается в новом формате — пользователю не нужно его сбрасывать.
    private boolean verifyPassword(String entered) {
        String storedSalt = preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(), "");
        String storedHash = preferences.getString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");

        if (LegacyCipher.isLegacySalt(storedSalt)) {
            if (!LegacyCipher.hash(entered, storedSalt, getAndroidId()).equals(storedHash)) {
                return false;
            }
            migrateToPbkdf2(entered);
            return true;
        }

        return Cipher.hashPassword(entered, storedSalt).equals(storedHash);
    }

    private void migrateToPbkdf2(String password) {
        String newSalt = Cipher.generateSalt();
        preferences.edit()
                .putString(StringEnum.APP_PREFERENCES_SALT.getValue(), newSalt)
                .putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), Cipher.hashPassword(password, newSalt))
                .apply();
    }

    @SuppressLint("HardwareIds")
    private String getAndroidId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        // оставляем пустым чтобы не работал возврат обратно
        // и не попадали на главную страницу кошелька
    }


    // для закрытие этой активити и попадания на главную активити
    public void closeThisPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
