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
 * Smoke-тест для InfoReferral: статичный информационный экран о реферальной программе.
 */
@RunWith(AndroidJUnit4.class)
public class InfoReferralTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<InfoReferral> scenario = ActivityScenario.launch(InfoReferral.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsTitle() {
        try (ActivityScenario<InfoReferral> scenario = ActivityScenario.launch(InfoReferral.class)) {
            onView(withId(R.id.become_referral_main_text_view)).check(matches(isDisplayed()));
        }
    }
}
