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
 * Smoke-тест для RulesOfTheGameGuessTheNumber: статичный экран с HTML-текстом правил.
 */
@RunWith(AndroidJUnit4.class)
public class RulesOfTheGameGuessTheNumberTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<RulesOfTheGameGuessTheNumber> scenario =
                     ActivityScenario.launch(RulesOfTheGameGuessTheNumber.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsRulesText() {
        try (ActivityScenario<RulesOfTheGameGuessTheNumber> scenario =
                     ActivityScenario.launch(RulesOfTheGameGuessTheNumber.class)) {
            onView(withId(R.id.guess_the_color_rules)).check(matches(isDisplayed()));
        }
    }
}
