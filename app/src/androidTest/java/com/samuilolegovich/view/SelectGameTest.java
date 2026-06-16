package com.samuilolegovich.view;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.samuilolegovich.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для SelectGame: экран выбора игры (рулетка/число/цвет), без сети и prefs-зависимостей.
 */
@RunWith(AndroidJUnit4.class)
public class SelectGameTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<SelectGame> scenario = ActivityScenario.launch(SelectGame.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsAllThreeGameOptions() {
        try (ActivityScenario<SelectGame> scenario = ActivityScenario.launch(SelectGame.class)) {
            onView(withId(R.id.guess_the_color_linc)).check(matches(isDisplayed()));
            onView(withId(R.id.double_your_bet_linc)).check(matches(isDisplayed()));
            onView(withId(R.id.roulette_linc)).check(matches(isDisplayed()));
        }
    }
}
