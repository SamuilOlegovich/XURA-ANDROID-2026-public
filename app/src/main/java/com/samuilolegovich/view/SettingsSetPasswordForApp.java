package com.samuilolegovich.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран установки/смены пароля приложения из настроек: требует двойного ввода
 * совпадающего пароля, при успехе сохраняет новую соль и хеш пароля.
 */
@AndroidEntryPoint
public class SettingsSetPasswordForApp extends BaseActivity {
    public static final String SETTINGS_SET_PASSWORD_FOR_APP_CLASS = ".SettingsSetPasswordForApp";


    private EditText passwordOne;
    private EditText passwordTwo;
    private EditText currentPasswordField;
    private TextInputLayout tilPasswordTwo;
    private TextInputLayout tilCurrentPassword;
    private TextView textView;
    private ImageView confirmIcon;
    private View confirm;

    private boolean hasPassword;



    /** Этот экран открывается уже после прохождения блокировки приложения — повторная проверка не нужна. */
    @Override
    protected boolean isLockExempt() {
        return true;
    }

    /** Инициализирует экран: включает FLAG_SECURE, разметку, View, локализация, слушатели. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.samuilolegovich.BuildConfig.DEBUG) getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.settings_set_password_for_app);
        setButtons();
        setLanguage();
        listeners();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        textView              = findViewById(R.id.settings_set_password_app_text_view);
        tilPasswordTwo        = findViewById(R.id.til_edit_text_passport_tow);
        tilCurrentPassword    = findViewById(R.id.til_current_password);
        passwordTwo           = findViewById(R.id.edit_text_passport_tow);
        passwordOne           = findViewById(R.id.password_field);
        currentPasswordField  = findViewById(R.id.current_password_field);
        confirmIcon           = findViewById(R.id.confirm_link_icon);
        confirm               = findViewById(R.id.confirm_link);
    }


    /** Устанавливает заголовок и показывает/скрывает поле текущего пароля в зависимости от того, установлен ли пароль. */
    private void setLanguage() {
        hasPassword = isPasswordSet();
        if (hasPassword) {
            textView.setText(R.string.change_password_title);
            tilCurrentPassword.setVisibility(View.VISIBLE);
            confirmIcon.setImageResource(R.drawable.ic_lock);
        } else {
            textView.setText(R.string.set_password_to_enter_application);
            tilCurrentPassword.setVisibility(View.GONE);
            confirmIcon.setImageResource(R.drawable.ic_lock_open);
        }
    }

    /** Возвращает true, если пароль приложения уже установлен. */
    private boolean isPasswordSet() {
        String stored = PrefsHelper.get(this).getString(
                StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
        return stored != null
                && !stored.isEmpty()
                && !stored.equals(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
    }

    /** Проверяет, совпадает ли введённый пароль с сохранённым. */
    private boolean verifyCurrentPassword(String input) {
        if (input == null || input.isEmpty()) return false;
        SharedPreferences prefs = PrefsHelper.get(this);
        String stored = prefs.getString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
        String salt   = prefs.getString(StringEnum.APP_PREFERENCES_SALT.getValue(), "");
        if (stored == null || stored.isEmpty() || salt == null || salt.isEmpty()) return false;
        return stored.equals(Cipher.hashPassword(input, salt));
    }


    /** Назначает обработчик подтверждения пароля: при смене — сначала верифицирует старый, затем проверяет совпадение нового. После успеха замок закрывается и экран закрывается. */
    private void listeners() {
        confirm.setOnClickListener(v -> {
            pulse(v);

            // Если пароль уже установлен — проверяем старый
            if (hasPassword) {
                String current = currentPasswordField.getText().toString();
                if (!verifyCurrentPassword(current)) {
                    currentPasswordField.setText("");
                    tilCurrentPassword.setError(getString(R.string.wrong_current_password));
                    return;
                }
            }

            String one = passwordOne.getText().toString();
            String two = passwordTwo.getText().toString();

            if (one.length() > 3 && one.equals(two)) {
                setPasswordForApp(one, true);
                // Показываем закрытый замок как обратную связь, затем закрываем экран
                confirmIcon.setImageResource(R.drawable.ic_lock);
                confirm.postDelayed(this::onBackPressed, 350);
            } else {
                passwordOne.setText("");
                passwordTwo.setText("");
                tilPasswordTwo.setError(StringEnum.PASSWORD_DOES_NOT_MATCH.getValue());
            }
        });

        passwordTwo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { tilPasswordTwo.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });

        currentPasswordField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { tilCurrentPassword.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    /** Сохраняет пароль приложения: при b=true генерирует соль и хеширует пароль, при b=false помечает, что пароль не установлен. */
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


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
