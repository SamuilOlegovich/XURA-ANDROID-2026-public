package com.samuilolegovich.wallet.model.sockets.enums;

import org.junit.Test;

import java.util.List;

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
    // byMessageType — поиск по типу сообщения (возвращает список совпадений)
    // -------------------------------------------------------------------------

    /** Поиск по "serverStatus" возвращает список из одной SERVER */
    @Test
    public void byMessageType_serverStatus_returnsServerOnly() {
        assertEquals(List.of(StreamSubscriptionEnum.SERVER),
                StreamSubscriptionEnum.byMessageType("serverStatus"));
    }

    /** Поиск по "ledgerClosed" возвращает список из одной LEDGER */
    @Test
    public void byMessageType_ledgerClosed_returnsLedgerOnly() {
        assertEquals(List.of(StreamSubscriptionEnum.LEDGER),
                StreamSubscriptionEnum.byMessageType("ledgerClosed"));
    }

    /**
     * Поиск по "transaction": и TRANSACTIONS (глобальный стрим), и ACCOUNT_CHANNELS
     * (подписка на конкретный счёт) отвечают этим типом сообщения, поэтому метод
     * должен вернуть оба варианта в порядке объявления констант enum.
     */
    @Test
    public void byMessageType_transaction_returnsTransactionsAndAccountChannels() {
        assertEquals(List.of(StreamSubscriptionEnum.TRANSACTIONS, StreamSubscriptionEnum.ACCOUNT_CHANNELS),
                StreamSubscriptionEnum.byMessageType("transaction"));
    }

    /** Поиск несуществующего типа возвращает пустой список, а не null */
    @Test
    public void byMessageType_unknownType_returnsEmptyList() {
        assertTrue(StreamSubscriptionEnum.byMessageType("unknown_type").isEmpty());
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
