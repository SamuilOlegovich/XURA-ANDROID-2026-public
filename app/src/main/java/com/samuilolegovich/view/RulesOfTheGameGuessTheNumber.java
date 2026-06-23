package com.samuilolegovich.view;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import dagger.hilt.android.AndroidEntryPoint;




/** Экран с текстовыми правилами игры "Угадай число" — статичный текст, локализованный и отформатированный из HTML. */
@AndroidEntryPoint
public class RulesOfTheGameGuessTheNumber extends BaseActivity {
    public static final String RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS = ".RulesOfTheGameGuessTheNumber";

    private TextView guessTheColorTextView;
    private TextView rules;

    private String RULES;



    /** Инициализирует экран: разметка, View, локализация и вывод HTML-текста правил. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_of_the_game_guess_the_number_page);
        setButtons();
        setLanguage();
        setText();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        guessTheColorTextView = (TextView) findViewById(R.id.guess_the_color_text_view);
        rules = (TextView) findViewById(R.id.guess_the_color_rules);
    }


    /** Загружает локализованные строки заголовка и текста правил игры. */
    private void setLanguage() {
        guessTheColorTextView.setText(R.string.guess_the_number);
        RULES = getString(R.string.rules_guess_the_number_text);
    }


    /** Отображает HTML-форматированный текст правил в TextView. */
    private void setText() {
        rules.setText(Html.fromHtml(RULES, Html.FROM_HTML_MODE_COMPACT));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
