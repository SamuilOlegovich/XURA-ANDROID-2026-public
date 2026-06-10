package com.samuilolegovich.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Тесты для класса Cipher — шифрование и дешифрование данных кошелька.
 * Любой регресс здесь означает, что пользователь потеряет доступ к своим seed-фразам.
 */
public class CipherTest {

    private static final String SEED   = "sEdVXzobfHcDjDFxpXPMKzGYGVVULVU";
    private static final String BUCKET = "bucket42";

    // -------------------------------------------------------------------------
    // encryptString / decryptString
    // -------------------------------------------------------------------------

    /** Базовый цикл: зашифровали → расшифровали → исходная строка совпадает */
    @Test
    public void encryptDecrypt_roundtrip_returnsOriginalString() {
        String salt = Cipher.encryptStringForSalt("my_salt", BUCKET);
        String encrypted = Cipher.encryptString(SEED, salt, BUCKET);
        String decrypted = Cipher.decryptString(encrypted, salt, BUCKET);
        assertEquals(SEED, decrypted);
    }

    /** Разные данные дают разный шифртекст */
    @Test
    public void encryptString_differentInputs_produceDifferentCiphertext() {
        String salt = Cipher.encryptStringForSalt("s", BUCKET);
        String enc1 = Cipher.encryptString("aaa", salt, BUCKET);
        String enc2 = Cipher.encryptString("bbb", salt, BUCKET);
        assertNotEquals(enc1, enc2);
    }

    /** Тот же ввод с другим bucket-ом даёт другой шифртекст */
    @Test
    public void encryptString_differentBucket_producesDifferentCiphertext() {
        String salt1 = Cipher.encryptStringForSalt("s", "bucket_A");
        String salt2 = Cipher.encryptStringForSalt("s", "bucket_B");
        String enc1 = Cipher.encryptString(SEED, salt1, "bucket_A");
        String enc2 = Cipher.encryptString(SEED, salt2, "bucket_B");
        assertNotEquals(enc1, enc2);
    }

    /** Одни и те же параметры дают один и тот же шифртекст (детерминированность) */
    @Test
    public void encryptString_sameInput_isDeterministic() {
        String salt = Cipher.encryptStringForSalt("fixed_salt", BUCKET);
        String enc1 = Cipher.encryptString("hello", salt, BUCKET);
        String enc2 = Cipher.encryptString("hello", salt, BUCKET);
        assertEquals(enc1, enc2);
    }

    // -------------------------------------------------------------------------
    // encryptStringForSalt
    // -------------------------------------------------------------------------

    /** Зашифрованная соль не совпадает с исходной строкой */
    @Test
    public void encryptStringForSalt_resultDiffersFromOriginal() {
        String salt = Cipher.encryptStringForSalt("my_salt_value", BUCKET);
        assertNotEquals("my_salt_value", salt);
        assertNotNull(salt);
    }

    // -------------------------------------------------------------------------
    // encryptStringIrreversibly (SHA-256)
    // -------------------------------------------------------------------------

    /** Необратимое шифрование одинаковых данных даёт одинаковый результат */
    @Test
    public void encryptStringIrreversibly_sameInput_isDeterministic() {
        String salt = Cipher.encryptStringForSalt("password_salt", BUCKET);
        String hash1 = Cipher.encryptStringIrreversibly("secret", salt, BUCKET);
        String hash2 = Cipher.encryptStringIrreversibly("secret", salt, BUCKET);
        assertEquals(hash1, hash2);
    }

    /** Необратимое шифрование разных строк даёт разные хеши */
    @Test
    public void encryptStringIrreversibly_differentInputs_produceDifferentHashes() {
        String salt = Cipher.encryptStringForSalt("s", BUCKET);
        String hash1 = Cipher.encryptStringIrreversibly("password1", salt, BUCKET);
        String hash2 = Cipher.encryptStringIrreversibly("password2", salt, BUCKET);
        assertNotEquals(hash1, hash2);
    }

    /** SHA-256 всегда возвращает 64-символьную строку */
    @Test
    public void encryptStringIrreversibly_alwaysReturns64CharHash() {
        String salt = Cipher.encryptStringForSalt("s", BUCKET);
        String hash = Cipher.encryptStringIrreversibly("any_password", salt, BUCKET);
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    // -------------------------------------------------------------------------
    // getBase64 / getFromBase64
    // -------------------------------------------------------------------------

    /** Base64-кодирование и декодирование образуют корректный цикл */
    @Test
    public void base64_encodeDecodeRoundtrip() {
        String original = "Hello, XRP!";
        String encoded = Cipher.getBase64(original);
        String decoded = Cipher.getFromBase64(encoded);
        assertEquals(original, decoded);
    }

    /** Кодирование кириллицы */
    @Test
    public void base64_encodeDecodeCyrillicRoundtrip() {
        String original = "Привет мир";
        String encoded = Cipher.getBase64(original);
        String decoded = Cipher.getFromBase64(encoded);
        assertEquals(original, decoded);
    }

    /** getFromBase64(null) не бросает исключение — возвращает null */
    @Test
    public void getFromBase64_nullInput_returnsNull() {
        assertNull(Cipher.getFromBase64(null));
    }
}