package com.samuilolegovich.view;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для EnterApplicationPassword: экран ввода пароля приложения. Сам себя
 * исключает из автоблокировки (isLockExempt), биометрию не показывает без включённого
 * флага/доступного оборудования, поэтому всегда отображает поле ввода пароля.
 */
@RunWith(AndroidJUnit4.class)
public class EnterApplicationPasswordTest {

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = PrefsHelper.get(context);
        prefs.edit()
                .putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "some_hash")
                .putString(StringEnum.APP_PREFERENCES_BIOMETRIC_ENABLED.getValue(), "false")
                .commit();
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<EnterApplicationPassword> scenario =
                     ActivityScenario.launch(EnterApplicationPassword.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsPasswordField() {
        try (ActivityScenario<EnterApplicationPassword> scenario =
                     ActivityScenario.launch(EnterApplicationPassword.class)) {
            onView(withId(R.id.enter_application_password_field)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsUnlockButton() {
        try (ActivityScenario<EnterApplicationPassword> scenario =
                     ActivityScenario.launch(EnterApplicationPassword.class)) {
            onView(withId(R.id.enter_application_password_next_link)).check(matches(isDisplayed()));
        }
    }
}
