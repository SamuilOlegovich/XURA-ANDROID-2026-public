package com.samuilolegovich;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SecureSeedStorage;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Одноразовый вспомогательный тест: записывает нужный seed в зашифрованное
 * хранилище через SecureSeedStorage, чтобы при следующем запуске приложения
 * оно загрузило именно этот кошелёк. Запускается вручную через adb.
 */
@RunWith(AndroidJUnit4.class)
public class SeedSetupTest {

    private static final String TARGET_SEED = "sEdV5gdDTythgn5V5sDfHKdhL4DwuNf";

    @Test
    public void setupSeedForManualTesting() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = PrefsHelper.get(context);

        prefs.edit()
                .putString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(),
                        StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue())
                .commit();

        SecureSeedStorage.delete(prefs, StringEnum.APP_PREFERENCES_SEED.getValue());
        SecureSeedStorage.save(prefs, StringEnum.APP_PREFERENCES_SEED.getValue(), TARGET_SEED);

        String loaded = SecureSeedStorage.load(prefs, StringEnum.APP_PREFERENCES_SEED.getValue());
        assertNotNull("Seed должен сохраниться", loaded);
        assertEquals("Seed должен совпадать с заданным", TARGET_SEED, loaded);
    }
}
