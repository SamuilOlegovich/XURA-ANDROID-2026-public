package com.samuilolegovich.view;

import static com.samuilolegovich.MainActivity.MAIN_ACTIVITY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;

import java.util.Locale;



public class SelectLanguage extends AppCompatActivity {
    public static final String SELECT_LANGUAGE_CLASS = ".SelectLanguage";

    @SuppressLint("StaticFieldLeak")
    public static volatile SelectLanguage SELECT_LANGUAGE_ACTIVITY;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private TextView settingsRussianLinc;
    private TextView settingsEnglishLinc;
    private TextView settingsTextView;

    private String languageNow;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.select_language);
        SELECT_LANGUAGE_ACTIVITY = this;
        setButtons();
        setLanguage();
        getSettingsLanguage();
        listeners();
    }



    private void setButtons() {
        settingsRussianLinc = (TextView) findViewById(R.id.settings_russian_linc);
        settingsEnglishLinc = (TextView) findViewById(R.id.settings_english_linc);
        settingsTextView = (TextView) findViewById(R.id.settings_text_view);
    }


    private void setLanguage() {
        settingsRussianLinc.setText(R.string.select_language_russian);
        settingsEnglishLinc.setText(R.string.select_language_english);
        settingsTextView.setText(R.string.select_language_text);
    }


    private void getSettingsLanguage() {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        languageNow = preferences.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        settingsRussianLinc.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);

                        if (!languageNow.equalsIgnoreCase(StringEnum.APP_RUSSIAN_LANGUAGE.getValue())) {
                            makeStackThread(StringEnum.APP_RUSSIAN_LANGUAGE);
                        }
                    }
                }
        );

        settingsEnglishLinc.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);

                        if (!languageNow.equalsIgnoreCase(StringEnum.APP_ENGLISH_LANGUAGE.getValue())) {
                            makeStackThread(StringEnum.APP_ENGLISH_LANGUAGE);
                        }
                    }
                }
        );
    }


    private void makeStackThread(StringEnum stringEnum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeStack(stringEnum);
            }
        }).start();
    }


    private void makeStack(StringEnum stringEnum) {
        MainActivity.newLocale = new Locale(stringEnum.getValue());
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);

        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), stringEnum.getValue());
        editor.apply();

        SelectLanguage.SELECT_LANGUAGE_ACTIVITY.setLanguageThread();
        MainActivity.MAIN_ACTIVITY.setLanguageThread();
        Settings.SETTINGS_ACTIVITY.setLanguageThread();
    }


    public void setLanguageThread() {
        new Thread() {
            public void run() {
                SELECT_LANGUAGE_ACTIVITY.runOnUiThread(new Runnable() {
                    public void run() {
                        executeRecreate();
                    }

                });
            }
        }.start();
    }


    private void executeRecreate() {
        recreate();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    // для закрытие этой активити и попадания на главную активити
    public void closeThisPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
