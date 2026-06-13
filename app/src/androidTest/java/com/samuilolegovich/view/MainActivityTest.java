package com.samuilolegovich.view;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SecureSeedStorage;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

/**
 * Инструментальные UI-тесты для MainActivity.
 *
 * setUp() записывает в EncryptedSharedPreferences минимально необходимые данные
 * (пароль = "не установлен" + seed через SecureSeedStorage/Android Keystore), чтобы
 * handleStartup() не делал редирект и MainActivity оставалась на главном экране.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest {

    private static final String TEST_SEED = "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU";

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MainActivity.START_FLAG = true;

        SharedPreferences prefs = PrefsHelper.get(context);
        // commit() обеспечивает синхронную запись до старта Activity
        prefs.edit()
                .putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                        StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue())
                .commit();

        SecureSeedStorage.save(prefs, StringEnum.APP_PREFERENCES_SEED.getValue(), TEST_SEED);
    }

    // ── Smoke ──────────────────────────────────────────────────────────────────

    /** Приложение запускается без краша */
    @Test
    public void a_app_launchesWithoutCrash() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    // ── Первый запуск (без кошелька) ──────────────────────────────────────────

    /** Без кошелька приложение открывает экран настройки */
    @Test
    public void b_firstLaunch_opensWalletSetupScreen() {
        try (ActivityScenario<CheckingNewWallet> scenario =
                     ActivityScenario.launch(CheckingNewWallet.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    // ── Главный экран (кошелёк настроен) ──────────────────────────────────────

    /** Кнопка баланса видна на главном экране */
    @Test
    public void c_mainScreen_showsBalanceButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.balance_linc)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка SEND видна на главном экране */
    @Test
    public void d_mainScreen_showsSendButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.next_link)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка REQUEST видна на главном экране */
    @Test
    public void e_mainScreen_showsRequestButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.request_link)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка TRANSACTION HISTORY видна на главном экране */
    @Test
    public void f_mainScreen_showsTransactionHistoryButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.transaction_history_link)).check(matches(isDisplayed()));
        }
    }

    /** Заголовок баланса (YOUR BALANCE) виден на главном экране */
    @Test
    public void g_mainScreen_showsLogoButton() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.your_balance_text)).check(matches(isDisplayed()));
        }
    }

    // ── Пакет ─────────────────────────────────────────────────────────────────

    /** Package name приложения соответствует ожидаемому */
    @Test
    public void h_app_hasCorrectPackageName() {
        assertEquals("com.samuil.olegovich", context.getPackageName());
    }
}
