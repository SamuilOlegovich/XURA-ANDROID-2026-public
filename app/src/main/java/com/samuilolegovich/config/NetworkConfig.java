package com.samuilolegovich.config;

import android.content.SharedPreferences;

import com.samuilolegovich.enums.StringEnum;

/**
 * Конфигурация сети в рантайме — единый источник истины для URL-адресов узла и адресов игровых серверов.
 *
 * Все игры (рулетка, угадай цвет, угадай число) используют один адрес {@link #SERVER_ROULETTE}.
 * Используйте switchNetwork() при переключении сети в DEV-панели.
 */
public final class NetworkConfig {

    // ── Ключи SharedPreferences ────────────────────────────────────────────
    public static final String KEY_TESTNET = "dev_use_testnet";

    private static final String KEY_ROULETTE_REAL  = "dev_server_roulette_real";
    private static final String KEY_REFERRAL_REAL  = "dev_server_referral_real";

    private static final String KEY_ROULETTE_TEST  = "dev_server_roulette_test";
    private static final String KEY_REFERRAL_TEST  = "dev_server_referral_test";

    // Старый ключ (до раздельного хранения по режимам) — используется как fallback при миграции
    private static final String KEY_ROULETTE_LEGACY = "dev_server_roulette";

    // ── Состояние в рантайме (можно читать из любого потока) ───────────────
    public static volatile boolean IS_TESTNET      = false;
    public static volatile String  SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();
    public static volatile String  SERVER_REFERRAL = StringEnum.SERVER_ADDRESS_BECOME_REFERRAL.getValue();

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

    /** Загружает из SharedPreferences режим сети и адреса игровых серверов для этого режима (с миграцией старых ключей). */
    public static void load(SharedPreferences prefs) {
        IS_TESTNET      = prefs.getBoolean(KEY_TESTNET, false);
        SERVER_ROULETTE = loadRoulette(prefs);
        SERVER_REFERRAL = loadReferral(prefs);
    }

    /** Сохраняет в SharedPreferences текущий режим сети и адреса серверов под ключами, соответствующими этому режиму. */
    public static void save(SharedPreferences prefs) {
        prefs.edit()
                .putBoolean(KEY_TESTNET,      IS_TESTNET)
                .putString(rouletteKey(),  SERVER_ROULETTE)
                .putString(referralKey(),  SERVER_REFERRAL)
                .apply();
    }

    /**
     * Вызывается при переключении сети в DEV-панели. Сохраняет адреса текущего режима, переключает флаг,
     * затем загружает адреса нового режима, чтобы поля в UI отражали правильный набор.
     */
    public static void switchNetwork(SharedPreferences prefs, boolean newIsTestnet) {
        save(prefs);
        IS_TESTNET      = newIsTestnet;
        SERVER_ROULETTE = loadRoulette(prefs);
        SERVER_REFERRAL = loadReferral(prefs);
        prefs.edit().putBoolean(KEY_TESTNET, IS_TESTNET).apply();
    }

    /** Сбрасывает адреса игровых серверов на значения по умолчанию и сохраняет их в SharedPreferences. */
    public static void resetAddresses(SharedPreferences prefs) {
        SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();
        SERVER_REFERRAL = StringEnum.SERVER_ADDRESS_BECOME_REFERRAL.getValue();
        save(prefs);
    }

    // ── Внутренние помощники ─────────────────────────────────────────────

    /** Возвращает ключ SharedPreferences для адреса сервера рулетки, соответствующий текущему режиму сети. */
    private static String rouletteKey() { return IS_TESTNET ? KEY_ROULETTE_TEST : KEY_ROULETTE_REAL; }
    /** Возвращает ключ SharedPreferences для адреса реферального сервера, соответствующий текущему режиму сети. */
    private static String referralKey() { return IS_TESTNET ? KEY_REFERRAL_TEST : KEY_REFERRAL_REAL; }

    /** Адрес сервера рулетки по умолчанию (зашит в StringEnum). */
    private static String defaultRoulette() { return StringEnum.SERVER_ADDRESS_ROULETTE.getValue(); }
    /** Адрес реферального сервера по умолчанию (зашит в StringEnum). */
    private static String defaultReferral() { return StringEnum.SERVER_ADDRESS_BECOME_REFERRAL.getValue(); }

    /** Читает адрес сервера рулетки для текущего режима, при отсутствии — мигрирует значение со старого общего ключа. */
    private static String loadRoulette(SharedPreferences p) {
        String key = rouletteKey();
        if (p.contains(key)) return p.getString(key, defaultRoulette());
        // Миграция со старого ключа (использовался до раздельного хранения по режимам)
        return p.getString(KEY_ROULETTE_LEGACY, defaultRoulette());
    }

    /** Читает адрес реферального сервера для текущего режима; при отсутствии возвращает значение по умолчанию. */
    private static String loadReferral(SharedPreferences p) {
        return p.getString(referralKey(), defaultReferral());
    }
}
