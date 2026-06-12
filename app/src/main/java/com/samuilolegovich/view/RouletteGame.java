package com.samuilolegovich.view;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

import static com.samuilolegovich.view.RulesOfTheGameRoulette.RULES_OF_THE_GAME_ROULETTE_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class RouletteGame extends BaseActivity {
    public static final String ROULETTE_GAME_CLASS = ".RouletteGame";

    private MediaPlayer casinoMediaPlayer;

    private Animation animTranslate;

    private TextView rouletteGameTextView;
    private TextView rulesInfo;
    private MaterialButton btnNotifyMe;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roulette_game_page);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        rouletteGameTextView = (TextView) findViewById(R.id.roulette_game_text_view);
        rulesInfo = (TextView) findViewById(R.id.rules_of_the_game_link);
        btnNotifyMe = findViewById(R.id.btn_notify_me);

        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        casinoMediaPlayer.setLooping(true);
        casinoMediaPlayer.start();
    }


    private void setLanguage() {
        rouletteGameTextView.setText(R.string.roulette_game);
        rulesInfo.setText(R.string.rules_of_the_game);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        rulesInfo.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(RULES_OF_THE_GAME_ROULETTE_CLASS);
        });

        btnNotifyMe.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            Toast.makeText(this, R.string.roulette_notify_toast, Toast.LENGTH_LONG).show();
        });
    }


    private void goToAnotherPage(String namePage) {
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
