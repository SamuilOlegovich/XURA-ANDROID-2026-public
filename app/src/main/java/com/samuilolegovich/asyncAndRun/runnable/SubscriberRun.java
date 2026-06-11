package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;
import com.samuilolegovich.wallet.subscribers.MyStreamSubscriber;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class SubscriberRun implements Runnable {

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