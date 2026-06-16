package com.samuilolegovich.view;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для Settings: экран настроек приложения (пароль, биометрия, режим игры,
 * язык). MainActivity.IS_REAL_GAME_MODE = false выставляется явно, чтобы карточка
 * тестового баланса была видна и поведение было детерминированным между тестами.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsTest {

    @Before
    public void setUp() {
        MainActivity.IS_REAL_GAME_MODE = false;
    }

    @After
    public void resetGameMode() {
        MainActivity.IS_REAL_GAME_MODE = false;
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsTitle() {
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            onView(withId(R.id.settings_text_view)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsSetPasswordCard() {
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            onView(withId(R.id.settings_set_password_linc)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsGameModeCard() {
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            onView(withId(R.id.settings_game_mode_linc)).check(matches(isDisplayed()));
        }
    }
}
