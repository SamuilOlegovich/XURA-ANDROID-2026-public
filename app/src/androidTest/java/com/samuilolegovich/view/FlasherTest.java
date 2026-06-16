package com.samuilolegovich.view;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для Flasher: экран ожидания результата розыгрыша.
 * IS_REAL_GAME_MODE=true предотвращает запуск goThreadTest() и связанного NotifierRunForTrialGame —
 * неотменяемого потока с 4-10с задержкой, который иначе загрязнял бы состояние последующих тестов.
 * FlasherRun (реальный режим) работает локально через Lotto.genNumberAndColor() и чисто
 * останавливается через onPause() при закрытии ActivityScenario.
 */
@RunWith(AndroidJUnit4.class)
public class FlasherTest {

    @Before
    public void setUp() {
        MainActivity.IS_REAL_GAME_MODE = true;
    }

    @After
    public void resetGameMode() {
        MainActivity.IS_REAL_GAME_MODE = false;
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<Flasher> scenario = ActivityScenario.launch(Flasher.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsContinueBettingWhileWaiting() {
        try (ActivityScenario<Flasher> scenario = ActivityScenario.launch(Flasher.class)) {
            onView(withId(R.id.ll_continue_bet)).check(matches(isDisplayed()));
        }
    }
}
