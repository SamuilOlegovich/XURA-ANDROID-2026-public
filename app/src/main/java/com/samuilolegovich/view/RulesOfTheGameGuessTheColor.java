package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import dagger.hilt.android.AndroidEntryPoint;




// выводим правила игры
@AndroidEntryPoint
public class RulesOfTheGameGuessTheColor extends BaseActivity {
    public static final String RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS = ".RulesOfTheGameGuessTheColor";

    private TextView rulesOfTheGameGuessTheColorView;
    private TextView rules;

    private String RULES;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_of_the_game_guess_the_color_page);
        setButtons();
        setLanguage();
        setText();
    }



    private void setButtons() {
        rulesOfTheGameGuessTheColorView = (TextView) findViewById(R.id.rules_of_the_game_guess_the_color_view);
        rules = (TextView) findViewById(R.id.guess_the_color_rules);
    }


    private void setLanguage() {
        rulesOfTheGameGuessTheColorView.setText(R.string.guess_the_color);
        RULES = getString(R.string.rules_guess_the_color_text);
    }


    private void setText() {
        rules.setText(RULES);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
