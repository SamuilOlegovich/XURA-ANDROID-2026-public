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
    private TextView confirmTitle;
    private ImageView confirmIcon;
    private View confirm;

    private boolean hasPassword;
    private boolean disableMode = false;



    /** Этот экран открывается уже после прохождения блокировки приложения — повторная проверка не нужна. */
    @Override
    protected boolean isLockExempt() {
        return true;
    }

    /** Инициализирует экран: включает FLAG_SECURE, разметку, View, локализация, слушатели. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FLAG_SECURE наследуется от BaseActivity
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
        confirmTitle          = findViewById(R.id.confirm_link_title);
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
        setConfirmMode(false);
    }

    /**
     * Переключает кнопку между режимом "Подтвердить" (false) и "Отключить пароль" (true).
     * В режиме отключения — розовый outline, в обычном — розовый fill.
     */
    private void setConfirmMode(boolean disable) {
        disableMode = disable;
        if (disable) {
            confirm.setBackgroundResource(R.drawable.bg_card_glass_clickable);
            confirmTitle.setText(R.string.settings_disable_password);
            confirmTitle.setTextColor(getColor(R.color.xura_pink));
            confirmIcon.setImageResource(R.drawable.ic_lock_open);
            confirmIcon.setColorFilter(getColor(R.color.xura_pink));
        } else {
            confirm.setBackgroundResource(R.drawable.bg_card_send);
            confirmTitle.setText(R.string.set_password);
            confirmTitle.setTextColor(getColor(R.color.xura_pink));
            confirmIcon.setImageResource(hasPassword ? R.drawable.ic_lock : R.drawable.ic_lock_open);
            confirmIcon.clearColorFilter();
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


    /** Назначает обработчики кнопки подтверждения и watchers для переключения режима кнопки. */
    private void listeners() {
        confirm.setOnClickListener(v -> {
            pulse(v);

            if (disableMode) {
                // Режим отключения: старый пароль уже верный (кнопка в этом режиме только после успешной проверки)
                setPasswordForApp("", false);
                onBackPressed();
                return;
            }

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
                confirmIcon.setImageResource(R.drawable.ic_lock);
                confirm.postDelayed(this::onBackPressed, 350);
            } else {
                passwordOne.setText("");
                passwordTwo.setText("");
                tilPasswordTwo.setError(StringEnum.PASSWORD_DOES_NOT_MATCH.getValue());
            }
        });

        // Следим за полем текущего пароля — переключаем режим кнопки
        currentPasswordField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilCurrentPassword.setError(null);
                if (hasPassword) {
                    boolean oldCorrect = verifyCurrentPassword(s.toString());
                    boolean newEmpty = passwordOne.getText().toString().isEmpty()
                            && passwordTwo.getText().toString().isEmpty();
                    setConfirmMode(oldCorrect && newEmpty);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Если пользователь начал вводить новый пароль — возвращаем режим "Подтвердить"
        TextWatcher newPasswordWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPasswordTwo.setError(null);
                if (disableMode && !s.toString().isEmpty()) setConfirmMode(false);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        passwordOne.addTextChangedListener(newPasswordWatcher);
        passwordTwo.addTextChangedListener(newPasswordWatcher);
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
