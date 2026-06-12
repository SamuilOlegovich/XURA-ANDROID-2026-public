package com.samuilolegovich.utils;

public class InactivityGuard {

    private static final long TIMEOUT_MS = 5 * 60 * 1000L; // 5 минут
    private static volatile long backgroundSince = 0L;

    // Вызывается когда все Activity ушли в stopped (приложение свёрнуто)
    public static void onBackground() {
        backgroundSince = System.currentTimeMillis();
    }

    // Вызывается когда первая Activity вернулась в started (приложение снова на экране)
    public static void onForeground() {
        backgroundSince = 0L;
    }

    // Сбрасывает таймер без перехода в foreground (используется после показа экрана блокировки)
    public static void reset() {
        backgroundSince = 0L;
    }

    public static boolean isLockRequired() {
        long t = backgroundSince;
        return t > 0L && (System.currentTimeMillis() - t) >= TIMEOUT_MS;
    }
}
