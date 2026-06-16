package com.samuilolegovich.utils;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.Assert.*;

/**
 * Тесты для LegacyCipher — верификация паролей, захэшированных старой
 * (до PBKDF2) схемой. Используется только при миграции на Cipher.hashPassword.
 */
public class LegacyCipherTest {

    private static final String ANDROID_ID = "abc123androidid";

    // Воспроизводит старую encryptStringForSalt()/getBase64() только для
    // построения тестовых фикстур — в проде эта схема больше не существует.
    private static String legacyEncryptedSalt(String rawSalt, String androidId) {
        StringBuilder sb = new StringBuilder(rawSalt);
        sb.reverse();
        sb.append(androidId);
        sb.reverse();
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Arrays.toString(Base64.getEncoder().encode(bytes))
                .replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll(",", "");
    }

    @Test
    public void hash_returns64CharShaHex() {
        String encryptedSalt = legacyEncryptedSalt("550e8400-e29b-41d4-a716-446655440000", ANDROID_ID);
        String hash = LegacyCipher.hash("mypassword", encryptedSalt, ANDROID_ID);
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void hash_isDeterministic() {
        String encryptedSalt = legacyEncryptedSalt("some-salt", ANDROID_ID);
        String h1 = LegacyCipher.hash("secret", encryptedSalt, ANDROID_ID);
        String h2 = LegacyCipher.hash("secret", encryptedSalt, ANDROID_ID);
        assertEquals(h1, h2);
    }

    @Test
    public void hash_differentPasswordsProduceDifferentHashes() {
        String encryptedSalt = legacyEncryptedSalt("some-salt", ANDROID_ID);
        assertNotEquals(
                LegacyCipher.hash("password1", encryptedSalt, ANDROID_ID),
                LegacyCipher.hash("password2", encryptedSalt, ANDROID_ID));
    }

    @Test
    public void hash_differentAndroidIdsProduceDifferentHashes() {
        String encryptedSalt1 = legacyEncryptedSalt("some-salt", "device-one");
        String encryptedSalt2 = legacyEncryptedSalt("some-salt", "device-two");
        assertNotEquals(
                LegacyCipher.hash("samePassword", encryptedSalt1, "device-one"),
                LegacyCipher.hash("samePassword", encryptedSalt2, "device-two"));
    }

    @Test
    public void isLegacySalt_recognizesOldFormat() {
        String encryptedSalt = legacyEncryptedSalt("some-salt", ANDROID_ID);
        assertTrue(LegacyCipher.isLegacySalt(encryptedSalt));
    }

    @Test
    public void isLegacySalt_rejectsNewPbkdf2Format() {
        assertFalse(LegacyCipher.isLegacySalt(Cipher.generateSalt()));
    }

    @Test
    public void isLegacySalt_nullIsNotLegacy() {
        assertFalse(LegacyCipher.isLegacySalt(null));
    }
}
