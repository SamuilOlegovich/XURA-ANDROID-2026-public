package com.samuilolegovich.view;

import android.view.View;
import android.widget.ImageView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.samuilolegovich.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для SplashActivity: проверяет, что заставка запускается без краша
 * и показывает логотип до автоматического перехода на MainActivity (через 1800ms).
 * Видимость логотипа проверяется напрямую внутри onActivity() (синхронно сразу после
 * launch()), а не через Espresso onView() — последний добавляет заметную задержку
 * синхронизации, из-за которой проверка может проиграть гонку с таймером перехода.
 */
@RunWith(AndroidJUnit4.class)
public class SplashActivityTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void splashScreen_showsLogo() {
        try (ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class)) {
            scenario.onActivity(activity -> {
                ImageView logo = activity.findViewById(R.id.splash_logo);
                assertNotNull(logo);
                assertEquals(View.VISIBLE, logo.getVisibility());
            });
        }
    }
}
