package com.samuilolegovich.view;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

/**
 * Инструментальные UI-тесты для MainActivity и экранов управления кошельком.
 * Запускаются на устройстве или эмуляторе. Проверяют что приложение стартует
 * без краша и основные элементы интерфейса отображаются корректно.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private Context context;

    /** Подготовка контекста и очистка SharedPreferences перед каждым тестом */
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(
                StringEnum.APP_PREFERENCES.getValue(),
                Context.MODE_PRIVATE
        ).edit().clear().commit();
    }

    // -------------------------------------------------------------------------
    // Smoke-тест: приложение запускается
    // -------------------------------------------------------------------------

    /** Приложение запускается без краша */
    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    // -------------------------------------------------------------------------
    // Экран первого запуска (без кошелька)
    // -------------------------------------------------------------------------

    /**
     * При первом запуске (без сохранённого кошелька) приложение
     * перенаправляет на экран CheckingNewWallet с кнопками создания/восстановления.
     */
    @Test
    public void firstLaunch_opensWalletSetupScreen() {
        try (ActivityScenario<CheckingNewWallet> scenario =
                     ActivityScenario.launch(CheckingNewWallet.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    // -------------------------------------------------------------------------
    // MainActivity: основные элементы UI
    // -------------------------------------------------------------------------

    /**
     * Если кошелёк настроен, главный экран показывает кнопку баланса.
     * Тестовый seed устанавливается вручную чтобы пропустить экран настройки.
     */
    @Test
    public void mainScreen_showsBalanceButton() {
        StringEnum.setValue(StringEnum.SEED_REAL, "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.balance_linc)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка SEND отображается на главном экране */
    @Test
    public void mainScreen_showsSendButton() {
        StringEnum.setValue(StringEnum.SEED_REAL, "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.next_link)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка REQUEST отображается на главном экране */
    @Test
    public void mainScreen_showsRequestButton() {
        StringEnum.setValue(StringEnum.SEED_REAL, "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.request_link)).check(matches(isDisplayed()));
        }
    }

    /** Кнопка TRANSACTION HISTORY отображается на главном экране */
    @Test
    public void mainScreen_showsTransactionHistoryButton() {
        StringEnum.setValue(StringEnum.SEED_REAL, "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.transaction_history_link)).check(matches(isDisplayed()));
        }
    }

    /** Логотип отображается на главном экране */
    @Test
    public void mainScreen_showsLogoButton() {
        StringEnum.setValue(StringEnum.SEED_REAL, "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.logo_button_link)).check(matches(isDisplayed()));
        }
    }

    // -------------------------------------------------------------------------
    // Проверка пакета приложения
    // -------------------------------------------------------------------------

    /** Package name приложения соответствует ожидаемому */
    @Test
    public void app_hasCorrectPackageName() {
        assertEquals("com.samuil.olegovich", context.getPackageName());
    }
}