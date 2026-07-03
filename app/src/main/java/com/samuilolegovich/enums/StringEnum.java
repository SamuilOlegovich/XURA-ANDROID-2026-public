package com.samuilolegovich.enums;



/**
 * Централизованный набор неизменяемых строковых констант приложения: ключи
 * SharedPreferences, коды языков, адреса игровых серверов, служебные теги
 * транзакций, лимиты ставок и URL-адреса XRPL-сетей (mainnet/testnet).
 */
public enum StringEnum {
    ONBOARDING_SEEN("onboarding_seen"),
    APP_PREFERENCES_BIOMETRIC_ENABLED("biometric_enabled"),
    APP_PREFERENCES_LOCK_TIMEOUT("lock_timeout"),
    APP_PREFERENCES_SOUND_ENABLED("sound_enabled"),
    APP_PREFERENCES_PASSWORD_NOT_INSTALLED("password not installed"),
    APP_PREFERENCES_REFERRAL_NOT_INSTALLED("0"),
    APP_PREFERENCES_PASSWORD("password"),
    APP_PREFERENCES_PRE_SEED("pre_seed"),
    APP_PREFERENCES_REFERRAL("referral"),
    APP_PREFERENCES_LOCALE("locale"),
    APP_PREFERENCES("my_settings"),
    APP_PREFERENCES_SEED("seed"),
    APP_PREFERENCES_SALT("salt"),
    APP_GAME_MODE_TEST("false"),
    APP_RUSSIAN_LANGUAGE("ru"),
    APP_ENGLISH_LANGUAGE("en"),
    APP_CHINESE_LANGUAGE("zh"),
    APP_HINDI_LANGUAGE("hi"),
    APP_SPANISH_LANGUAGE("es"),
    APP_FRENCH_LANGUAGE("fr"),
    APP_GERMAN_LANGUAGE("de"),
    APP_ARABIC_LANGUAGE("ar"),
    APP_PORTUGUESE_LANGUAGE("pt"),
    APP_BENGALI_LANGUAGE("bn"),
    APP_GAME_MODE("game_mode"),
    APP_GAME_MODE_REAL("true"),

    SERVER_ADDRESS_ROULETTE("rGrEJZaBFYhPGuyM7NiJbJw2yXVB9vJHah"),

    NOT_WIN_GUESS_THE_COLOR("213"),
    BET_WIN_GUESS_THE_COLOR("212"),
    LOTTO_WIN_GUESS_THE_COLOR("211"),
    DONATION("99"),
    REFUND("22"),

    BECOME_A_REFERRAL("206"),
    RECOVERY_BECOME_A_REFERRAL("205"),
    TAG_RED_GUESS_THE_COLOR("202"),
    TAG_BLACK_GUESS_THE_NUMBER("201"),
    MAX_BET_GUESS_THE_COLOR("100"),
    MIN_BET_GUESS_THE_COLOR("0.1"),
    MAX_BET_GUESS_THE_NUMBER("36"),
    MIN_BET_GUESS_THE_NUMBER("0"),
    MAX_BET_ROULETTE("100"),
    MIN_BET_ROULETTE("0.1"),
    MAX_BET_SLOT("100"),
    MIN_BET_SLOT("0.1"),

    // tag 214 referral 7483647
    MAX_REFERRALS("666666"),
    REFERRAL_COST("66"),
    REFERRAL_RECOVERY_COST("13"),

    GET_RECOVERY_BECOME_REFERRAL("RECOVERY ORDER REFERRAL SUCCESSFULLY SENT - PLEASE WAIT FOR REPLY FROM THE SERVER"),
    GET_BECOME_REFERRAL("ORDER FOR REFERRAL SUCCESSFULLY SENT - PLEASE WAIT FOR REPLY FROM THE SERVER"),
    PASSWORD_DOES_NOT_MATCH("PASSWORD DOES NOT MATCH PLEASE BE CAREFUL"),
    SEED_DOES_NOT_MATCH("SEED DOES NOT MATCH PLEASE BE CAREFUL"),
    REFERRAL_DOES_NOT_MATCH("REFERRAL DOES NOT MATCH"),

    NET_REAL_POST_URL_ONE("https://s1.ripple.com:51234"),
    NET_REAL_POST_URL_TWO("https://s2.ripple.com:51234"),
    NET_REAL_GET_URL("https://data.ripple.com"),

    WSS_REAL("wss://xrplcluster.com"),

    FAUCET_CLIENT_HTTP_URL_TEST("https://faucet.altnet.rippletest.net"),
    NET_TEST("https://s.altnet.rippletest.net:51234/"),
    WSS_TEST("wss://s.altnet.rippletest.net:51233"),

    ACTIVATION_PAYMENT_SOCKET("10000000"),
    ACTIVATION_PAYMENT("10.000000"),

    WALLET_NOT_ACTIVATED("Wallet not activated."),

    APP_PREFERENCES_BET_INPUT_STYLE("bet_input_style"),
    APP_PREFERENCES_BET_TIMEOUT("bet_response_timeout"),
    APP_PREFERENCES_ANIMATIONS_ENABLED("animations_enabled"),
    APP_PREFERENCES_SEED_PIN_SALT("seed_pin_salt"),
    APP_PREFERENCES_SEED_PIN_ENABLED("seed_pin_enabled")

    ;



    private final String value;



    /** Связывает константу enum с её строковым значением. */
    StringEnum(String value) {
        this.value = value;
    }



    /** Возвращает строковое значение константы. */
    public String getValue() { return value; }
}
