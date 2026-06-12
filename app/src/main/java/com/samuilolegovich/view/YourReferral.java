package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class YourReferral  extends BaseActivity {
    public static final String YOUR_REFERRAL_CLASS = ".YourReferral";
    public static String MASSAGE = "";
    public static String CODE = "";

    private String CODE_COPIED_TO_PHONE_BUFFER;


    private ClipboardManager clipboardManager;
    private MediaPlayer erMediaPlayer;
    private ClipData clipData;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private TextView yourReferralPageTextView;
    private TextView copy;
    private TextView code;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.your_referral_page);
        setButtons();
        setLanguage();
        listeners();
        goText();
        setReferralForApp(CODE);
    }



    private void setButtons() {
        yourReferralPageTextView = (TextView) findViewById(R.id.your_referral_page_text_view);
        copy = (TextView) findViewById(R.id.your_referral_page_copy_linc);
        code = (TextView) findViewById(R.id.your_referral_page_code);

        erMediaPlayer = MediaPlayer.create(this, R.raw.error);
        erMediaPlayer.start();

    }


    private void setLanguage() {
        CODE_COPIED_TO_PHONE_BUFFER = getString(R.string.code_copied_to_phone_buffer);
        yourReferralPageTextView.setText(R.string.your_referral_code);
        copy.setText(R.string.copy);
    }


    private void listeners() {
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        copy.setOnClickListener(v -> {
            pulse(v);
            clipData = ClipData.newPlainText("text", CODE);
            clipboardManager.setPrimaryClip(clipData);
            makeToast(CODE_COPIED_TO_PHONE_BUFFER);
        });
    }


    private void setReferralForApp(String referral) {
        preferences = PrefsHelper.get(this);
        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), referral);
        editor.apply();
    }


    private void makeToast(String massage) {
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
        toast.show();
    }


    // настройка для бегущей строки
    @SuppressLint("SetTextI18n")
    private void goText() {
        code.setText(CODE);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
