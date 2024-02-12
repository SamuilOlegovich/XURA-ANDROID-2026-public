package com.samuilolegovich.enums;



public enum StringEnum {
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
    APP_GAME_MODE("game_mode"),
    APP_GAME_MODE_REAL("true"),

    SERVER_ADDRESS_GUESS_THE_COLOR("rGrEJZaBFYhPGuyM7NiJbJw2yXVB9vJHah"),
    SERVER_ADDRESS_GUESS_THE_NUMBER("rfcMxSEz4JP8zj65LU5Nw9hKfpEaD6Ss9"),
    SERVER_ADDRESS_BECOME_REFERRAL("rGrEJZaBFYhPGuyM7NiJbJw2yXVB9vJHah"),

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

    // tag 214 referral 7483647
    MAX_REFERRALS("666666"),
    REFERRAL_COST("66"),
    REFERRAL_RECOVERY_COST("13"),

    GET_RECOVERY_BECOME_REFERRAL("RECOVERY ORDER REFERRAL SUCCESSFULLY SENT - PLEASE WAIT FOR REPLY FROM THE SERVER"),
    GET_BECOME_REFERRAL("ORDER FOR REFERRAL SUCCESSFULLY SENT - PLEASE WAIT FOR REPLY FROM THE SERVER"),
    PASSWORD_DOES_NOT_MATCH("PASSWORD DOES NOT MATCH PLEASE BE CAREFUL"),
    SEED_DOES_NOT_MATCH("SEED DOES NOT MATCH PLEASE BE CAREFUL"),
    REFERRAL_DOES_NOT_MATCH("REFERRAL DOES NOT MATCH"),

    SEED_REAL(""),

    NET_REAL_POST_URL_ONE("https://s1.ripple.com:51234"),
    NET_REAL_POST_URL_TWO("https://s2.ripple.com:51234"),
    NET_REAL_GET_URL("https://data.ripple.com"),

    WSS_REAL("wss://xrplcluster.com"),
//    WSS_REAL_1("wss://s1.ripple.com"),
//    WSS_REAL_2("wss://s2.ripple.com"),
//    WSS_REAL_3("wss://xrpl.link"),

    FAUCET_CLIENT_HTTP_URL_TEST("https://faucet.altnet.rippletest.net"),
//    ADDRESS_FOR_SEND_TEST("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
//    NET_TEST("https://s.altnet.rippletest.net:51234/"),
//    WSS_TEST("wss://s.altnet.rippletest.net:51233"),
//    SEED_TEST("sn3nxiW7v8KXzPzAqzyHXbSSKNuN9"),
//    WSS_PORT_TEST("51233"),

    ACTIVATION_PAYMENT_SOCKET("10000000"),
    ACTIVATION_PAYMENT("10.000000"),

    WALLET_NOT_ACTIVATED("Wallet not activated.")

    ;



    private String value;



    StringEnum(String value) {
        this.value = value;
    }



    private void setValue(String value) {
        this.value = value;
    }

    public String getValue() { return value; }

    public static void setValue(StringEnum enums, String value) {
        for (StringEnum e : StringEnum.values()) {
            if (e.equals(enums)) {
                e.setValue(value);
                break;
            }
        }
    }
}
