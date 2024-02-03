package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;



public class YourReferral  extends AppCompatActivity {
    public static final String YOUR_REFERRAL_CLASS = ".YourReferral";
    public static String MASSAGE = "";
    public static String CODE = "";

    private ClipboardManager clipboardManager;
    private MediaPlayer erMediaPlayer;
    private Animation animTranslate;
    private ClipData clipData;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private TextView copy;
    private TextView code;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.your_referral);
        setButtons();
        listeners();
        goText();
        setReferralForApp(CODE);
    }



    private void setButtons() {
        erMediaPlayer = MediaPlayer.create(this, R.raw.error);
        code = (TextView) findViewById(R.id.code);
        copy = (TextView) findViewById(R.id.copy);

        erMediaPlayer.start();
    }



    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

        copy.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        clipData = ClipData.newPlainText("text", CODE);
                        clipboardManager.setPrimaryClip(clipData);
                        makeToast("CODE COPIED TO PHONE BUFFER ");
                    }
                }
        );
    }



    private void setReferralForApp(String referral) {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
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
