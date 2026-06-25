package com.samuilolegovich.config;

import android.content.SharedPreferences;

import com.samuilolegovich.enums.StringEnum;

/**
 * Конфигурация сети в рантайме — единый источник истины для URL-адресов узла и адреса игрового сервера.
 *
 * Все игры (рулетка, угадай цвет, угадай число, реферал) используют один адрес {@link #SERVER_ROULETTE}.
 * Используйте switchNetwork() при переключении сети в DEV-панели.
 */
public final class NetworkConfig {

    // ── Ключи SharedPreferences ────────────────────────────────────────────
    public static final String KEY_TESTNET = "dev_use_testnet";

    private static final String KEY_ROULETTE_REAL = "dev_server_roulette_real";
    private static final String KEY_ROULETTE_TEST = "dev_server_roulette_test";

    // Старый ключ (до раздельного хранения по режимам) — используется как fallback при миграции
    private static final String KEY_ROULETTE_LEGACY = "dev_server_roulette";

    // ── Состояние в рантайме (можно читать из любого потока) ───────────────
    public static volatile boolean IS_TESTNET      = false;
    public static volatile String  SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();

    /** Приватный конструктор запрещает создание экземпляров — класс статический. */
    private NetworkConfig() {}

    // ── Доступ к URL ────────────────────────────────────────────────────

    /** Возвращает RPC-адрес XRPL-узла для текущего режима сети (testnet или mainnet). */
    public static String getRpcUrl() {
        return IS_TESTNET
                ? StringEnum.NET_TEST.getValue()
                : StringEnum.NET_REAL_POST_URL_ONE.getValue();
    }

    /** Возвращает адрес WebSocket-узла для текущего режима сети (testnet или mainnet). */
    public static String getWssUrl() {
        return IS_TESTNET
                ? StringEnum.WSS_TEST.getValue()
                : StringEnum.WSS_REAL.getValue();
    }

    // ── Сохранение настроек ──────────────────────────────────────────────

    /** Загружает из SharedPreferences режим сети и адрес игрового сервера (с миграцией старых ключей). */
    public static void load(SharedPreferences prefs) {
        IS_TESTNET      = prefs.getBoolean(KEY_TESTNET, false);
        SERVER_ROULETTE = loadRoulette(prefs);
    }

    /** Сохраняет в SharedPreferences текущий режим сети и адрес сервера под ключом, соответствующим режиму. */
    public static void save(SharedPreferences prefs) {
        prefs.edit()
                .putBoolean(KEY_TESTNET,     IS_TESTNET)
                .putString(rouletteKey(), SERVER_ROULETTE)
                .apply();
    }

    /**
     * Вызывается при переключении сети в DEV-панели. Сохраняет адрес текущего режима, переключает флаг,
     * затем загружает адрес нового режима, чтобы поле в UI отразило правильное значение.
     */
    public static void switchNetwork(SharedPreferences prefs, boolean newIsTestnet) {
        save(prefs);
        IS_TESTNET      = newIsTestnet;
        SERVER_ROULETTE = loadRoulette(prefs);
        prefs.edit().putBoolean(KEY_TESTNET, IS_TESTNET).apply();
    }

    /** Сбрасывает адрес игрового сервера на значение по умолчанию и сохраняет в SharedPreferences. */
    public static void resetAddresses(SharedPreferences prefs) {
        SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();
        save(prefs);
    }

    // ── Внутренние помощники ─────────────────────────────────────────────

    /** Возвращает ключ SharedPreferences для адреса сервера, соответствующий текущему режиму сети. */
    private static String rouletteKey() { return IS_TESTNET ? KEY_ROULETTE_TEST : KEY_ROULETTE_REAL; }

    /** Адрес сервера по умолчанию (зашит в StringEnum). */
    private static String defaultRoulette() { return StringEnum.SERVER_ADDRESS_ROULETTE.getValue(); }

    /** Читает адрес сервера для текущего режима, при отсутствии — мигрирует значение со старого общего ключа. */
    private static String loadRoulette(SharedPreferences p) {
        String key = rouletteKey();
        if (p.contains(key)) return p.getString(key, defaultRoulette());
        // Миграция со старого ключа (использовался до раздельного хранения по режимам)
        return p.getString(KEY_ROULETTE_LEGACY, defaultRoulette());
    }
}
