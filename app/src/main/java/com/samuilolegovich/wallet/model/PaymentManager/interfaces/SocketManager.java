package com.samuilolegovich.wallet.model.PaymentManager.interfaces;


import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.model.sockets.exceptions.InvalidStateException;
import com.samuilolegovich.wallet.model.sockets.interfaces.CommandListener;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.Map;

public interface SocketManager {
    void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                   Map<String, Object> parameters,
                   StreamSubscriber subscriber) throws InvalidStateException, Exception;
    void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                   StreamSubscriber subscriber) throws InvalidStateException, Exception;
    void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws InvalidStateException, Exception;
    void closeWhenComplete();

    String sendCommand(String command,
                       Map<String, Object> parameters,
                       CommandListener listener) throws InvalidStateException, Exception;
    String sendCommand(String command,
                       CommandListener listener) throws InvalidStateException, Exception;

    EnumSet<StreamSubscriptionEnum> getActiveSubscriptions();
}
