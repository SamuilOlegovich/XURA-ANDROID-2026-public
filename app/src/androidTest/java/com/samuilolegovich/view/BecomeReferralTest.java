package com.samuilolegovich.view;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SecureSeedStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для BecomeReferral: экран приглашения стать рефералом. Реальные
 * вызовы WalletRepository (checkData/makePayment) происходят только по нажатию
 * кнопок, поэтому простой запуск экрана их не задействует.
 */
@RunWith(AndroidJUnit4.class)
public class BecomeReferralTest {

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
    }

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<BecomeReferral> scenario = ActivityScenario.launch(BecomeReferral.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsBecomeReferralButton() {
        try (ActivityScenario<BecomeReferral> scenario = ActivityScenario.launch(BecomeReferral.class)) {
            onView(withId(R.id.become_referral)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsRestoreReferralButton() {
        try (ActivityScenario<BecomeReferral> scenario = ActivityScenario.launch(BecomeReferral.class)) {
            onView(withId(R.id.restore_referral)).check(matches(isDisplayed()));
        }
    }
}
