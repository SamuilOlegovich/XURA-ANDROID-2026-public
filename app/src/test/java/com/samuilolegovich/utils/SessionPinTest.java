package com.samuilolegovich.utils;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Тесты для SessionPin — volatile in-memory держатель PIN текущей сессии.
 */
public class SessionPinTest {

    @After
    public void tearDown() {
        SessionPin.clear();
    }

    // -------------------------------------------------------------------------
    // Начальное состояние
    // -------------------------------------------------------------------------

    @Test
    public void initialState_getReturnsNull() {
        SessionPin.clear();
        assertNull(SessionPin.get());
    }

    @Test
    public void initialState_isAvailableReturnsFalse() {
        SessionPin.clear();
        assertFalse(SessionPin.isAvailable());
    }

    // -------------------------------------------------------------------------
    // set / get
    // -------------------------------------------------------------------------

    @Test
    public void set_get_returnsSameValue() {
        SessionPin.set("123456");
        assertEquals("123456", SessionPin.get());
    }

    @Test
    public void set_overwritesPreviousValue() {
        SessionPin.set("111111");
        SessionPin.set("999999");
        assertEquals("999999", SessionPin.get());
    }

    // -------------------------------------------------------------------------
    // isAvailable
    // -------------------------------------------------------------------------

    @Test
    public void isAvailable_trueAfterSet() {
        SessionPin.set("000000");
        assertTrue(SessionPin.isAvailable());
    }

    @Test
    public void isAvailable_falseAfterClear() {
        SessionPin.set("123456");
        SessionPin.clear();
        assertFalse(SessionPin.isAvailable());
    }

    // -------------------------------------------------------------------------
    // clear
    // -------------------------------------------------------------------------

    @Test
    public void clear_setsGetToNull() {
        SessionPin.set("123456");
        SessionPin.clear();
        assertNull(SessionPin.get());
    }

    @Test
    public void clear_onEmptyState_doesNotThrow() {
        SessionPin.clear();
        SessionPin.clear(); // повторный вызов не должен бросать исключение
        assertNull(SessionPin.get());
    }
}
