package com.samuilolegovich.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static java.lang.Integer.parseInt;

public class Cipher {
    private  static byte[] bytes = new byte[128];

    // зашифровать
    public static String encryptString(String string, String salt, String vedroId) {
        StringBuilder stringBuilder = new StringBuilder(string);
        stringBuilder.reverse();
        stringBuilder.append(vedroId);
        stringBuilder.reverse();
        stringBuilder.append(decryptStringForSalt(salt, vedroId));
        stringBuilder.reverse();
        return getBase64(stringBuilder.toString());
    }

    // расшифровать
    public static String decryptString(String string, String salt,  String vedroId) {
        StringBuilder stringBuilder = new StringBuilder(getFromBase64(string));
        stringBuilder.reverse();
        stringBuilder.replace(stringBuilder.length()
                - decryptStringForSalt(salt, vedroId).length(),
                stringBuilder.length(), "");
        stringBuilder.reverse();
        stringBuilder.replace(stringBuilder.length() - vedroId.length(),
                stringBuilder.length(), "");
        stringBuilder.reverse();
        return stringBuilder.toString();
    }

    // зашифровать соль
    public static String encryptStringForSalt(String string, String vedroId) {
        StringBuilder stringBuilder = new StringBuilder(string);
        stringBuilder.reverse();
        stringBuilder.append(vedroId);
        stringBuilder.reverse();
        return getBase64(stringBuilder.toString());
    }

    // расшифровать соль
    private static String decryptStringForSalt(String string, String vedroId) {
        StringBuilder stringBuilder = new StringBuilder(getFromBase64(string));
        stringBuilder.reverse();
        stringBuilder.replace(stringBuilder.length() - vedroId.length(),
                stringBuilder.length(), "");
        stringBuilder.reverse();
        return stringBuilder.toString();
    }


    // необратимо зашифровать
    public static String encryptStringIrreversibly(String string, String salt, String vedroId) {
        StringBuilder stringBuilder = new StringBuilder(string);
        stringBuilder.reverse();
        stringBuilder.append(vedroId);
        stringBuilder.reverse();
        stringBuilder.append(decryptStringForSalt(salt, vedroId));
        return SHA256(stringBuilder.toString());
    }

    private static String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    private static String SHA(final String strText, final String strType) {
        // возвращаемое значение
        String strResult = null;

        // Это допустимая строка
        if (strText != null && strText.length() > 0) {
            try {
                // Шифрование SHA начинается
                // Создаем объект шифрования и передаем тип шифрования
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // Передаем строку для шифрования
                messageDigest.update(strText.getBytes());
                // получаем результат байтового типа
                byte byteBuffer[] = messageDigest.digest();

                // конвертируем байт в строку
                StringBuffer strHexString = new StringBuffer();
                // Обходим байтовый буфер
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // получаем результат возврата
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }

    // шифрование
    public static String getBase64(String string) {
        String result = null;
        byte[] bytes = null;

        try {
            bytes = string.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (bytes != null) {
            result = Arrays.toString(Base64.getEncoder().encode(bytes))
                    .replaceAll("\\[", "")
                    .replaceAll("]", "")
                    .replaceAll(",", "");
        }
        
        return result;
    }

    // расшифровать
    public static String getFromBase64(String string) {
        String result = null;
        getByteList();
        byte[] bb;
        byte[] b;

        if (string != null) {
            try {
                String[] strings = string.split(" ");

                bb = new byte[strings.length];

                for (int i = 0; i < strings.length; i++) {
                    bb[i] = bytes[parseInt(strings[i])];
                }

                b = Base64.getDecoder().decode(bb);
                result = new String(b, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // пересмотреть этот момент так быть не должно сделал на скорую руку кастыль чтобы не зависать
    private static void getByteList() {
        for (int b = 0; b < 128; b++) {
            bytes[b] = (byte) b;
        }
    }
}
