package com.samuilolegovich.view;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private Animation animTranslate;

    private TextView selectTextView;
    private TextView guessTheNumber;
    private TextView guessTheColor;
    private TextView roulette;



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
        guessTheNumber = (TextView) findViewById(R.id.double_your_bet_linc);
        guessTheColor = (TextView) findViewById(R.id.guess_the_color_linc);
        selectTextView = (TextView) findViewById(R.id.select_text_view);
        roulette = (TextView) findViewById(R.id.roulette_linc);
    }


    private void setLanguage() {
        guessTheNumber.setText(R.string.name_guess_the_number);
        guessTheColor.setText(R.string.name_guess_the_color);
        selectTextView.setText(R.string.select_game);
        roulette.setText(R.string.name_roulette);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        guessTheColor.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        flourOfChoiceMediaPlayer.stop();
                        flourOfChoiceMediaPlayer.prepareAsync();
                        goToAnotherPage(GUESS_THE_COLOR_GAME_CLASS);
                    }
                }
        );

        guessTheNumber.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        flourOfChoiceMediaPlayer.stop();
                        flourOfChoiceMediaPlayer.prepareAsync();
                        goToAnotherPage(GUESS_THE_NUMBER_GAME_CLASS);
                    }
                }
        );

        roulette.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        flourOfChoiceMediaPlayer.stop();
                        flourOfChoiceMediaPlayer.prepareAsync();
                        goToAnotherPage(ROULETTE_GAME_CLASS);
                    }
                }
        );
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
