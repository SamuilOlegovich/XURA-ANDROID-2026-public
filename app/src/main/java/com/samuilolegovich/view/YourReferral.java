package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран отображения собственного реферального кода пользователя: позволяет
 * скопировать код в буфер обмена для дальнейшей передачи приглашаемому.
 */
@AndroidEntryPoint
public class YourReferral extends BaseActivity {
    public static final String YOUR_REFERRAL_CLASS = ".YourReferral";
    public static String MASSAGE = "";
    public static String CODE = "";

    private String CODE_COPIED_TO_PHONE_BUFFER;

    private MediaPlayer winMediaPlayer;

    private ClipboardManager clipboardManager;
    private ClipData clipData;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private TextView yourReferralPageTextView;
    private View copy;
    private TextView code;



    /** Инициализирует экран: разметка, View, локализация, слушатели, текст кода и сохранение реферального кода в настройках. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.your_referral_page);
        setButtons();
        setLanguage();
        listeners();
        goText();
        setReferralForApp(CODE);
        winMediaPlayer = MediaPlayer.create(this, R.raw.win);
        if (winMediaPlayer != null && AudioHelper.isSoundEnabled(this)) winMediaPlayer.start();
    }



    /** Находит View разметки экрана. */
    private void setButtons() {
        yourReferralPageTextView = (TextView) findViewById(R.id.your_referral_page_text_view);
        copy = findViewById(R.id.your_referral_page_copy_linc);
        code = (TextView) findViewById(R.id.your_referral_page_code);
    }


    /** Загружает локализованные строки: сообщение о копировании и заголовок экрана. */
    private void setLanguage() {
        CODE_COPIED_TO_PHONE_BUFFER = getString(R.string.code_copied_to_phone_buffer);
        yourReferralPageTextView.setText(R.string.your_referral_code);
    }


    /** Назначает обработчик копирования реферального кода в буфер обмена с показом снэкбара об успехе. */
    private void listeners() {
        View root = findViewById(android.R.id.content);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        copy.setOnClickListener(v -> {
            pulse(v);
            clipData = ClipData.newPlainText("text", CODE);
            clipboardManager.setPrimaryClip(clipData);
            showSnackbar(root, CODE_COPIED_TO_PHONE_BUFFER, SnackbarType.INFO);
        });
    }


    /** Сохраняет реферальный код пользователя в SharedPreferences приложения. */
    private void setReferralForApp(String referral) {
        preferences = PrefsHelper.get(this);
        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), referral);
        editor.apply();
    }


    /** Настройка для бегущей строки: подставляет реферальный код из статического поля CODE в текстовое поле. */
    @SuppressLint("SetTextI18n")
    private void goText() {
        code.setText(CODE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (winMediaPlayer != null && winMediaPlayer.isPlaying()) winMediaPlayer.pause();
    }

    @Override
    public void onBackPressed() {
        if (winMediaPlayer != null) { try { winMediaPlayer.stop(); } catch (Exception ignored) {} }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (winMediaPlayer != null) { winMediaPlayer.release(); winMediaPlayer = null; }
    }
}
