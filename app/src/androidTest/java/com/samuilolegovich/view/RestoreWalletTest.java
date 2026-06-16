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
 * Smoke-тест для RestoreWallet: экран ввода seed-фразы для восстановления кошелька.
 * Восстановление запускается только по нажатию кнопки, поэтому простой запуск
 * экрана не задействует сеть.
 */
@RunWith(AndroidJUnit4.class)
public class RestoreWalletTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<RestoreWallet> scenario = ActivityScenario.launch(RestoreWallet.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsSeedField() {
        try (ActivityScenario<RestoreWallet> scenario = ActivityScenario.launch(RestoreWallet.class)) {
            onView(withId(R.id.restore_wallet_text_view)).check(matches(isDisplayed()));
            onView(withId(R.id.restore_wallet_seed_field)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsRestoreButton() {
        try (ActivityScenario<RestoreWallet> scenario = ActivityScenario.launch(RestoreWallet.class)) {
            onView(withId(R.id.restore_wallet_next_link)).check(matches(isDisplayed()));
        }
    }
}
