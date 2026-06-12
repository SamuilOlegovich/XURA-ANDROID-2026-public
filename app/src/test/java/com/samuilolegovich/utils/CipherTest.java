package com.samuilolegovich.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Тесты для Cipher — PBKDF2WithHmacSHA256 + SecureRandom salt.
 */
public class CipherTest {

    // -------------------------------------------------------------------------
    // generateSalt
    // -------------------------------------------------------------------------

    @Test
    public void generateSalt_returnsNonNull() {
        assertNotNull(Cipher.generateSalt());
    }

    @Test
    public void generateSalt_returns32CharHex() {
        // 16 байт → 32 HEX символа
        assertEquals(32, Cipher.generateSalt().length());
    }

    @Test
    public void generateSalt_differentCallsProduceDifferentSalts() {
        assertNotEquals(Cipher.generateSalt(), Cipher.generateSalt());
    }

    // -------------------------------------------------------------------------
    // hashPassword
    // -------------------------------------------------------------------------

    @Test
    public void hashPassword_sameInputIsDeterministic() {
        String salt = Cipher.generateSalt();
        String h1 = Cipher.hashPassword("secret", salt);
        String h2 = Cipher.hashPassword("secret", salt);
        assertEquals(h1, h2);
    }

    @Test
    public void hashPassword_differentPasswordsProduceDifferentHashes() {
        String salt = Cipher.generateSalt();
        assertNotEquals(
                Cipher.hashPassword("password1", salt),
                Cipher.hashPassword("password2", salt));
    }

    @Test
    public void hashPassword_differentSaltsProduceDifferentHashes() {
        String salt1 = Cipher.generateSalt();
        String salt2 = Cipher.generateSalt();
        assertNotEquals(
                Cipher.hashPassword("samePassword", salt1),
                Cipher.hashPassword("samePassword", salt2));
    }

    @Test
    public void hashPassword_returns64CharHex() {
        // 256 бит → 32 байта → 64 HEX символа
        String hash = Cipher.hashPassword("any", Cipher.generateSalt());
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void hashPassword_outputIsLowercaseHex() {
        String hash = Cipher.hashPassword("test", Cipher.generateSalt());
        assertTrue(hash.matches("[0-9a-f]+"));
    }
}
