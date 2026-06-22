package com.samuilolegovich.utils;

/**
 * Отслеживает время, проведённое приложением в фоне, и определяет, не пора ли
 * заблокировать приложение и потребовать повторный ввод пароля (автоблокировка по неактивности).
 */
public class InactivityGuard {

    public static final long DEFAULT_TIMEOUT_MS = 5 * 60 * 1000L; // 5 минут по умолчанию
    private static volatile long timeoutMs      = DEFAULT_TIMEOUT_MS;
    private static volatile long backgroundSince = 0L;

    /** Устанавливает таймаут автоблокировки (в миллисекундах). Вызывается при старте и при смене настройки. */
    public static void setTimeoutMs(long ms) {
        timeoutMs = ms;
    }

    /** Возвращает текущий таймаут автоблокировки в миллисекундах. */
    public static long getTimeoutMs() {
        return timeoutMs;
    }

    /** Вызывается, когда все Activity ушли в stopped (приложение свёрнуто) — запоминает момент ухода в фон. */
    public static void onBackground() {
        backgroundSince = System.currentTimeMillis();
    }

    /** Сбрасывает таймер (используется после проверки блокировки или показа экрана блокировки). */
    public static void onForeground() {
        backgroundSince = 0L;
    }

    /** Сбрасывает таймер без перехода в foreground (используется после показа экрана блокировки, чтобы не запросить пароль повторно). */
    public static void reset() {
        backgroundSince = 0L;
    }

    /** Возвращает true, если приложение провело в фоне больше timeoutMs и пора показать экран ввода пароля. */
    public static boolean isLockRequired() {
        long t = backgroundSince;
        return t > 0L && (System.currentTimeMillis() - t) >= timeoutMs;
    }
}
