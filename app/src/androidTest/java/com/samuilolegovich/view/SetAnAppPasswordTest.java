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
 * Smoke-тест для SetAnAppPassword: экран установки пароля приложения при первом
 * запуске (с возможностью пропустить через SKIP).
 */
@RunWith(AndroidJUnit4.class)
public class SetAnAppPasswordTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<SetAnAppPassword> scenario = ActivityScenario.launch(SetAnAppPassword.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsPasswordFields() {
        try (ActivityScenario<SetAnAppPassword> scenario = ActivityScenario.launch(SetAnAppPassword.class)) {
            onView(withId(R.id.settings_set_password_app_field)).check(matches(isDisplayed()));
            onView(withId(R.id.settings_set_password_app_field_tow)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsConfirmAndSkipButtons() {
        try (ActivityScenario<SetAnAppPassword> scenario = ActivityScenario.launch(SetAnAppPassword.class)) {
            onView(withId(R.id.settings_set_password_app_confirm_link)).check(matches(isDisplayed()));
            onView(withId(R.id.settings_set_password_app_skip_linc)).check(matches(isDisplayed()));
        }
    }
}
