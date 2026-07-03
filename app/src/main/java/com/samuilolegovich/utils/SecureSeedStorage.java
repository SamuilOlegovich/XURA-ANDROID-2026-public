package com.samuilolegovich.utils;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.samuilolegovich.enums.StringEnum;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Двухслойное шифрование seed-фразы:
 *   слой 1 — AES-256-GCM ключом, производным от PIN через PBKDF2 (100 000 итераций)
 *   слой 2 — AES-256-GCM ключом из Android Keystore (TEE), ключ никогда не покидает чип
 *
 * Старый API (save/load) используется только для временного pre_seed и остаётся без PIN-слоя.
 * Для постоянного seed используйте saveSeed / loadSeed / reencryptSeed.
 */
public class SecureSeedStorage {

    private static final String KEYSTORE_PROVIDER    = "AndroidKeyStore";
    private static final String KEY_ALIAS            = "xura_wallet_key";
    private static final String TRANSFORMATION       = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LENGTH        = 12;
    private static final int    GCM_TAG_BITS         = 128;
    private static final int    PIN_PBKDF2_ITERATIONS = 100_000;

    // ── Старый API — только для pre_seed (без PIN-слоя) ──────────────────────

    /** Шифрует текст ключом Keystore и сохраняет IV+шифротекст (Base64) в SharedPreferences. */
    public static void save(SharedPreferences prefs, String prefKey, String plaintext) {
        try {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv         = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
            prefs.edit()
                    .putString(prefKey, Base64.getEncoder().encodeToString(concat(iv, ciphertext)))
                    .apply();
        } catch (Exception e) {
            throw new RuntimeException("SecureSeedStorage: encrypt failed", e);
        }
    }

    /** Расшифровывает значение Keystore-ключом; null если ключа нет, данные повреждены или старый формат. */
    public static String load(SharedPreferences prefs, String prefKey) {
        String stored = prefs.getString(prefKey, null);
        if (stored == null || stored.contains(" ")) return null;
        try {
            byte[] combined  = Base64.getDecoder().decode(stored);
            if (combined.length <= GCM_IV_LENGTH) return null;
            byte[] iv = Arrays.copyOf(combined, GCM_IV_LENGTH);
            byte[] ct = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ct), "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /** Удаляет зашифрованное значение из SharedPreferences (Keystore-ключ не трогает). */
    public static void delete(SharedPreferences prefs, String prefKey) {
        prefs.edit().remove(prefKey).apply();
    }

    // ── Новый API с PIN-слоем — для постоянного seed ─────────────────────────

    /**
     * Сохраняет seed с двойным шифрованием.
     * pin != null → PIN-AES-GCM → Keystore-AES-GCM → Base64 → prefs
     * pin == null → только Keystore-AES-GCM (обратная совместимость / биометрия без PIN)
     */
    public static void saveSeed(SharedPreferences prefs, String plaintext, String pin) {
        try {
            String seedKey   = StringEnum.APP_PREFERENCES_SEED.getValue();
            byte[] data      = plaintext.getBytes("UTF-8");
            boolean pinLayer = pin != null && !pin.isEmpty();

            if (pinLayer) {
                data = pinEncrypt(data, pin, prefs);
            }

            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();
            byte[] ct = cipher.doFinal(data);

            prefs.edit()
                    .putString(seedKey, Base64.getEncoder().encodeToString(concat(iv, ct)))
                    .putString(StringEnum.APP_PREFERENCES_SEED_PIN_ENABLED.getValue(),
                            pinLayer ? "true" : "false")
                    .apply();
        } catch (Exception e) {
            throw new RuntimeException("SecureSeedStorage: saveSeed failed", e);
        }
    }

    /**
     * Загружает и расшифровывает seed.
     * Возвращает null если seed_pin_enabled=true, но pin == null (PIN обязателен, но не передан),
     * или если любой слой расшифровки провалился (неверный PIN, повреждённые данные).
     */
    public static String loadSeed(SharedPreferences prefs, String pin) {
        String seedKey = StringEnum.APP_PREFERENCES_SEED.getValue();
        String stored  = prefs.getString(seedKey, null);
        if (stored == null || stored.contains(" ")) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(stored);
            if (combined.length <= GCM_IV_LENGTH) return null;
            byte[] iv = Arrays.copyOf(combined, GCM_IV_LENGTH);
            byte[] ct = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            // Keystore-слой
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] data = cipher.doFinal(ct);

            // PIN-слой
            boolean pinEnabled = "true".equals(prefs.getString(
                    StringEnum.APP_PREFERENCES_SEED_PIN_ENABLED.getValue(), "false"));
            if (pinEnabled) {
                if (pin == null || pin.isEmpty()) return null; // PIN нужен, но не передан
                data = pinDecrypt(data, pin, prefs);
                if (data == null) return null; // неверный PIN
            }

            return new String(data, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Перешифровывает seed новым PIN (вызывать при смене PIN пользователем).
     * Возвращает false, если не удалось расшифровать seed с oldPin.
     */
    public static boolean reencryptSeed(SharedPreferences prefs, String oldPin, String newPin) {
        String plaintext = loadSeed(prefs, oldPin);
        if (plaintext == null) return false;
        // Удаляем старую соль — saveSeed создаст новую
        prefs.edit().remove(StringEnum.APP_PREFERENCES_SEED_PIN_SALT.getValue()).apply();
        saveSeed(prefs, plaintext, newPin);
        return true;
    }

    // ── PIN-слой: шифрование / расшифровка ───────────────────────────────────

    private static byte[] pinEncrypt(byte[] data, String pin, SharedPreferences prefs) throws Exception {
        byte[]       salt    = getOrCreatePinSalt(prefs);
        SecretKeySpec keySpec = derivePinKey(pin, salt);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] iv = cipher.getIV();
        byte[] ct = cipher.doFinal(data);
        return concat(iv, ct);
    }

    private static byte[] pinDecrypt(byte[] data, String pin, SharedPreferences prefs) {
        try {
            String saltHex = prefs.getString(
                    StringEnum.APP_PREFERENCES_SEED_PIN_SALT.getValue(), null);
            if (saltHex == null || data.length <= GCM_IV_LENGTH) return null;
            byte[]        salt    = hexToBytes(saltHex);
            SecretKeySpec keySpec = derivePinKey(pin, salt);
            byte[] iv = Arrays.copyOf(data, GCM_IV_LENGTH);
            byte[] ct = Arrays.copyOfRange(data, GCM_IV_LENGTH, data.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(ct); // бросит AEADBadTagException при неверном PIN
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] getOrCreatePinSalt(SharedPreferences prefs) {
        String saltKey = StringEnum.APP_PREFERENCES_SEED_PIN_SALT.getValue();
        String hex     = prefs.getString(saltKey, null);
        if (hex != null) return hexToBytes(hex);
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        prefs.edit().putString(saltKey, bytesToHex(salt)).apply();
        return salt;
    }

    private static SecretKeySpec derivePinKey(String pin, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PIN_PBKDF2_ITERATIONS, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return new SecretKeySpec(keyBytes, "AES");
    }

    // ── Keystore ──────────────────────────────────────────────────────────────

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            KeyStore.SecretKeyEntry entry =
                    (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
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

    // ── Вспомогательные методы для работы с байтами ───────────────────────────

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int    len  = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        return data;
    }
}
