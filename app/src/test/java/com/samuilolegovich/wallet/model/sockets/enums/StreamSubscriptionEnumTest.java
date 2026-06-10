package com.samuilolegovich.wallet.model.sockets.enums;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Тесты для StreamSubscriptionEnum — перечисление типов подписок на XRP Ledger WebSocket.
 * Ошибка здесь приводит к потере событий о транзакциях и некорректному обновлению баланса.
 */
public class StreamSubscriptionEnumTest {

    // -------------------------------------------------------------------------
    // Корректность значений полей
    // -------------------------------------------------------------------------

    /** SERVER подписка имеет правильные name и messageType */
    @Test
    public void server_hasCorrectNameAndMessageType() {
        assertEquals("server", StreamSubscriptionEnum.SERVER.getName());
        assertEquals("serverStatus", StreamSubscriptionEnum.SERVER.getMessageType());
    }

    /** LEDGER подписка имеет правильные name и messageType */
    @Test
    public void ledger_hasCorrectNameAndMessageType() {
        assertEquals("ledger", StreamSubscriptionEnum.LEDGER.getName());
        assertEquals("ledgerClosed", StreamSubscriptionEnum.LEDGER.getMessageType());
    }

    /** TRANSACTIONS подписка имеет правильные name и messageType */
    @Test
    public void transactions_hasCorrectNameAndMessageType() {
        assertEquals("transactions", StreamSubscriptionEnum.TRANSACTIONS.getName());
        assertEquals("transaction", StreamSubscriptionEnum.TRANSACTIONS.getMessageType());
    }

    /** ACCOUNT_CHANNELS подписка имеет правильные name и messageType */
    @Test
    public void accountChannels_hasCorrectNameAndMessageType() {
        assertEquals("ledger", StreamSubscriptionEnum.ACCOUNT_CHANNELS.getName());
        assertEquals("transaction", StreamSubscriptionEnum.ACCOUNT_CHANNELS.getMessageType());
    }

    // -------------------------------------------------------------------------
    // byMessageType — поиск по типу сообщения
    // -------------------------------------------------------------------------

    /** Поиск по "serverStatus" возвращает SERVER */
    @Test
    public void byMessageType_serverStatus_returnsServer() {
        assertEquals(StreamSubscriptionEnum.SERVER, StreamSubscriptionEnum.byMessageType("serverStatus"));
    }

    /** Поиск по "ledgerClosed" возвращает LEDGER */
    @Test
    public void byMessageType_ledgerClosed_returnsLedger() {
        assertEquals(StreamSubscriptionEnum.LEDGER, StreamSubscriptionEnum.byMessageType("ledgerClosed"));
    }

    /**
     * Поиск по "transaction": TRANSACTIONS и ACCOUNT_CHANNELS оба имеют этот messageType.
     * Из-за порядка инициализации enum последний в списке (ACCOUNT_CHANNELS) перезаписывает первый.
     * Этот тест фиксирует текущее поведение — если оно изменится, тест сразу сообщит об этом.
     */
    @Test
    public void byMessageType_transaction_returnsAccountChannels() {
        assertEquals(StreamSubscriptionEnum.ACCOUNT_CHANNELS,
                StreamSubscriptionEnum.byMessageType("transaction"));
    }

    /** Поиск несуществующего типа возвращает null */
    @Test
    public void byMessageType_unknownType_returnsNull() {
        assertNull(StreamSubscriptionEnum.byMessageType("unknown_type"));
    }

    // -------------------------------------------------------------------------
    // byName — поиск по имени
    // -------------------------------------------------------------------------

    /** Поиск по "server" возвращает SERVER */
    @Test
    public void byName_server_returnsServer() {
        assertEquals(StreamSubscriptionEnum.SERVER, StreamSubscriptionEnum.byName("server"));
    }

    /** Поиск по "transactions" возвращает TRANSACTIONS */
    @Test
    public void byName_transactions_returnsTransactions() {
        assertEquals(StreamSubscriptionEnum.TRANSACTIONS, StreamSubscriptionEnum.byName("transactions"));
    }

    /**
     * Поиск по "ledger": LEDGER и ACCOUNT_CHANNELS оба имеют это имя.
     * Последний в enum (ACCOUNT_CHANNELS) перезаписывает LEDGER в lookup-таблице.
     */
    @Test
    public void byName_ledger_returnsAccountChannels() {
        assertEquals(StreamSubscriptionEnum.ACCOUNT_CHANNELS, StreamSubscriptionEnum.byName("ledger"));
    }

    /** Поиск несуществующего имени возвращает null */
    @Test
    public void byName_unknownName_returnsNull() {
        assertNull(StreamSubscriptionEnum.byName("no_such_stream"));
    }

    // -------------------------------------------------------------------------
    // Полнота перечисления
    // -------------------------------------------------------------------------

    /** В enum должно быть ровно 4 значения */
    @Test
    public void enumValues_containsExactlyFourEntries() {
        assertEquals(4, StreamSubscriptionEnum.values().length);
    }
}