package com.samuilolegovich.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Тесты для InactivityGuard — автоблокировка приложения по неактивности.
 * Таймаут (5 минут) реально ждать в тестах непрактично, поэтому для проверки
 * "просроченного" состояния напрямую выставляем приватное статическое поле
 * backgroundSince через reflection — это единственный доступный способ
 * детерминированно смоделировать прошедшее время без подмены System.currentTimeMillis().
 */
public class InactivityGuardTest {

    @Before
    public void resetBeforeTest() {
        InactivityGuard.reset();
    }

    @After
    public void resetAfterTest() {
        InactivityGuard.reset();
    }

    @Test
    public void freshState_doesNotRequireLock() {
        assertFalse(InactivityGuard.isLockRequired());
    }

    @Test
    public void onBackground_immediatelyAfter_doesNotRequireLock() {
        InactivityGuard.onBackground();
        assertFalse(InactivityGuard.isLockRequired());
    }

    @Test
    public void onForeground_clearsBackgroundTimer() throws Exception {
        InactivityGuard.onBackground();
        setBackgroundSince(System.currentTimeMillis() - 6 * 60 * 1000L);
        InactivityGuard.onForeground();
        assertFalse(InactivityGuard.isLockRequired());
    }

    @Test
    public void reset_clearsBackgroundTimer() throws Exception {
        InactivityGuard.onBackground();
        setBackgroundSince(System.currentTimeMillis() - 6 * 60 * 1000L);
        InactivityGuard.reset();
        assertFalse(InactivityGuard.isLockRequired());
    }

    @Test
    public void backgroundedPastTimeout_requiresLock() throws Exception {
        InactivityGuard.onBackground();
        setBackgroundSince(System.currentTimeMillis() - 6 * 60 * 1000L); // > 5 минут таймаута
        assertTrue(InactivityGuard.isLockRequired());
    }

    @Test
    public void backgroundedJustBeforeTimeout_doesNotRequireLock() throws Exception {
        InactivityGuard.onBackground();
        setBackgroundSince(System.currentTimeMillis() - 4 * 60 * 1000L); // < 5 минут таймаута
        assertFalse(InactivityGuard.isLockRequired());
    }

    private static void setBackgroundSince(long value) throws Exception {
        Field field = InactivityGuard.class.getDeclaredField("backgroundSince");
        field.setAccessible(true);
        field.set(null, value);
    }
}
