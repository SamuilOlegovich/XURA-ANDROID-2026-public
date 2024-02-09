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

import static com.samuilolegovich.view.RulesOfTheGameRoulette.RULES_OF_THE_GAME_ROULETTE_CLASS;



public class RouletteGame extends AppCompatActivity {
    public static final String ROULETTE_GAME_CLASS = ".RouletteGame";

    private MediaPlayer casinoMediaPlayer;

    private Animation animTranslate;

    private TextView rouletteGameTextView;
    private TextView rouletteGameMessage;
    private TextView rulesInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.roulette_game_page);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        rouletteGameTextView = (TextView) findViewById(R.id.roulette_game_text_view);
        rouletteGameMessage = (TextView) findViewById(R.id.roulette_game_message);
        rulesInfo = (TextView) findViewById(R.id.rules_of_the_game_link);

        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);

        casinoMediaPlayer.setLooping(true);
        casinoMediaPlayer.start();
    }


    private void setLanguage() {
        rouletteGameTextView.setText(R.string.roulette_game);
        rouletteGameMessage.setText(R.string.coming_soon);
        rulesInfo.setText(R.string.rules_of_the_game);
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
