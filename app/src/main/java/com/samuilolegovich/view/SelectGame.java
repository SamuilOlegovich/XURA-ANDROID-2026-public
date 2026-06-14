package com.samuilolegovich.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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

    public static volatile SelectGame SELECT_GAME_ACTIVITY;

    private static final long WAVE_INITIAL_DELAY  = 600L;   // ждём пока доиграет логотип
    private static final long WAVE_REPEAT_DELAY   = 3500L;  // пауза между волнами
    private static final int  WAVE_STAGGER_MS     = 100;    // задержка между карточками
    private static final int  WAVE_BOUNCE_DP      = 20;     // высота подпрыгивания

    private MediaPlayer flourOfChoiceMediaPlayer;

    private TextView selectTextView;
    private View guessTheNumber;
    private View guessTheColor;
    private View roulette;

    private Handler waveHandler;
    private Runnable waveRunnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_game_page);
        SELECT_GAME_ACTIVITY = this;
        setButtons();
        setLanguage();
        setSound();
        listeners();
        setupBottomNav();
        waveHandler = new Handler(Looper.getMainLooper());
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
        stopWave();
    }


    @Override
    protected void onResume() {
        flourOfChoiceMediaPlayer.setLooping(true);
        flourOfChoiceMediaPlayer.start();
        super.onResume();
        startWave();
    }


    // ─── Wave bounce animation ────────────────────────────────────────────────

    private void startWave() {
        stopWave();
        waveRunnable = new Runnable() {
            @Override
            public void run() {
                playWave();
                waveHandler.postDelayed(this, WAVE_REPEAT_DELAY);
            }
        };
        // Первая волна — после логотипной анимации (600ms), потом каждые 3.5s
        waveHandler.postDelayed(waveRunnable, WAVE_INITIAL_DELAY);
    }

    private void stopWave() {
        if (waveRunnable != null) {
            waveHandler.removeCallbacks(waveRunnable);
            waveRunnable = null;
        }
    }

    private void playWave() {
        // Порядок: снизу вверх → roulette первая, guessTheNumber вторая, guessTheColor третья
        View[] cards = { roulette, guessTheNumber, guessTheColor };
        float bounceY = WAVE_BOUNCE_DP * getResources().getDisplayMetrics().density;

        for (int i = 0; i < cards.length; i++) {
            animateBounce(cards[i], (long) i * WAVE_STAGGER_MS, bounceY);
        }

        // Когда волна дошла до верхней карточки — логотип получает толчок снизу
        long logoDelay = (long) (cards.length - 1) * WAVE_STAGGER_MS + 170L;
        animateLogoJolt(logoDelay);
    }

    private void animateBounce(View card, long startDelay, float bounceY) {
        // Вверх: быстро, с замедлением в пике
        ObjectAnimator up = ObjectAnimator.ofFloat(card, "translationY", 0f, -bounceY);
        up.setDuration(170);
        up.setInterpolator(new DecelerateInterpolator(1.5f));

        // Вниз: с пружинкой при приземлении
        ObjectAnimator down = ObjectAnimator.ofFloat(card, "translationY", -bounceY, 0f);
        down.setDuration(230);
        down.setInterpolator(new OvershootInterpolator(1.8f));

        AnimatorSet bounce = new AnimatorSet();
        bounce.playSequentially(up, down);
        bounce.setStartDelay(startDelay);
        bounce.start();
    }

    private void animateLogoJolt(long startDelay) {
        View logo = findViewById(R.id.logo_xura);
        if (logo == null) return;

        float joltY = 8 * getResources().getDisplayMetrics().density; // лёгкий толчок — 8dp

        // Вниз: волна "давит" на лого снизу — оно чуть просаживается
        ObjectAnimator down = ObjectAnimator.ofFloat(logo, "translationY", 0f, joltY);
        down.setDuration(80);
        down.setInterpolator(new DecelerateInterpolator());

        // Вверх: пружинит с небольшим overshoot
        ObjectAnimator up = ObjectAnimator.ofFloat(logo, "translationY", joltY, 0f);
        up.setDuration(200);
        up.setInterpolator(new OvershootInterpolator(2.5f));

        AnimatorSet jolt = new AnimatorSet();
        jolt.playSequentially(down, up);
        jolt.setStartDelay(startDelay);
        jolt.start();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        flourOfChoiceMediaPlayer.stop();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SELECT_GAME_ACTIVITY = null;
    }
}
