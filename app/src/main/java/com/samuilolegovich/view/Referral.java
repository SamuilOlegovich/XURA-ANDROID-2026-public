package com.samuilolegovich.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран ввода реферального кода: пользователь может ввести код (проверяется на корректность
 * и допустимый диапазон) или пропустить этот шаг. Статический флаг {@link #FLAG} определяет,
 * куда вернуться после установки кода — на главный экран (онбординг) или просто назад
 * (повторная установка реферала из настроек/программы рефералов).
 */
@AndroidEntryPoint
public class Referral extends BaseActivity {
    public static final String REFERRAL_CLASS = ".Referral";
    /** true — экран открыт в потоке онбординга (после установки кода переходим на главный экран); false — открыт повторно, возвращаемся просто назад. */
    public static Boolean FLAG = true;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private TextInputLayout tilReferralCode;
    private EditText enterReferralCode;
    private TextView referralTextView;
    private View skip;
    private View set;



    /** Инициализирует экран: разметка, View, локализация, слушатели кнопок. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.referral);
        setButtons();
        setLanguage();
        listeners();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        referralTextView = (TextView) findViewById(R.id.referral_text_view);
        tilReferralCode = findViewById(R.id.til_referral_code_field);
        enterReferralCode = (EditText) findViewById(R.id.referral_code_field);
        skip = findViewById(R.id.referral_skip_linc);
        set = findViewById(R.id.referral_set_linc);
    }


    /** Устанавливает локализованный текст заголовка экрана. */
    private void setLanguage() {
        referralTextView.setText(R.string.referral_text);
    }


    /** Назначает обработчики: применить введённый код (с валидацией) или пропустить шаг — оба ведут дальше согласно флагу FLAG. */
    private void listeners() {
        set.setOnClickListener(v -> {
            pulse(v);
            String code = enterReferralCode.getText().toString();

            if (code.length() > 0
                    && Long.parseLong(code) < Long.parseLong(StringEnum.MAX_REFERRALS.getValue())) {
                setReferralForApp(code);
                if (FLAG) {
                    closeThisPage();
                } else {
                    onBackPressed();
                }
            } else {
                enterReferralCode.setText("");
                tilReferralCode.setError(StringEnum.REFERRAL_DOES_NOT_MATCH.getValue());
            }
        });

        skip.setOnClickListener(v -> {
            pulse(v);
            setReferralForApp(StringEnum.APP_PREFERENCES_REFERRAL_NOT_INSTALLED.getValue());
            if (FLAG) {
                closeThisPage();
            } else {
                onBackPressed();
            }
        });

        enterReferralCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { tilReferralCode.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }


    /** Сохраняет реферальный код (или признак его отсутствия) в зашифрованные preferences приложения. */
    private void setReferralForApp(String referral) {
        preferences = PrefsHelper.get(this);
        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), referral);
        editor.apply();
    }


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /** Закрывает текущий стек экранов и возвращает пользователя на главный экран приложения. */
    public void closeThisPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
