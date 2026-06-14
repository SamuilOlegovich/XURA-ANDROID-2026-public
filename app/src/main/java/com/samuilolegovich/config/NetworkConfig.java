package com.samuilolegovich.config;

import android.content.SharedPreferences;

import com.samuilolegovich.enums.StringEnum;

/**
 * Runtime network configuration — single source of truth for URLs and server addresses.
 *
 * Load on app start via {@link #load(SharedPreferences)}.
 * Persist changes via {@link #save(SharedPreferences)}.
 *
 * All fields are volatile — safe to read from any thread.
 */
public final class NetworkConfig {

    // ── SharedPreferences keys ────────────────────────────────────────────
    public static final String KEY_TESTNET        = "dev_use_testnet";
    public static final String KEY_SERVER_ROULETTE = "dev_server_roulette";
    public static final String KEY_SERVER_COLOR    = "dev_server_color";
    public static final String KEY_SERVER_NUMBER   = "dev_server_number";

    // ── Runtime state (read on any thread) ───────────────────────────────
    public static volatile boolean IS_TESTNET      = false;
    public static volatile String  SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();
    public static volatile String  SERVER_COLOR    = StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue();
    public static volatile String  SERVER_NUMBER   = StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue();

    private NetworkConfig() {}

    // ── URL accessors ────────────────────────────────────────────────────

    /** JSON-RPC endpoint used by WalletXRP for submitting transactions. */
    public static String getRpcUrl() {
        return IS_TESTNET
                ? StringEnum.NET_TEST.getValue()
                : StringEnum.NET_REAL_POST_URL_ONE.getValue();
    }

    /** WebSocket endpoint used by SocketXRP for ledger subscriptions. */
    public static String getWssUrl() {
        return IS_TESTNET
                ? StringEnum.WSS_TEST.getValue()
                : StringEnum.WSS_REAL.getValue();
    }

    // ── Persistence ──────────────────────────────────────────────────────

    /** Call once in MainActivity.onCreate() to restore persisted dev settings. */
    public static void load(SharedPreferences prefs) {
        IS_TESTNET      = prefs.getBoolean(KEY_TESTNET, false);
        SERVER_ROULETTE = prefs.getString(KEY_SERVER_ROULETTE,
                StringEnum.SERVER_ADDRESS_ROULETTE.getValue());
        SERVER_COLOR    = prefs.getString(KEY_SERVER_COLOR,
                StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue());
        SERVER_NUMBER   = prefs.getString(KEY_SERVER_NUMBER,
                StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue());
    }

    /** Persists current state to SharedPreferences. Call after any change. */
    public static void save(SharedPreferences prefs) {
        prefs.edit()
                .putBoolean(KEY_TESTNET, IS_TESTNET)
                .putString(KEY_SERVER_ROULETTE, SERVER_ROULETTE)
                .putString(KEY_SERVER_COLOR, SERVER_COLOR)
                .putString(KEY_SERVER_NUMBER, SERVER_NUMBER)
                .apply();
    }

    /** Resets server addresses to their hardcoded defaults (does not reset testnet flag). */
    public static void resetAddresses(SharedPreferences prefs) {
        SERVER_ROULETTE = StringEnum.SERVER_ADDRESS_ROULETTE.getValue();
        SERVER_COLOR    = StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue();
        SERVER_NUMBER   = StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue();
        save(prefs);
    }
}
