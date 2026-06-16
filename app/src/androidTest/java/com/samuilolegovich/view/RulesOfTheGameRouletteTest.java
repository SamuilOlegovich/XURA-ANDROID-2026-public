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
 * Smoke-тест для RulesOfTheGameRoulette: статичный экран с HTML-текстом правил рулетки.
 */
@RunWith(AndroidJUnit4.class)
public class RulesOfTheGameRouletteTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<RulesOfTheGameRoulette> scenario =
                     ActivityScenario.launch(RulesOfTheGameRoulette.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsRulesText() {
        try (ActivityScenario<RulesOfTheGameRoulette> scenario =
                     ActivityScenario.launch(RulesOfTheGameRoulette.class)) {
            onView(withId(R.id.rules_of_the_game_roulette_guess_the_color_rules)).check(matches(isDisplayed()));
        }
    }
}
