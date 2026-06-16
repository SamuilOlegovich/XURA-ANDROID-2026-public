package com.samuilolegovich.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Верификация пароля, захэшированного старой (до PBKDF2) схемой:
 * SHA-256(reverse(password) + androidId + decryptStringForSalt(saltEncrypted, androidId)).
 * Используется только один раз — для прозрачной миграции существующих
 * хэшей на {@link Cipher#hashPassword} при следующем успешном входе.
 * Новые пароли этой схемой никогда не создаются.
 */
public final class LegacyCipher {

    /** Приватный конструктор запрещает создание экземпляров — класс статический. */
    private LegacyCipher() {}

    private static final byte[] BYTES = new byte[128];
    static {
        for (int b = 0; b < 128; b++) {
            BYTES[b] = (byte) b;
        }
    }

    /** Соль PBKDF2 — это 32-символьный HEX; всё остальное — старый формат. */
    public static boolean isLegacySalt(String salt) {
        return salt != null && !salt.matches("^[0-9a-f]{32}$");
    }

    /** Воспроизводит старую формулу хеширования пароля (реверс пароля + androidId + расшифрованная соль → SHA-256), чтобы сверить с сохранённым значением при миграции. */
    public static String hash(String password, String saltEncrypted, String androidId) {
        StringBuilder sb = new StringBuilder(password);
        sb.reverse();
        sb.append(androidId);
        sb.reverse();
        sb.append(decryptStringForSalt(saltEncrypted, androidId));
        return sha256(sb.toString());
    }

    /** Расшифровывает соль из старого самодельного формата (base64 + обрезание добавленного androidId с конца). */
    private static String decryptStringForSalt(String string, String vedroId) {
        if (string == null || string.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(getFromBase64(string));
        sb.reverse();
        sb.replace(sb.length() - vedroId.length(), sb.length(), "");
        sb.reverse();
        return sb.toString();
    }

    /** Декодирует строку из старого формата (числа через пробел → байты → Base64 → UTF-8 текст), использовавшегося для хранения соли. */
    private static String getFromBase64(String string) {
        if (string == null) return null;
        try {
            String[] parts = string.split(" ");
            byte[] bb = new byte[parts.length];
            for (int i = 0; i < parts.length; i++) {
                bb[i] = BYTES[Integer.parseInt(parts[i])];
            }
            byte[] decoded = Base64.getDecoder().decode(bb);
            return new String(decoded, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /** Считает SHA-256 от строки и возвращает результат в виде HEX-строки. */
    private static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes());
            byte[] digest = md.digest();
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
