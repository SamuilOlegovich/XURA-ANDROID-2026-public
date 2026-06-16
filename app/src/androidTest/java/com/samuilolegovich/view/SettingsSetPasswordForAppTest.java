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
 * Smoke-тест для SettingsSetPasswordForApp: экран смены пароля приложения из
 * раздела настроек (в отличие от SetAnAppPassword — без кнопки SKIP).
 */
@RunWith(AndroidJUnit4.class)
public class SettingsSetPasswordForAppTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<SettingsSetPasswordForApp> scenario =
                     ActivityScenario.launch(SettingsSetPasswordForApp.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsPasswordFields() {
        try (ActivityScenario<SettingsSetPasswordForApp> scenario =
                     ActivityScenario.launch(SettingsSetPasswordForApp.class)) {
            onView(withId(R.id.password_field)).check(matches(isDisplayed()));
            onView(withId(R.id.edit_text_passport_tow)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsConfirmButton() {
        try (ActivityScenario<SettingsSetPasswordForApp> scenario =
                     ActivityScenario.launch(SettingsSetPasswordForApp.class)) {
            onView(withId(R.id.confirm_link)).check(matches(isDisplayed()));
        }
    }
}
