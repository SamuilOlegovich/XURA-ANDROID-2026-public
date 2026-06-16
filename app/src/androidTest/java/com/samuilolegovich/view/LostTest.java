package com.samuilolegovich.view;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.samuilolegovich.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для Lost: экран проигрыша. Lost.MASSAGE выставляется явно, так как
 * goText() отображает именно это статическое поле.
 */
@RunWith(AndroidJUnit4.class)
public class LostTest {

    @Before
    public void setUp() {
        Lost.MASSAGE = "Test lost message";
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<Lost> scenario = ActivityScenario.launch(Lost.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsPlayAgainAndBackButtons() {
        try (ActivityScenario<Lost> scenario = ActivityScenario.launch(Lost.class)) {
            onView(withId(R.id.btn_play_again)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_back_to_games)).check(matches(isDisplayed()));
        }
    }
}
