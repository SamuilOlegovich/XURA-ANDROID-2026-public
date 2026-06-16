package com.samuilolegovich.utils;

/**
 * Отслеживает время, проведённое приложением в фоне, и определяет, не пора ли
 * заблокировать приложение и потребовать повторный ввод пароля (автоблокировка по неактивности).
 */
public class InactivityGuard {

    private static final long TIMEOUT_MS = 5 * 60 * 1000L; // 5 минут
    private static volatile long backgroundSince = 0L;

    /** Вызывается, когда все Activity ушли в stopped (приложение свёрнуто) — запоминает момент ухода в фон. */
    public static void onBackground() {
        backgroundSince = System.currentTimeMillis();
    }

    /** Вызывается, когда первая Activity вернулась в started (приложение снова на экране) — сбрасывает отсчёт. */
    public static void onForeground() {
        backgroundSince = 0L;
    }

    /** Сбрасывает таймер без перехода в foreground (используется после показа экрана блокировки, чтобы не запросить пароль повторно). */
    public static void reset() {
        backgroundSince = 0L;
    }

    /** Возвращает true, если приложение провело в фоне больше TIMEOUT_MS и пора показать экран ввода пароля. */
    public static boolean isLockRequired() {
        long t = backgroundSince;
        return t > 0L && (System.currentTimeMillis() - t) >= TIMEOUT_MS;
    }
}
