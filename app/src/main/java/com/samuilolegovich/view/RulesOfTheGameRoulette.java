package com.samuilolegovich.view;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import dagger.hilt.android.AndroidEntryPoint;




/** Экран с текстовыми правилами игры в рулетку — статичный текст, локализованный и отформатированный из HTML. */
@AndroidEntryPoint
public class RulesOfTheGameRoulette extends BaseActivity {
    public static final String RULES_OF_THE_GAME_ROULETTE_CLASS = ".RulesOfTheGameRoulette";

    private TextView rulesOfTheGameRouletteGuessTheColor;
    private TextView rulesOfTheGameRouletteGuessTheColorRules;



    /** Инициализирует экран: разметка, View и локализация текста правил. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_of_the_game_roulette_page);
        setButtons();
        setLanguage();
    }



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        rulesOfTheGameRouletteGuessTheColorRules = (TextView) findViewById(R.id.rules_of_the_game_roulette_guess_the_color_rules);
        rulesOfTheGameRouletteGuessTheColor = (TextView) findViewById(R.id.rules_of_the_game_roulette_guess_the_color);
    }


    /** Загружает локализованный заголовок и HTML-форматированный текст правил рулетки. */
    private void setLanguage() {
        rulesOfTheGameRouletteGuessTheColorRules.setText(Html.fromHtml(getString(R.string.rules_roulette), Html.FROM_HTML_MODE_COMPACT));
        rulesOfTheGameRouletteGuessTheColor.setText(R.string.roulette_game);
    }


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
