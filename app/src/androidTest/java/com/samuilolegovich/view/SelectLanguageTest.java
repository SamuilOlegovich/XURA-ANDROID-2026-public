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
 * Smoke-тест для SelectLanguage: экран выбора языка интерфейса (карточки языков).
 */
@RunWith(AndroidJUnit4.class)
public class SelectLanguageTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<SelectLanguage> scenario = ActivityScenario.launch(SelectLanguage.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsTitle() {
        try (ActivityScenario<SelectLanguage> scenario = ActivityScenario.launch(SelectLanguage.class)) {
            onView(withId(R.id.settings_text_view)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsEnglishOption() {
        try (ActivityScenario<SelectLanguage> scenario = ActivityScenario.launch(SelectLanguage.class)) {
            onView(withId(R.id.settings_english_linc)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsRussianOption() {
        try (ActivityScenario<SelectLanguage> scenario = ActivityScenario.launch(SelectLanguage.class)) {
            onView(withId(R.id.settings_russian_linc)).check(matches(isDisplayed()));
        }
    }
}
