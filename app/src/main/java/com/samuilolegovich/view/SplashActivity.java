package com.samuilolegovich.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

/**
 * Стартовый экран-заставка: показывает анимированный логотип приложения и через
 * фиксированную паузу переходит на {@link MainActivity}.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1800L;

    /** Инициализирует экран, запускает анимацию логотипа и планирует переход на главный экран после паузы. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        ImageView logo = findViewById(R.id.splash_logo);
        animateLogo(logo);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, SPLASH_DELAY_MS);
    }

    /** Анимирует появление логотипа: масштабирование с эффектом overshoot и одновременное проявление прозрачности. */
    private void animateLogo(ImageView logo) {
        logo.setScaleX(0f);
        logo.setScaleY(0f);
        logo.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f);
        ObjectAnimator alpha  = ObjectAnimator.ofFloat(logo, "alpha",  0f, 1f);

        OvershootInterpolator overshoot = new OvershootInterpolator(1.3f);
        scaleX.setInterpolator(overshoot);
        scaleY.setInterpolator(overshoot);
        scaleX.setDuration(850);
        scaleY.setDuration(850);
        alpha.setDuration(550);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.start();
    }
}
