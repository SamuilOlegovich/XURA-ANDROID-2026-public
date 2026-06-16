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
 * Smoke-тест для Referral: экран ввода реферального кода. Referral.FLAG
 * выставляется явно (true = в потоке онбординга), так как используется для навигации.
 */
@RunWith(AndroidJUnit4.class)
public class ReferralTest {

    @Before
    public void setUp() {
        Referral.FLAG = true;
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<Referral> scenario = ActivityScenario.launch(Referral.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsCodeField() {
        try (ActivityScenario<Referral> scenario = ActivityScenario.launch(Referral.class)) {
            onView(withId(R.id.til_referral_code_field)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsSetAndSkipButtons() {
        try (ActivityScenario<Referral> scenario = ActivityScenario.launch(Referral.class)) {
            onView(withId(R.id.referral_set_linc)).check(matches(isDisplayed()));
            onView(withId(R.id.referral_skip_linc)).check(matches(isDisplayed()));
        }
    }
}
