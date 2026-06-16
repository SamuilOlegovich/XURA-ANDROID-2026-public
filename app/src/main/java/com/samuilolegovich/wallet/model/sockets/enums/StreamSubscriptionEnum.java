package com.samuilolegovich.wallet.model.sockets.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Перечисление типов потоковых подписок XRPL WebSocket (сервер, леджер, транзакции,
 * каналы счёта): связывает имя команды подписки с типом сообщения, которым
 * сервер отвечает на событие, и предоставляет быстрый поиск константы по любому
 * из этих двух значений.
 */
public enum StreamSubscriptionEnum {
    SERVER("server", "serverStatus"),
    LEDGER("ledger", "ledgerClosed"),
    TRANSACTIONS("transactions", "transaction"),
    ACCOUNT_CHANNELS("ledger", "transaction"),
    ;

    private final String responseMessageType;
    private final String name;

    private static final Map<String, StreamSubscriptionEnum> lookupByMessageType = new HashMap<>();
    private static final Map<String, StreamSubscriptionEnum> lookupByName = new HashMap<>();


    static {
        for (StreamSubscriptionEnum enums : StreamSubscriptionEnum.values()) {
            lookupByMessageType.put(enums.getMessageType(), enums);
            lookupByName.put(enums.getName(), enums);
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



    /** Находит константу перечисления по типу входящего сообщения от сервера. */
    public static StreamSubscriptionEnum byMessageType(String type) {
        return lookupByMessageType.get(type);
    }

    /** Находит константу перечисления по имени подписки. */
    public static StreamSubscriptionEnum byName(String name) {
        return lookupByName.get(name);
    }
}
