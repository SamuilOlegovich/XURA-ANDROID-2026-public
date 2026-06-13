package com.samuilolegovich.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.BiometricHelper;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.utils.PrefsHelper;

import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




// тут устанавливаем пароль на приложение
@AndroidEntryPoint
public class SetAnAppPassword extends BaseActivity {
    public static final String SET_AN_APP_PASSWORD_CLASS = ".SetAnAppPassword";

    private EditText passwordOne;
    private EditText passwordTwo;
    private TextInputLayout tilPasswordTwo;

    private TextView settingsSetPasswordAppTextView;
    private TextView confirm;
    private TextView skip;



    @Override
    protected boolean isLockExempt() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.set_password_for_app_page);
        setButtons();
        setLanguage();
        listeners();
    }


    private void setButtons() {
        settingsSetPasswordAppTextView = (TextView) findViewById(R.id.settings_set_password_app_text_view);
        tilPasswordTwo = findViewById(R.id.til_settings_set_password_app_field_tow);
        passwordTwo = (EditText) findViewById(R.id.settings_set_password_app_field_tow);
        confirm = (TextView) findViewById(R.id.settings_set_password_app_confirm_link);
        passwordOne = (EditText) findViewById(R.id.settings_set_password_app_field);
        skip = (TextView) findViewById(R.id.settings_set_password_app_skip_linc);
    }


    private void setLanguage() {
        settingsSetPasswordAppTextView.setText(R.string.set_password_to_enter_application);
        confirm.setText(R.string.set_password);
        skip.setText(R.string.skip);
    }


    private void listeners() {
        confirm.setOnClickListener(v -> {
            pulse(v);
            String one = passwordOne.getText().toString();
            String two = passwordTwo.getText().toString();

            if (one.length() > 3 && one.equals(two)) {
                setPasswordForApp(one, true);
                offerBiometric();
            } else {
                passwordOne.setText("");
                passwordTwo.setText("");
                tilPasswordTwo.setError(StringEnum.PASSWORD_DOES_NOT_MATCH.getValue());
            }
        });

        skip.setOnClickListener(v -> {
            pulse(v);
            setPasswordForApp(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue(), false);
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
        });

        passwordTwo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { tilPasswordTwo.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    private void offerBiometric() {
        if (!BiometricHelper.isAvailable(this)) {
            saveBiometricEnabled(false);
            goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.biometric_dialog_title))
                .setMessage(getString(R.string.biometric_dialog_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.biometric_enable), (d, w) -> {
                    saveBiometricEnabled(true);
                    goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
                })
                .setNegativeButton(getString(R.string.biometric_skip), (d, w) -> {
                    saveBiometricEnabled(false);
                    goToAnotherPage(RESTORE_OR_NEW_WALLET_CLASS);
                })
                .show();
    }

    private void saveBiometricEnabled(boolean enabled) {
        PrefsHelper.get(this).edit()
                .putString(StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(),
                        enabled ? "true" : "false")
                .apply();
    }

    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    private void setPasswordForApp(String password, boolean b) {
        SharedPreferences.Editor edit = PrefsHelper.get(this).edit();
        if (b) {
            String salt = Cipher.generateSalt();
            edit.putString(StringEnum.APP_PREFERENCES_SALT.getValue(), salt);
            edit.putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                    Cipher.hashPassword(password, salt));
        } else {
            edit.putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                    StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
        }
        edit.apply();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        // оставляем пустым чтобы не работал возврат обратно
        // и не попадали на главную страницу кошелька
    }
}
