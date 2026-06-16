package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;
import com.samuilolegovich.wallet.subscribers.MyStreamSubscriber;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Первичная подписка на обновления XRPL-счёта при старте приложения:
 * открывает сокет и подписывается на поток ACCOUNT_CHANNELS для адреса текущего кошелька.
 */
public class SubscriberRun implements Runnable {

    /** Открывает соединение с XRPL-узлом и подписывается на обновления по счёту текущего кошелька. */
    @Override
    public void run() {
        WalletRepository repository = WalletRepository.getInstance();
        repository.startSocket();

        StreamSubscriber subscriber = new MyStreamSubscriber();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accounts", List.of(repository.getClassicAddress()));

        try {
            Thread.sleep(1000);
            repository.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS), parameters, subscriber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}