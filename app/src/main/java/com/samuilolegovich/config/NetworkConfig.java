package com.samuilolegovich.config;

import android.content.SharedPreferences;

import com.samuilolegovich.enums.StringEnum;

/**
 * Runtime network configuration — single source of truth for URLs and server addresses.
 *
 * Server addresses are stored separately for mainnet and testnet.
 * Use switchNetwork() when toggling the DEV panel network toggle — it saves the current
 * mode's addresses and loads the new mode's set so fields update correctly.
 */
public final class NetworkConfig {

    // ── SharedPreferences keys ────────────────────────────────────────────
    public static final String KEY_TESTNET = "dev_use_testnet";

    private static final String KEY_ROULETTE_REAL = "dev_server_roulette_real";
    private static final String KEY_COLOR_REAL    = "dev_server_color_real";
    private static final String KEY_NUMBER_REAL   = "dev_server_number_real";

    private static final String KEY_ROULETTE_TEST = "dev_server_roulette_test";
    private static final String KEY_COLOR_TEST    = "dev_server_color_test";
    private static final String KEY_NUMBER_TEST   = "dev_server_number_test";

    // Legacy keys (before per-mode storage was added) — migration fallback
    private static final String KEY_ROULETTE_LEGACY = "dev_server_roulette";
    private static final String KEY_COLOR_LEGACY    = "dev_server_color";
    private static final String KEY_NUMBER_LEGACY   = "dev_server_number";

    // ── Runtime state (read on any thread) ───────────────────────────────
    public static volatile boolean IS_TESTNET      = false;
    public static volatile String  SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();
    public static volatile String  SERVER_COLOR    = StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue();
    public static volatile String  SERVER_NUMBER   = StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue();

    private NetworkConfig() {}

    // ── URL accessors ────────────────────────────────────────────────────

    public static String getRpcUrl() {
        return IS_TESTNET
                ? StringEnum.NET_TEST.getValue()
                : StringEnum.NET_REAL_POST_URL_ONE.getValue();
    }

    public static String getWssUrl() {
        return IS_TESTNET
                ? StringEnum.WSS_TEST.getValue()
                : StringEnum.WSS_REAL.getValue();
    }

    // ── Persistence ──────────────────────────────────────────────────────

    public static void load(SharedPreferences prefs) {
        IS_TESTNET      = prefs.getBoolean(KEY_TESTNET, false);
        SERVER_ROULETTE = loadRoulette(prefs);
        SERVER_COLOR    = loadColor(prefs);
        SERVER_NUMBER   = loadNumber(prefs);
    }

    public static void save(SharedPreferences prefs) {
        prefs.edit()
                .putBoolean(KEY_TESTNET, IS_TESTNET)
                .putString(rouletteKey(), SERVER_ROULETTE)
                .putString(colorKey(),    SERVER_COLOR)
                .putString(numberKey(),   SERVER_NUMBER)
                .apply();
    }

    /**
     * Call from the DEV network toggle. Saves current mode's addresses, switches the flag,
     * then loads the new mode's addresses so the UI fields reflect the right set.
     */
    public static void switchNetwork(SharedPreferences prefs, boolean newIsTestnet) {
        save(prefs);
        IS_TESTNET      = newIsTestnet;
        SERVER_ROULETTE = loadRoulette(prefs);
        SERVER_COLOR    = loadColor(prefs);
        SERVER_NUMBER   = loadNumber(prefs);
        prefs.edit().putBoolean(KEY_TESTNET, IS_TESTNET).apply();
    }

    public static void resetAddresses(SharedPreferences prefs) {
        SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();
        SERVER_COLOR    = StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue();
        SERVER_NUMBER   = StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue();
        save(prefs);
    }

    // ── Internal helpers ─────────────────────────────────────────────────

    private static String rouletteKey() { return IS_TESTNET ? KEY_ROULETTE_TEST : KEY_ROULETTE_REAL; }
    private static String colorKey()    { return IS_TESTNET ? KEY_COLOR_TEST    : KEY_COLOR_REAL; }
    private static String numberKey()   { return IS_TESTNET ? KEY_NUMBER_TEST   : KEY_NUMBER_REAL; }

    private static String defaultRoulette() { return StringEnum.SERVER_ADDRESS_ROULETTE.getValue(); }
    private static String defaultColor()    { return StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue(); }
    private static String defaultNumber()   { return StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue(); }

    private static String loadRoulette(SharedPreferences p) {
        String key = rouletteKey();
        if (p.contains(key)) return p.getString(key, defaultRoulette());
        // Migrate from legacy key (used before per-mode storage)
        return p.getString(KEY_ROULETTE_LEGACY, defaultRoulette());
    }

    private static String loadColor(SharedPreferences p) {
        String key = colorKey();
        if (p.contains(key)) return p.getString(key, defaultColor());
        return p.getString(KEY_COLOR_LEGACY, defaultColor());
    }

    private static String loadNumber(SharedPreferences p) {
        String key = numberKey();
        if (p.contains(key)) return p.getString(key, defaultNumber());
        return p.getString(KEY_NUMBER_LEGACY, defaultNumber());
    }
}
