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
 * Smoke-тест для Win: экран победы. Win.MASSAGE выставляется явно, так как
 * goText() отображает именно это статическое поле.
 */
@RunWith(AndroidJUnit4.class)
public class WinTest {

    @Before
    public void setUp() {
        Win.MASSAGE = "Test win message";
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<Win> scenario = ActivityScenario.launch(Win.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsPlayAgainAndBackButtons() {
        try (ActivityScenario<Win> scenario = ActivityScenario.launch(Win.class)) {
            onView(withId(R.id.btn_play_again)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_back_to_games)).check(matches(isDisplayed()));
        }
    }
}
