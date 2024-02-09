package com.samuilolegovich.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.runnable.GenNumberRun;



// выводим правила игры
public class RulesOfTheGameGuessTheNumber extends AppCompatActivity {
    public static final String RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS = ".RulesOfTheGameGuessTheNumber";

    private String RULES;
//            "The rules of this game are very simple.\n\n" +
//            "The bet is always fixed and is - 1XRP.\n\n" +
//            "You just have to choose the number you want to bet on. (from - 1 to -  36)\n\n" +
//            "In case of winning, the bet will be increased by 36 times.\n\n" +
//            "If the bet is lost, it goes to the savings fund - LOTTO.\n\n" +
//            "Absolutely any user can win a LOTTO, regardless of what and how much he bet - if the LOTTO sector falls out. " +
//            "And you can also win a mini LOTTO - it is one tenth of the LOTTO.";
    private TextView guessTheColorTextView;
    private TextView rules;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.rules_of_the_game_guess_the_number_page);
        setButtons();
        setLanguage();
        setText();
    }



    private void setButtons() {
        guessTheColorTextView = (TextView) findViewById(R.id.guess_the_color_text_view);
        rules = (TextView) findViewById(R.id.guess_the_color_rules);
        RULES = getString(R.string.rules_guess_the_number_text);
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
