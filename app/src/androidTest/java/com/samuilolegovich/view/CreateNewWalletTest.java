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
 * Smoke-тест для CreateNewWallet: экран сразу при запуске асинхронно генерирует
 * новый XRPL-кошелёк (локально, без сети) через WalletRepository и показывает seed.
 * Генерация выполняется на фоновом потоке, поэтому проверяем только статичную часть
 * разметки, не дожидаясь конкретного содержимого seed-поля.
 */
@RunWith(AndroidJUnit4.class)
public class CreateNewWalletTest {

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<CreateNewWallet> scenario = ActivityScenario.launch(CreateNewWallet.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsTitleAndSeedField() {
        try (ActivityScenario<CreateNewWallet> scenario = ActivityScenario.launch(CreateNewWallet.class)) {
            onView(withId(R.id.create_new_wallet_text_view)).check(matches(isDisplayed()));
            onView(withId(R.id.seed_field)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsNextButton() {
        try (ActivityScenario<CreateNewWallet> scenario = ActivityScenario.launch(CreateNewWallet.class)) {
            onView(withId(R.id.next_link)).check(matches(isDisplayed()));
        }
    }
}
