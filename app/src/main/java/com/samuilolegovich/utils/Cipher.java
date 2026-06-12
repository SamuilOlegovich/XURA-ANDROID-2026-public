package com.samuilolegovich.utils;

import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Cipher {

    private static final int ITERATIONS = 310_000;
    private static final int KEY_BITS   = 256;
    private static final int SALT_BYTES = 16;

    /** Генерирует криптографически случайную соль (16 байт → HEX). */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * PBKDF2WithHmacSHA256 — 310 000 итераций, 256-бит выход.
     * saltHex — результат generateSalt(), хранится в EncryptedSharedPreferences.
     */
    public static String hashPassword(String password, String saltHex) {
        try {
            byte[] salt = hexToBytes(saltHex);
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, ITERATIONS, KEY_BITS);
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("hashPassword failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
