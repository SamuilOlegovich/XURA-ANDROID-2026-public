package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;
import com.samuilolegovich.wallet.subscribers.MyStreamSubscriber;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class RestartSubscriberRun implements Runnable {
    public static volatile boolean FLAG = true;

    private final WalletRepository repository;
    private final int time;



    public RestartSubscriberRun() {
        this.repository = WalletRepository.getInstance();
        this.time = 1000;
    }

    public RestartSubscriberRun(Integer time) {
        this.repository = WalletRepository.getInstance();
        this.time = time;
    }



    @Override
    public void run() {
        FLAG = false;
        restartSocket();
        startSocket();
        restartSubscribeTo();
        FLAG = true;
    }



    private void restartSocket() {
        try {
            repository.restartSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startSocket() {
        boolean connected = false;

        while (!connected) {
            try {
                connected = repository.startSocket();
            } catch (Exception e) {
                e.printStackTrace();
                restartSocket();
            }

            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void restartSubscribeTo() {
        try {
            StreamSubscriber subscriber = new MyStreamSubscriber();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("accounts", List.of(repository.getClassicAddress()));

            Thread.sleep(1000);
            repository.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS), parameters, subscriber);

        } catch (Exception e) {
            FLAG = true;
            AppExecutors.io().execute(new RestartSubscriberRun(10000));
            e.printStackTrace();
        }
    }
}