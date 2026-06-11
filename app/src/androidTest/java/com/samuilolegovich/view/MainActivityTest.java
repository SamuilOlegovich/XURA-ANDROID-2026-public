package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

/**
 * Инструментальные UI-тесты для MainActivity.
 *
 * setUp() записывает в SharedPreferences минимально необходимые данные
 * (пароль = "не установлен" + зашифрованный тестовый seed), чтобы
 * handleStartup() не делал редирект и MainActivity оставалась на главном экране.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    // Тестовый seed — ненастоящий, нужен только чтобы пройти Cipher.decryptString без NPE
    private static final String TEST_SEED = "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU";

    private Context context;

    @SuppressLint("HardwareIds")
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MainActivity.START_FLAG = true;

        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // salt хранится пустым если не задана — берём пустую строку
        String encryptedSeed = Cipher.encryptString(TEST_SEED, "", androidId);

        context.getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE)
                .edit()
                .clear()
                // пароль = "password not installed" → handleStartup() считает пароль не установленным
                // → не редиректит на экран ввода пароля
                .putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                        StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue())
                // зашифрованный seed → handleStartup() не редиректит на экран создания кошелька
                .putString(StringEnum.APP_PREFERENCES_SEED.getValue(), encryptedSeed)
                .apply();
    }

    // ── Smoke ──────────────────────────────────────────────────────────────────

    /** Приложение запускается без краша */
    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    // ── Первый запуск (без кошелька) ──────────────────────────────────────────

    /** Без кошелька приложение открывает экран настройки */
    @Test
    public void firstLaunch_opensWalletSetupScreen() {
        try (ActivityScenario<CheckingNewWallet> scenario =
                     ActivityScenario.launch(CheckingNewWallet.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    // ── Главный экран (кошелёк настроен) ──────────────────────────────────────

    /** Кнопка баланса видна на главном экране */
    @Test
    public void mainScreen_showsBalanceButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.balance_linc)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка SEND видна на главном экране */
    @Test
    public void mainScreen_showsSendButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.next_link)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка REQUEST видна на главном экране */
    @Test
    public void mainScreen_showsRequestButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.request_link)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка TRANSACTION HISTORY видна на главном экране */
    @Test
    public void mainScreen_showsTransactionHistoryButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.transaction_history_link)).check(matches(isDisplayed()));
        }
    }

    /** Логотип видна на главном экране */
    @Test
    public void mainScreen_showsLogoButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.logo_button_link)).check(matches(isDisplayed()));
        }
    }

    // ── Пакет ─────────────────────────────────────────────────────────────────

    /** Package name приложения соответствует ожидаемому */
    @Test
    public void app_hasCorrectPackageName() {
        assertEquals("com.samuil.olegovich", context.getPackageName());
    }
}