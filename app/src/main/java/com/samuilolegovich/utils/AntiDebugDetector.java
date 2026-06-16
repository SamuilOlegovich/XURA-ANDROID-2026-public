package com.samuilolegovich.utils;

import android.os.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;

/**
 * Обнаруживает подключённый JDWP-дебаггер и запущенный frida-server.
 * Каждый метод — независимый эвристический сигнал (открытый порт, инжектированная
 * библиотека в памяти процесса, именованный поток GLib-event-loop, который
 * Frida создаёт при инжекции). Сетевая проверка портов требует фонового
 * потока — вызывать isDetected() только не с главного потока.
 */
public class AntiDebugDetector {

    private static final int[] FRIDA_PORTS = {27042, 27043};

    private static final String[] FRIDA_MARKERS = {
            "frida", "gum-js-loop", "gmain", "gdbus", "linjector"
    };

    /** Возвращает true, если сработал хотя бы один из эвристических признаков отладчика/Frida. */
    public static boolean isDetected() {
        return checkDebugger()
                || checkFridaPorts()
                || checkProcMaps()
                || checkThreadNames();
    }

    /** Проверяет через стандартный Android API, подключён ли JDWP-дебаггер или ожидается его подключение. */
    private static boolean checkDebugger() {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger();
    }

    /** Пытается подключиться к стандартным TCP-портам frida-server (USB/TCP-режим) — открытый порт означает запущенный Frida. */
    private static boolean checkFridaPorts() {
        for (int port : FRIDA_PORTS) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 200);
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /** Ищет в карте памяти процесса (/proc/self/maps) следы инжектированных Frida-библиотек. */
    private static boolean checkProcMaps() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (containsMarker(line)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    /** Ищет среди потоков процесса именованные потоки GLib event loop, которые создаёт Frida при инжекции. */
    private static boolean checkThreadNames() {
        File[] tasks = new File("/proc/self/task").listFiles();
        if (tasks == null) return false;

        for (File task : tasks) {
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(task, "comm")))) {
                String name = reader.readLine();
                if (name != null && containsMarker(name)) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /** Проверяет, содержит ли строка одну из известных текстовых меток Frida (без учёта регистра). */
    private static boolean containsMarker(String line) {
        String lower = line.toLowerCase(Locale.US);
        for (String marker : FRIDA_MARKERS) {
            if (lower.contains(marker)) return true;
        }
        return false;
    }
}
