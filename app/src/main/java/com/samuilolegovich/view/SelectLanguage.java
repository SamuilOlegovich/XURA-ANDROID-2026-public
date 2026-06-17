package com.samuilolegovich.view;

import static com.samuilolegovich.MainActivity.MAIN_ACTIVITY;
import static com.samuilolegovich.view.SelectGame.SELECT_GAME_ACTIVITY;

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



/**
 * Экран выбора языка интерфейса приложения: показывает список поддерживаемых языков,
 * подсвечивает текущий выбранный, сохраняет выбор в preferences и пересоздаёт открытые
 * экраны (главный, настройки, выбор игры), чтобы применить новую локаль сразу.
 */
@AndroidEntryPoint
public class SelectLanguage extends BaseActivity {
    public static final String SELECT_LANGUAGE_CLASS = ".SelectLanguage";

    private SharedPreferences preferences;

    private View settingsEnglishLinc;
    private View settingsRussianLinc;
    private View settingsChineseLinc;
    private View settingsHindiLinc;
    private View settingsSpanishLinc;
    private View settingsFrenchLinc;
    private View settingsGermanLinc;
    private View settingsArabicLinc;
    private View settingsPortugueseLinc;
    private View settingsBengaliLinc;

    private TextView settingsTextView;
    private TextView englishTitle;
    private TextView russianTitle;
    private TextView chineseTitle;
    private TextView hindiTitle;
    private TextView spanishTitle;
    private TextView frenchTitle;
    private TextView germanTitle;
    private TextView arabicTitle;
    private TextView portugueseTitle;
    private TextView bengaliTitle;

    private String languageNow;



    /** Инициализирует экран: разметка, View, локализация заголовка, подсветка текущего языка и слушатели карточек. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_language);
        setButtons();
        setLanguage();
        getSettingsLanguage();
        listeners();
    }



    /** Находит и сохраняет ссылки на все карточки языков и их подписи в разметке. */
    private void setButtons() {
        settingsEnglishLinc    = findViewById(R.id.settings_english_linc);
        settingsRussianLinc    = findViewById(R.id.settings_russian_linc);
        settingsChineseLinc    = findViewById(R.id.settings_chinese_linc);
        settingsHindiLinc      = findViewById(R.id.settings_hindi_linc);
        settingsSpanishLinc    = findViewById(R.id.settings_spanish_linc);
        settingsFrenchLinc     = findViewById(R.id.settings_french_linc);
        settingsGermanLinc     = findViewById(R.id.settings_german_linc);
        settingsArabicLinc     = findViewById(R.id.settings_arabic_linc);
        settingsPortugueseLinc = findViewById(R.id.settings_portuguese_linc);
        settingsBengaliLinc    = findViewById(R.id.settings_bengali_linc);

        settingsTextView = findViewById(R.id.settings_text_view);
        englishTitle     = findViewById(R.id.english_title);
        russianTitle     = findViewById(R.id.russian_title);
        chineseTitle     = findViewById(R.id.chinese_title);
        hindiTitle       = findViewById(R.id.hindi_title);
        spanishTitle     = findViewById(R.id.spanish_title);
        frenchTitle      = findViewById(R.id.french_title);
        germanTitle      = findViewById(R.id.german_title);
        arabicTitle      = findViewById(R.id.arabic_title);
        portugueseTitle  = findViewById(R.id.portuguese_title);
        bengaliTitle     = findViewById(R.id.bengali_title);
    }


    /** Устанавливает локализованный текст заголовка экрана. */
    private void setLanguage() {
        settingsTextView.setText(R.string.select_language_text);
    }


    /** Считывает текущий выбранный язык приложения из preferences (английский по умолчанию) и подсвечивает соответствующую карточку. */
    private void getSettingsLanguage() {
        preferences = PrefsHelper.get(this);
        languageNow = preferences.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
        highlightSelectedLanguage();
    }


    /** Проходит по всем карточкам языков и подсвечивает ту, что соответствует текущему выбранному языку. */
    private void highlightSelectedLanguage() {
        String lang = languageNow.toLowerCase();

        setCardHighlight(settingsEnglishLinc, englishTitle,
                lang.equals(StringEnum.APP_ENGLISH_LANGUAGE.getValue()));
        setCardHighlight(settingsRussianLinc, russianTitle,
                lang.equals(StringEnum.APP_RUSSIAN_LANGUAGE.getValue()));
        setCardHighlight(settingsChineseLinc, chineseTitle,
                lang.equals(StringEnum.APP_CHINESE_LANGUAGE.getValue()));
        setCardHighlight(settingsHindiLinc, hindiTitle,
                lang.equals(StringEnum.APP_HINDI_LANGUAGE.getValue()));
        setCardHighlight(settingsSpanishLinc, spanishTitle,
                lang.equals(StringEnum.APP_SPANISH_LANGUAGE.getValue()));
        setCardHighlight(settingsFrenchLinc, frenchTitle,
                lang.equals(StringEnum.APP_FRENCH_LANGUAGE.getValue()));
        setCardHighlight(settingsGermanLinc, germanTitle,
                lang.equals(StringEnum.APP_GERMAN_LANGUAGE.getValue()));
        setCardHighlight(settingsArabicLinc, arabicTitle,
                lang.equals(StringEnum.APP_ARABIC_LANGUAGE.getValue()));
        setCardHighlight(settingsPortugueseLinc, portugueseTitle,
                lang.equals(StringEnum.APP_PORTUGUESE_LANGUAGE.getValue()));
        setCardHighlight(settingsBengaliLinc, bengaliTitle,
                lang.equals(StringEnum.APP_BENGALI_LANGUAGE.getValue()));
    }


    /** Меняет фон и цвет текста карточки языка в зависимости от того, выбран ли этот язык сейчас. */
    private void setCardHighlight(View card, TextView title, boolean selected) {
        card.setBackground(ContextCompat.getDrawable(this,
                selected ? R.drawable.bg_card_action_primary : R.drawable.bg_card_glass_clickable));
        title.setTextColor(ContextCompat.getColor(this,
                selected ? R.color.xura_cyan : R.color.xura_text_primary));
    }


    /** Назначает обработчики клика на каждую карточку языка: если язык отличается от текущего — запускает смену локали. */
    private void listeners() {
        settingsEnglishLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_ENGLISH_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_ENGLISH_LANGUAGE);
        });
        settingsRussianLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_RUSSIAN_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_RUSSIAN_LANGUAGE);
        });
        settingsChineseLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_CHINESE_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_CHINESE_LANGUAGE);
        });
        settingsHindiLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_HINDI_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_HINDI_LANGUAGE);
        });
        settingsSpanishLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_SPANISH_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_SPANISH_LANGUAGE);
        });
        settingsFrenchLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_FRENCH_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_FRENCH_LANGUAGE);
        });
        settingsGermanLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_GERMAN_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_GERMAN_LANGUAGE);
        });
        settingsArabicLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_ARABIC_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_ARABIC_LANGUAGE);
        });
        settingsPortugueseLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_PORTUGUESE_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_PORTUGUESE_LANGUAGE);
        });
        settingsBengaliLinc.setOnClickListener(v -> {
            pulse(v);
            if (!languageNow.equalsIgnoreCase(StringEnum.APP_BENGALI_LANGUAGE.getValue()))
                makeStackThread(StringEnum.APP_BENGALI_LANGUAGE);
        });
    }


    /** Запускает сохранение выбранного языка на фоновом потоке. */
    private void makeStackThread(StringEnum stringEnum) {
        AppExecutors.io().execute(() -> makeStack(stringEnum));
    }


    /** Сохраняет выбранный язык в preferences и пересоздаёт текущий и все открытые экраны приложения (главный, настройки, выбор игры), чтобы изменение локали отразилось сразу. */
    private void makeStack(StringEnum stringEnum) {
        preferences = PrefsHelper.get(this);
        preferences.edit()
                .putString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), stringEnum.getValue())
                .apply();

        runOnUiThread(() -> findViewById(android.R.id.content).postDelayed(this::finish, 150));
        if (MAIN_ACTIVITY != null)
            MAIN_ACTIVITY.runOnUiThread(MAIN_ACTIVITY::recreate);
        if (Settings.SETTINGS_ACTIVITY != null)
            Settings.SETTINGS_ACTIVITY.runOnUiThread(Settings.SETTINGS_ACTIVITY::recreate);
        if (SELECT_GAME_ACTIVITY != null)
            SELECT_GAME_ACTIVITY.runOnUiThread(SELECT_GAME_ACTIVITY::recreate);
    }


    /** Пересоздаёт текущую активити на UI-потоке (вызывается извне для применения смены языка). */
    public void setLanguageThread() {
        runOnUiThread(this::recreate);
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
