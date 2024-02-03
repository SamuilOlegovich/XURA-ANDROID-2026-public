package com.samuilolegovich.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;



public class RulesOfTheGameRoulette extends AppCompatActivity {
    public static final String RULES_OF_THE_GAME_ROULETTE_CLASS = ".RulesOfTheGameRoulette";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.rules_of_the_game_roulette);
    }

    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
