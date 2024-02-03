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
import com.samuilolegovich.asyncAndRun.runnable.GenColorRun;
import com.samuilolegovich.asyncAndRun.runnable.GenNumberRun;

import static com.samuilolegovich.view.BecomeReferral.BECOME_REFERRAL_CLASS;
import static com.samuilolegovich.view.GuessTheNumberGame.GUESS_THE_NUMBER_GAME_CLASS;
import static com.samuilolegovich.view.GuessTheColorGame.GUESS_THE_COLOR_GAME_CLASS;
import static com.samuilolegovich.view.RouletteGame.ROULETTE_GAME_CLASS;



public class SelectGame extends AppCompatActivity {
    public static final String SELECT_GAME_CLASS = ".SelectGame";

    private MediaPlayer flourOfChoiceMediaPlayer;
    private Animation animTranslate;

    private TextView becomeReferral;
    private TextView guessTheColor;
    private TextView doubleYourBet;
    private TextView roulette;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.select_game);
        setButtons();
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
        becomeReferral = (TextView) findViewById(R.id.become_referral_linc);
        guessTheColor = (TextView) findViewById(R.id.guess_the_color);
        doubleYourBet = (TextView) findViewById(R.id.double_your_bet);
        roulette = (TextView) findViewById(R.id.roulette);
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

        doubleYourBet.setOnClickListener(
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
