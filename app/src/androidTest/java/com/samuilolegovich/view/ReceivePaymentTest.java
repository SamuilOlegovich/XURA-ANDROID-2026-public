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
 * Smoke-тест для ReceivePayment: экран приёма платежа (адрес кошелька + QR-код).
 * repository.getClassicAddress() безопасен и без активного кошелька (возвращает
 * placeholder "WALLET_NOT_ACTIVATED"), поэтому тест не требует прогрева WalletRepository.
 */
@RunWith(AndroidJUnit4.class)
public class ReceivePaymentTest {

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
        try (ActivityScenario<ReceivePayment> scenario = ActivityScenario.launch(ReceivePayment.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsAddressAndQrCode() {
        try (ActivityScenario<ReceivePayment> scenario = ActivityScenario.launch(ReceivePayment.class)) {
            onView(withId(R.id.address)).check(matches(isDisplayed()));
            onView(withId(R.id.qr_code)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void screen_showsShareButton() {
        try (ActivityScenario<ReceivePayment> scenario = ActivityScenario.launch(ReceivePayment.class)) {
            onView(withId(R.id.share_linc)).check(matches(isDisplayed()));
        }
    }
}
