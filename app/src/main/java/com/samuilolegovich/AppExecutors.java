package com.samuilolegovich;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Утилитный класс-держатель общих пулов потоков приложения.
 * Нужен, чтобы не создавать новый ExecutorService в каждом классе,
 * а использовать один общий пул для фоновых IO-операций.
 */
public final class AppExecutors {
    private static final ExecutorService IO = Executors.newCachedThreadPool();

    /** Приватный конструктор запрещает создание экземпляров — класс статический. */
    private AppExecutors() {}

    /** Возвращает общий пул потоков для выполнения IO-операций (сеть, диск). */
    public static ExecutorService io() { return IO; }
}