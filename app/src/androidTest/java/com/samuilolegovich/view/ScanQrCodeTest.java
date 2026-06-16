package com.samuilolegovich.view;

import android.Manifest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.samuilolegovich.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Smoke-тест для ScanQrCode: экран сканирования QR-кода через CameraX. Разрешение на камеру
 * выдаётся заранее через GrantPermissionRule, чтобы избежать системного диалога запроса
 * разрешения, который блокирует Espresso.
 */
@RunWith(AndroidJUnit4.class)
public class ScanQrCodeTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Test
    public void app_launchesWithoutCrash() {
        try (ActivityScenario<ScanQrCode> scenario = ActivityScenario.launch(ScanQrCode.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void screen_showsCameraPreview() {
        try (ActivityScenario<ScanQrCode> scenario = ActivityScenario.launch(ScanQrCode.class)) {
            onView(withId(R.id.camera)).check(matches(isDisplayed()));
        }
    }
}
