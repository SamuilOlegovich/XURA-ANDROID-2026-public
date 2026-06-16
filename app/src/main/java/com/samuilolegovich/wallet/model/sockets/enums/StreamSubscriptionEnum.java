package com.samuilolegovich.wallet.model.sockets.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Перечисление типов потоковых подписок XRPL WebSocket (сервер, леджер, транзакции,
 * счёт): связывает имя команды подписки с типом сообщения, которым сервер отвечает
 * на событие. Несколько констант могут отвечать одинаковым типом сообщения — например,
 * {@link #TRANSACTIONS} (глобальный стрим всех транзакций) и {@link #ACCOUNT_CHANNELS}
 * (подписка на конкретный счёт через параметр "accounts") обе присылают сообщения
 * с type="transaction". Поэтому {@link #byMessageType(String)} возвращает список
 * всех подходящих по типу констант, а не одну — какая из них реально активна,
 * решает вызывающий код, сверяясь со своими активными подписками.
 */
public enum StreamSubscriptionEnum {
    SERVER("server", "serverStatus"),
    LEDGER("ledger", "ledgerClosed"),
    TRANSACTIONS("transactions", "transaction"),
    /**
     * Не отдельный XRPL-стрим, а подписка на конкретный счёт через параметр
     * "accounts" (см. SubscriberRun/RestartSubscriberRun); вместе с ней
     * намеренно запрашивается и стрим "ledger". Входящие транзакции по счёту
     * приходят с тем же type="transaction", что и у глобального {@link #TRANSACTIONS}.
     */
    ACCOUNT_CHANNELS("ledger", "transaction"),
    ;

    private final String responseMessageType;
    private final String name;

    private static final Map<String, List<StreamSubscriptionEnum>> lookupByMessageType = new HashMap<>();


    static {
        for (StreamSubscriptionEnum enums : StreamSubscriptionEnum.values()) {
            lookupByMessageType
                    .computeIfAbsent(enums.getMessageType(), k -> new ArrayList<>())
                    .add(enums);
        }
    }


    /** Создаёт константу перечисления с именем подписки и ожидаемым типом ответного сообщения. */
    StreamSubscriptionEnum(String name, String responseMessageType) {
        this.responseMessageType = responseMessageType;
        this.name = name;
    }

    /** Возвращает имя команды подписки, отправляемое на сервер. */
    public String getName() {
        return name;
    }

    /** Возвращает тип сообщения, которым сервер отвечает на события этой подписки. */
    public String getMessageType() {
        return responseMessageType;
    }



    /**
     * Возвращает все константы, чьи сообщения сервера имеют указанный тип
     * (например, и {@link #TRANSACTIONS}, и {@link #ACCOUNT_CHANNELS} отвечают
     * типом "transaction"), либо пустой список, если тип не распознан.
     */
    public static List<StreamSubscriptionEnum> byMessageType(String type) {
        return lookupByMessageType.getOrDefault(type, Collections.emptyList());
    }
}
