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
 * Smoke-тест для SendPayment: экран отправки платежа. В onCreate вызывает
 * viewModel.loadBalance() — в тестовом режиме игры (IS_REAL_GAME_MODE = false)
 * это читает локальный виртуальный баланс без обращения к сети.
 */
@RunWith(AndroidJUnit4.class)
public class SendPaymentTest {

    private static final String TEST_SEED = "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU";

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = PrefsHelper.get(context);
        prefs.edit()
                .putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                        StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue())
                .commit();
        SecureSeedStorage.save(prefs, StringEnum.APP_PREFERENCES_SEED.getValue(), TEST_SEED);
        MainActivity.IS_REAL_GAME_MODE = false;
    }

    @After
    public void resetGameMode() {
        MainActivity.IS_REAL_GAME_MODE = false;
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<SendPayment> scenario = ActivityScenario.launch(SendPayment.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsAddressAndAmountFields() {
        try (ActivityScenario<SendPayment> scenario = ActivityScenario.launch(SendPayment.class)) {
            onView(withId(R.id.scan_linc)).check(matches(isDisplayed()));
            onView(withId(R.id.amount_field)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsSendButton() {
        try (ActivityScenario<SendPayment> scenario = ActivityScenario.launch(SendPayment.class)) {
            onView(withId(R.id.send_linc)).check(matches(isDisplayed()));
        }
    }
}
