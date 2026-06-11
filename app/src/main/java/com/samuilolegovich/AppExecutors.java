package com.samuilolegovich;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public final class AppExecutors {
    private static final ExecutorService IO = Executors.newCachedThreadPool();

    private AppExecutors() {}

    public static ExecutorService io() { return IO; }
}