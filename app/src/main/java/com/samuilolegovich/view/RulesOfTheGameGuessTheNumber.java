package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.runnable.GenNumberRun;



// выводим правила игры
public class RulesOfTheGameGuessTheNumber extends BaseActivity {
    public static final String RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS = ".RulesOfTheGameGuessTheNumber";

    private TextView guessTheColorTextView;
    private TextView rules;

    private String RULES;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_of_the_game_guess_the_number_page);
        setButtons();
        setLanguage();
        setText();
    }



    private void setButtons() {
        guessTheColorTextView = (TextView) findViewById(R.id.guess_the_color_text_view);
        rules = (TextView) findViewById(R.id.guess_the_color_rules);
    }


    private void setLanguage() {
        guessTheColorTextView.setText(R.string.guess_the_number);
        RULES = getString(R.string.rules_guess_the_number_text);
    }


    private void setText() {
        rules.setText(RULES);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        GenNumberRun.FLAG =  false;
        super.onBackPressed();
    }
}
