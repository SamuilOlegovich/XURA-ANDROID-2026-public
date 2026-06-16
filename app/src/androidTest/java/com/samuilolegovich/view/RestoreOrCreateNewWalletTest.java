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
 * Smoke-тест для RestoreOrCreateNewWallet: экран выбора между восстановлением
 * существующего кошелька и созданием нового. Не требует предварительной настройки
 * preferences — это первый экран онбординга.
 */
@RunWith(AndroidJUnit4.class)
public class RestoreOrCreateNewWalletTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<RestoreOrCreateNewWallet> scenario =
                     ActivityScenario.launch(RestoreOrCreateNewWallet.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsCreateNewWalletButton() {
        try (ActivityScenario<RestoreOrCreateNewWallet> scenario =
                     ActivityScenario.launch(RestoreOrCreateNewWallet.class)) {
            onView(withId(R.id.create_new_wallet_linc)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsRestoreWalletButton() {
        try (ActivityScenario<RestoreOrCreateNewWallet> scenario =
                     ActivityScenario.launch(RestoreOrCreateNewWallet.class)) {
            onView(withId(R.id.restore_wallet_linc)).check(matches(isDisplayed()));
        }
    }
}
