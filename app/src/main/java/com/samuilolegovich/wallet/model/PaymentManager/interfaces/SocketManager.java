package com.samuilolegovich.wallet.model.PaymentManager.interfaces;


import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.model.sockets.exceptions.InvalidStateException;
import com.samuilolegovich.wallet.model.sockets.interfaces.CommandListener;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.Map;

/** Контракт менеджера WebSocket-соединения с XRPL: подписка/отписка от потоков событий и отправка произвольных команд серверу. */
public interface SocketManager {
    /** Подписывается на указанные потоки событий с дополнительными параметрами запроса, уведомляя переданного подписчика. */
    void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                   Map<String, Object> parameters,
                   StreamSubscriber subscriber) throws InvalidStateException, Exception;
    /** Подписывается на указанные потоки событий без дополнительных параметров. */
    void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                   StreamSubscriber subscriber) throws InvalidStateException, Exception;
    /** Отписывается от указанных потоков событий. */
    void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws InvalidStateException, Exception;
    /** Закрывает соединение после завершения всех текущих операций. */
    void closeWhenComplete();

    /** Отправляет произвольную команду серверу с параметрами и возвращает идентификатор запроса; ответ передаётся слушателю. */
    String sendCommand(String command,
                       Map<String, Object> parameters,
                       CommandListener listener) throws InvalidStateException, Exception;
    /** Отправляет произвольную команду серверу без параметров. */
    String sendCommand(String command,
                       CommandListener listener) throws InvalidStateException, Exception;

    /** Возвращает набор всех активных в данный момент подписок. */
    EnumSet<StreamSubscriptionEnum> getActiveSubscriptions();
}
