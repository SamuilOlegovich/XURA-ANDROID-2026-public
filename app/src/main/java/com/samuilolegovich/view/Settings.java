package com.samuilolegovich.view;

import static com.samuilolegovich.view.SelectLanguage.SELECT_LANGUAGE_CLASS;
import static com.samuilolegovich.view.SettingsSetPasswordForApp.SETTINGS_SET_PASSWORD_FOR_APP_CLASS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class Settings extends BaseActivity {
    public static final String SETTINGS_CLASS = ".Settings";

    @SuppressLint("StaticFieldLeak")
    public static volatile Settings SETTINGS_ACTIVITY;


    private Animation animTranslate;

    private TextView settingsSelectEnglishLinc;
    private TextView settingsSetPasswordLinc;
    private TextView settingsTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        SETTINGS_ACTIVITY = this;
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        settingsSelectEnglishLinc = (TextView) findViewById(R.id.settings_select_english_linc);
        settingsSetPasswordLinc = (TextView) findViewById(R.id.settings_set_password_linc);
        settingsTextView = (TextView) findViewById(R.id.settings_text_view);
    }


    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        settingsSelectEnglishLinc.setText(R.string.settings_select_language);
        settingsTextView.setText(R.string.settings_text);
        settingsSetPasswordLinc.setText(R.string.settings_set_password);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        settingsSetPasswordLinc.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(SETTINGS_SET_PASSWORD_FOR_APP_CLASS);
                    }
                }
        );

        settingsSelectEnglishLinc.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(SELECT_LANGUAGE_CLASS);
                    }
                }
        );
    }

    public void setLanguageThread() {
        new Thread() {
            public void run() {
                SETTINGS_ACTIVITY.runOnUiThread(new Runnable() {
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


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
