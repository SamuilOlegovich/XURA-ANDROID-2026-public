package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class RulesOfTheGameRoulette extends BaseActivity {
    public static final String RULES_OF_THE_GAME_ROULETTE_CLASS = ".RulesOfTheGameRoulette";

    private TextView rulesOfTheGameRouletteGuessTheColor;
    private TextView rulesOfTheGameRouletteGuessTheColorRules;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_of_the_game_roulette_page);
        setButtons();
        setLanguage();
    }



    private void setButtons() {
        rulesOfTheGameRouletteGuessTheColorRules = (TextView) findViewById(R.id.rules_of_the_game_roulette_guess_the_color_rules);
        rulesOfTheGameRouletteGuessTheColor = (TextView) findViewById(R.id.rules_of_the_game_roulette_guess_the_color);
    }


    private void setLanguage() {
        rulesOfTheGameRouletteGuessTheColorRules.setText(R.string.rules_roulette);
        rulesOfTheGameRouletteGuessTheColor.setText(R.string.roulette_game);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
