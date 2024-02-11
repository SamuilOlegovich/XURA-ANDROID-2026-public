package com.samuilolegovich.view;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

import static com.samuilolegovich.view.BecomeReferral.BECOME_REFERRAL_CLASS;
import static com.samuilolegovich.view.GuessTheNumberGame.GUESS_THE_NUMBER_GAME_CLASS;
import static com.samuilolegovich.view.GuessTheColorGame.GUESS_THE_COLOR_GAME_CLASS;
import static com.samuilolegovich.view.RouletteGame.ROULETTE_GAME_CLASS;
import static com.samuilolegovich.view.SelectGameMode.SELECT_GAME_MODE_CLASS;



public class SelectGame extends AppCompatActivity {
    public static final String SELECT_GAME_CLASS = ".SelectGame";

    private MediaPlayer flourOfChoiceMediaPlayer;
    private Animation animTranslate;

    private TextView selectTextView;
    private TextView becomeReferral;
    private TextView selectModeGame;
    private TextView guessTheNumber;
    private TextView guessTheColor;
    private TextView roulette;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.select_game_page);
        setButtons();
        setLanguage();
        setSound();
        listeners();
    }



    private void setSound() {
        flourOfChoiceMediaPlayer = MediaPlayer.create(this, R.raw.flour_of_choice);
        flourOfChoiceMediaPlayer.setVolume(0.5f, 0.5f);
        flourOfChoiceMediaPlayer.setLooping(true);
        flourOfChoiceMediaPlayer.start();
    }


    private void setButtons() {
        selectModeGame = (TextView) findViewById(R.id.select_game_select_mode_game_linc);
        becomeReferral = (TextView) findViewById(R.id.become_referral_linc);
        guessTheNumber = (TextView) findViewById(R.id.double_your_bet_linc);
        guessTheColor = (TextView) findViewById(R.id.guess_the_color_linc);
        selectTextView = (TextView) findViewById(R.id.select_text_view);
        roulette = (TextView) findViewById(R.id.roulette_linc);
    }


    private void setLanguage() {
        guessTheNumber.setText(R.string.name_guess_the_number);
        guessTheColor.setText(R.string.name_guess_the_color);
        becomeReferral.setText(R.string.become_a_referral);
        selectModeGame.setText(R.string.select_game_mode);
        selectTextView.setText(R.string.select_game);
        roulette.setText(R.string.name_roulette);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        becomeReferral.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        flourOfChoiceMediaPlayer.stop();
                        flourOfChoiceMediaPlayer.prepareAsync();
                        goToAnotherPage(BECOME_REFERRAL_CLASS);
                    }
                }
        );

        selectModeGame.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        flourOfChoiceMediaPlayer.stop();
                        flourOfChoiceMediaPlayer.prepareAsync();
                        goToAnotherPage(SELECT_GAME_MODE_CLASS);
                    }
                }
        );

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
