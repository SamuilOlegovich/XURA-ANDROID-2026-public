package com.samuilolegovich.enums;

import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Тесты для StringEnum — конфигурационные константы приложения.
 * Случайное изменение адреса игрового сервера или тега отправит деньги не туда.
 * Случайное изменение активационного платежа сломает логику проверки баланса.
 */
public class StringEnumTest {

    // -------------------------------------------------------------------------
    // Адреса серверов игр — критические константы
    // -------------------------------------------------------------------------

    /** Адрес сервера игры "Угадай цвет" должен быть неизменным */
    @Test
    public void guessTheColorServer_hasCorrectAddress() {
        assertEquals(
                "rGrEJZaBFYhPGuyM7NiJbJw2yXVB9vJHah",
                StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue()
        );
    }

    /** Адрес сервера игры "Угадай число" должен быть неизменным */
    @Test
    public void guessTheNumberServer_hasCorrectAddress() {
        assertEquals(
                "rfcMxSEz4JP8zj65LU5Nw9hKfpEaD6Ss9",
                StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue()
        );
    }

    // -------------------------------------------------------------------------
    // Теги результатов — логика выплат
    // -------------------------------------------------------------------------

    /** Тег проигрыша равен 213 */
    @Test
    public void notWinTag_equals213() {
        assertEquals("213", StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue());
    }

    /** Тег победы в ставке равен 212 */
    @Test
    public void betWinTag_equals212() {
        assertEquals("212", StringEnum.BET_WIN_GUESS_THE_COLOR.getValue());
    }

    /** Тег победы в лотто равен 211 */
    @Test
    public void lottoWinTag_equals211() {
        assertEquals("211", StringEnum.LOTTO_WIN_GUESS_THE_COLOR.getValue());
    }

    /** Тег ставки на красное равен 202 */
    @Test
    public void redBetTag_equals202() {
        assertEquals("202", StringEnum.TAG_RED_GUESS_THE_COLOR.getValue());
    }

    /** Тег ставки на чёрное равен 201 */
    @Test
    public void blackBetTag_equals201() {
        assertEquals("201", StringEnum.TAG_BLACK_GUESS_THE_NUMBER.getValue());
    }

    /** Тег запроса реферала равен 206 */
    @Test
    public void referralRequestTag_equals206() {
        assertEquals("206", StringEnum.BECOME_A_REFERRAL.getValue());
    }

    // -------------------------------------------------------------------------
    // Активационный платёж — минимальный баланс для работы кошелька
    // -------------------------------------------------------------------------

    /** Строковое значение активационного платежа равно 10.000000 XRP */
    @Test
    public void activationPayment_equals10Xrp() {
        assertEquals("10.000000", StringEnum.ACTIVATION_PAYMENT.getValue());
    }

    /** Числовое значение активационного платежа в дропах равно 10 000 000 */
    @Test
    public void activationPaymentInDrops_equals10Million() {
        assertEquals("10000000", StringEnum.ACTIVATION_PAYMENT_SOCKET.getValue());
    }

    /** Активационный платёж в дропах соответствует активационному платежу в XRP (1 XRP = 1 000 000 drops) */
    @Test
    public void activationPayment_dropsMatchXrpValue() {
        BigDecimal xrp    = new BigDecimal(StringEnum.ACTIVATION_PAYMENT.getValue());
        BigDecimal drops  = new BigDecimal(StringEnum.ACTIVATION_PAYMENT_SOCKET.getValue());
        BigDecimal factor = new BigDecimal("1000000");
        assertEquals(0, xrp.multiply(factor).compareTo(drops));
    }

    // -------------------------------------------------------------------------
    // WebSocket URL
    // -------------------------------------------------------------------------

    /** WSS адрес XRPL кластера должен начинаться с wss:// */
    @Test
    public void wssUrl_startsWithWssScheme() {
        assertTrue(StringEnum.WSS_REAL.getValue().startsWith("wss://"));
    }

    // -------------------------------------------------------------------------
    // Лимиты ставок
    // -------------------------------------------------------------------------

    /** Максимальная ставка "Угадай цвет" равна 100 XRP */
    @Test
    public void maxBetGuessColor_equals100() {
        assertEquals("100", StringEnum.MAX_BET_GUESS_THE_COLOR.getValue());
    }

    /** Минимальная ставка "Угадай цвет" равна 0.1 XRP */
    @Test
    public void minBetGuessColor_equals0_1() {
        assertEquals("0.1", StringEnum.MIN_BET_GUESS_THE_COLOR.getValue());
    }

    /** Максимальная ставка "Угадай число" равна 36 XRP */
    @Test
    public void maxBetGuessNumber_equals36() {
        assertEquals("36", StringEnum.MAX_BET_GUESS_THE_NUMBER.getValue());
    }

    // -------------------------------------------------------------------------
    // setValue — динамическое обновление значений
    // -------------------------------------------------------------------------

    /**
     * setValue позволяет изменить значение во время выполнения.
     * Используется при подключении кошелька для сохранения seed.
     */
    @Test
    public void setValue_updatesEnumValue() {
        String original = StringEnum.SEED_REAL.getValue();
        StringEnum.setValue(StringEnum.SEED_REAL, "test_seed_value");
        assertEquals("test_seed_value", StringEnum.SEED_REAL.getValue());
        StringEnum.setValue(StringEnum.SEED_REAL, original);
    }

    /** Изменение SEED_REAL не влияет на другие константы */
    @Test
    public void setValue_doesNotAffectOtherConstants() {
        String colorServerBefore = StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue();
        StringEnum.setValue(StringEnum.SEED_REAL, "some_seed");
        assertEquals("Адрес сервера не должен измениться",
                colorServerBefore,
                StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue());
        StringEnum.setValue(StringEnum.SEED_REAL, "");
    }

    // -------------------------------------------------------------------------
    // Настройки локализации
    // -------------------------------------------------------------------------

    /** Константа русского языка равна "ru" */
    @Test
    public void russianLanguageConstant_equalsRu() {
        assertEquals("ru", StringEnum.APP_RUSSIAN_LANGUAGE.getValue());
    }

    /** Константа английского языка равна "en" */
    @Test
    public void englishLanguageConstant_equalsEn() {
        assertEquals("en", StringEnum.APP_ENGLISH_LANGUAGE.getValue());
    }

    /** Восстанавливаем SEED_REAL в "" после каждого теста на случай сбоя */
    @After
    public void resetSeedRealAfterTest() {
        StringEnum.setValue(StringEnum.SEED_REAL, "");
    }
}