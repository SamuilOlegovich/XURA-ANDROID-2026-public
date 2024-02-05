package com.samuilolegovich.view;

import android.os.Bundle;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



// выводим правила игры
public class RulesOfTheGameGuessTheColor extends AppCompatActivity {
    public static final String RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS = ".RulesOfTheGameGuessTheColor";
    private static final String RULES = "The rules of this game are very simple.\n\n" +
            "You place a bet and indicate which color.\n\n" +
            "If the color is guessed - your bet will be doubled.\n\n" +
            "If the bet is lost, it goes to the savings fund - LOTTO.\n\n" +
            "Absolutely any user can win a LOTTO, regardless of what and how much he bet - if the LOTTO sector falls out." +
            "And you can also win a mini LOTTO - it is one tenth of the LOTTO.\n\n" +
            "The minimum bet is 0.1 XRP, the maximum is 100 XRP.";

    private TextView rules;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.rules_of_the_game_guess_the_color);
        setButtons();
        setLanguage();
        setText();
    }

    private void setButtons() {
        rules = (TextView) findViewById(R.id.rules);
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
