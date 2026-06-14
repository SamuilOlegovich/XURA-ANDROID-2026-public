package com.samuilolegovich.view;

import static com.samuilolegovich.MainActivity.MAIN_ACTIVITY;
import static com.samuilolegovich.view.SelectGame.SELECT_GAME_ACTIVITY;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import dagger.hilt.android.AndroidEntryPoint;





@AndroidEntryPoint
public class SelectLanguage extends BaseActivity {
    public static final String SELECT_LANGUAGE_CLASS = ".SelectLanguage";

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private View settingsRussianLinc;
    private View settingsEnglishLinc;
    private TextView settingsTextView;
    private TextView russianTitle;
    private TextView englishTitle;

    private String languageNow;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_language);
        setButtons();
        setLanguage();
        getSettingsLanguage();
        listeners();
    }



    private void setButtons() {
        settingsRussianLinc = findViewById(R.id.settings_russian_linc);
        settingsEnglishLinc = findViewById(R.id.settings_english_linc);
        settingsTextView    = findViewById(R.id.settings_text_view);
        russianTitle        = findViewById(R.id.russian_title);
        englishTitle        = findViewById(R.id.english_title);
    }


    private void setLanguage() {
        settingsTextView.setText(R.string.select_language_text);
    }


    private void getSettingsLanguage() {
        preferences = PrefsHelper.get(this);
        languageNow = preferences.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
        highlightSelectedLanguage();
    }


    private void highlightSelectedLanguage() {
        boolean isRussian = languageNow.equalsIgnoreCase(StringEnum.APP_RUSSIAN_LANGUAGE.getValue());

        settingsRussianLinc.setBackground(ContextCompat.getDrawable(this,
                isRussian ? R.drawable.bg_card_action_primary : R.drawable.bg_card_glass_clickable));
        settingsEnglishLinc.setBackground(ContextCompat.getDrawable(this,
                isRussian ? R.drawable.bg_card_glass_clickable : R.drawable.bg_card_action_primary));

        russianTitle.setTextColor(ContextCompat.getColor(this,
                isRussian ? R.color.xura_cyan : R.color.xura_text_primary));
        englishTitle.setTextColor(ContextCompat.getColor(this,
                isRussian ? R.color.xura_text_primary : R.color.xura_cyan));
    }


    private void listeners() {
        settingsRussianLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_RUSSIAN_LANGUAGE.getValue())) {
                makeStackThread(StringEnum.APP_RUSSIAN_LANGUAGE);
            }
        });

        settingsEnglishLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_ENGLISH_LANGUAGE.getValue())) {
                makeStackThread(StringEnum.APP_ENGLISH_LANGUAGE);
            }
        });
    }


    private void makeStackThread(StringEnum stringEnum) {
        AppExecutors.io().execute(() -> makeStack(stringEnum));
    }


    private void makeStack(StringEnum stringEnum) {
        preferences = PrefsHelper.get(this);
        preferences.edit()
                .putString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), stringEnum.getValue())
                .apply();

        // BaseActivity.applyLocale() прочитает новую локаль при recreate()
        runOnUiThread(this::recreate);
        if (MAIN_ACTIVITY != null)
            MAIN_ACTIVITY.runOnUiThread(MAIN_ACTIVITY::recreate);
        if (Settings.SETTINGS_ACTIVITY != null)
            Settings.SETTINGS_ACTIVITY.runOnUiThread(Settings.SETTINGS_ACTIVITY::recreate);
        if (SELECT_GAME_ACTIVITY != null)
            SELECT_GAME_ACTIVITY.runOnUiThread(SELECT_GAME_ACTIVITY::recreate);
    }


    public void setLanguageThread() {
        runOnUiThread(this::recreate);
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
