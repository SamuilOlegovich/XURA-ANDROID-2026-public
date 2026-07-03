package com.samuilolegovich.utils;

/**
 * Держит PIN текущей сессии в памяти (volatile — потокобезопасно).
 * Устанавливается после успешной PIN-аутентификации, сбрасывается при блокировке приложения.
 * Никогда не сохраняется на диск.
 */
public class SessionPin {
    private static volatile String pin = null;

    public static void set(String value)   { pin = value; }
    public static String get()             { return pin; }
    public static void clear()             { pin = null; }
    public static boolean isAvailable()    { return pin != null; }
}
