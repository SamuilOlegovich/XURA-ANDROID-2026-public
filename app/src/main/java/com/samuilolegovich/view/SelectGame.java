package com.samuilolegovich.view;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

import static com.samuilolegovich.view.GuessTheNumberGame.GUESS_THE_NUMBER_GAME_CLASS;
import static com.samuilolegovich.view.GuessTheColorGame.GUESS_THE_COLOR_GAME_CLASS;
import static com.samuilolegovich.view.RouletteGame.ROULETTE_GAME_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class SelectGame extends BaseActivity {
    public static final String SELECT_GAME_CLASS = ".SelectGame";

    private MediaPlayer flourOfChoiceMediaPlayer;

    private TextView selectTextView;
    private View guessTheNumber;
    private View guessTheColor;
    private View roulette;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_game_page);
        setButtons();
        setLanguage();
        setSound();
        listeners();
        setupBottomNav();
    }



    private void setSound() {
        flourOfChoiceMediaPlayer = MediaPlayer.create(this, R.raw.flour_of_choice);
        flourOfChoiceMediaPlayer.setVolume(0.5f, 0.5f);
        flourOfChoiceMediaPlayer.setLooping(true);
        flourOfChoiceMediaPlayer.start();
    }


    private void setButtons() {
        guessTheNumber = findViewById(R.id.double_your_bet_linc);
        guessTheColor = findViewById(R.id.guess_the_color_linc);
        selectTextView = (TextView) findViewById(R.id.select_text_view);
        roulette = findViewById(R.id.roulette_linc);
    }


    private void setLanguage() {
        selectTextView.setText(R.string.select_game);
    }


    private void listeners() {
        guessTheColor.setOnClickListener(v -> {
            pulse(v);
            flourOfChoiceMediaPlayer.stop();
            flourOfChoiceMediaPlayer.prepareAsync();
            goToAnotherPage(GUESS_THE_COLOR_GAME_CLASS);
        });

        guessTheNumber.setOnClickListener(v -> {
            pulse(v);
            flourOfChoiceMediaPlayer.stop();
            flourOfChoiceMediaPlayer.prepareAsync();
            goToAnotherPage(GUESS_THE_NUMBER_GAME_CLASS);
        });

        roulette.setOnClickListener(v -> {
            pulse(v);
            flourOfChoiceMediaPlayer.stop();
            flourOfChoiceMediaPlayer.prepareAsync();
            goToAnotherPage(ROULETTE_GAME_CLASS);
        });
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        flourOfChoiceMediaPlayer.setLooping(true);
        flourOfChoiceMediaPlayer.start();
        super.onResume();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        flourOfChoiceMediaPlayer.stop();
        super.onBackPressed();
    }
}
