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
 * Smoke-тест для RulesOfTheGameGuessTheColor: статичный экран с HTML-текстом правил.
 */
@RunWith(AndroidJUnit4.class)
public class RulesOfTheGameGuessTheColorTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<RulesOfTheGameGuessTheColor> scenario =
                     ActivityScenario.launch(RulesOfTheGameGuessTheColor.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsRulesText() {
        try (ActivityScenario<RulesOfTheGameGuessTheColor> scenario =
                     ActivityScenario.launch(RulesOfTheGameGuessTheColor.class)) {
            onView(withId(R.id.guess_the_color_rules)).check(matches(isDisplayed()));
        }
    }
}
