package com.samuilolegovich.view;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.R;

import static com.samuilolegovich.view.RulesOfTheGameRoulette.RULES_OF_THE_GAME_ROULETTE_CLASS;

public class RouletteGame extends AppCompatActivity {
    public static final String ROULETTE_GAME_CLASS = ".RouletteGame";

    private MediaPlayer casinoMediaPlayer;
    private Animation animTranslate;
    private TextView rulesInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roulette_game);
        setButtons();
        listeners();
    }

    private void setButtons() {
        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        rulesInfo = (TextView) findViewById(R.id.rules_of_the_game);

        casinoMediaPlayer.setLooping(true);
        casinoMediaPlayer.start();
    }

    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        rulesInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(RULES_OF_THE_GAME_ROULETTE_CLASS);
                    }
                }
        );
    }

    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }

    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        super.onBackPressed();
    }
}
