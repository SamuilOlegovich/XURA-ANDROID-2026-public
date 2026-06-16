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
 * Smoke-тест для YourReferral: экран показа собственного реферального кода.
 * YourReferral.CODE выставляется явно — экран показывает именно его, без сети.
 */
@RunWith(AndroidJUnit4.class)
public class YourReferralTest {

    @Before
    public void setUp() {
        YourReferral.CODE = "TESTCODE123";
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<YourReferral> scenario = ActivityScenario.launch(YourReferral.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsReferralCode() {
        try (ActivityScenario<YourReferral> scenario = ActivityScenario.launch(YourReferral.class)) {
            onView(withId(R.id.your_referral_page_code)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsCopyButton() {
        try (ActivityScenario<YourReferral> scenario = ActivityScenario.launch(YourReferral.class)) {
            onView(withId(R.id.your_referral_page_copy_linc)).check(matches(isDisplayed()));
        }
    }
}
