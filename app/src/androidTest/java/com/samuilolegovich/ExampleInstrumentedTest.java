package com.samuilolegovich;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.fastloan", appContext.getPackageName());
    }

    @Test
    public void paymentAndSocketManagerXRPLTest() {
        PaymentAndSocketManagerXRPL paymentAndSocketManagerXRPL = PaymentAndSocketManagerXRPL.getInstances();
        Map<String, String> map = paymentAndSocketManagerXRPL.restoreWallet(StringEnum.SEED_REAL.getValue(), true);
        if (map != null) {
            map.forEach((s, s2) -> System.out.println(s + "   **************   " + s2));
        }
        System.out.println("***********************************");
    }
}