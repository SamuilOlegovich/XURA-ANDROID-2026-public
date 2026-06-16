package com.samuilolegovich.utils;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Аппаратное шифрование seed-фразы через Android Keystore (AES-256-GCM).
 * Ключ генерируется в TEE (Trusted Execution Environment) и никогда не покидает устройство,
 * поэтому даже при компрометации зашифрованных данных в SharedPreferences расшифровать
 * seed без физического доступа к устройству (и его TEE) невозможно.
 */
public class SecureSeedStorage {

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "xura_wallet_key";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;

    /** Шифрует переданный текст ключом из Keystore и сохраняет IV+шифротекст (в Base64) в SharedPreferences. */
    public static void save(SharedPreferences prefs, String prefKey, String plaintext) {
        try {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            prefs.edit()
                    .putString(prefKey, Base64.getEncoder().encodeToString(combined))
                    .apply();
        } catch (Exception e) {
            throw new RuntimeException("SecureSeedStorage: encrypt failed", e);
        }
    }

    /** Расшифровывает сохранённое значение; возвращает null, если ключа нет, данные повреждены или это старый (не-Keystore) формат. */
    public static String load(SharedPreferences prefs, String prefKey) {
        String stored = prefs.getString(prefKey, null);
        if (stored == null || stored.contains(" ")) return null; // старый формат — пробелы
        try {
            byte[] combined = Base64.getDecoder().decode(stored);
            if (combined.length <= GCM_IV_LENGTH) return null;
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /** Удаляет сохранённое зашифрованное значение из SharedPreferences (сам ключ Keystore не трогает). */
    public static void delete(SharedPreferences prefs, String prefKey) {
        prefs.edit().remove(prefKey).apply();
    }

    /** Возвращает существующий AES-256-ключ из Android Keystore либо генерирует новый, если его ещё нет. */
    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            return entry.getSecretKey();
        }
        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
        kg.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        return kg.generateKey();
    }
}