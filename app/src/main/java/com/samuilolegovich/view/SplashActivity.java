package com.samuilolegovich.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1800L;

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

    private void animateLogo(ImageView logo) {
        Drawable original = logo.getDrawable().mutate();
        // Прорисовка слева направо: level 0 = скрыто, 10000 = полностью видно
        ClipDrawable clip = new ClipDrawable(original, Gravity.START, ClipDrawable.HORIZONTAL);
        logo.setImageDrawable(clip);
        logo.setAlpha(0f);

        // Фаза 1: логотип "рисуется" слева направо
        ValueAnimator drawAnim = ValueAnimator.ofInt(0, 10000);
        drawAnim.setDuration(1100);
        drawAnim.setInterpolator(new DecelerateInterpolator(1.2f));
        drawAnim.addUpdateListener(anim -> clip.setLevel((int) anim.getAnimatedValue()));

        // Фаза 2: плавное появление (убирает жёсткую левую границу)
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        fadeAnim.setDuration(700);
        fadeAnim.setStartDelay(150);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(drawAnim, fadeAnim);
        set.start();
    }
}
