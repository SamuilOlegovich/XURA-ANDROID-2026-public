package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.subscribers.MyStreamSubscriber;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class RestartSubscriberRun implements Runnable {
//    private PaymentAndSocketManagerXRPL paymentManager;
//
//    @Override
//    public void run() {
//        paymentManager = PaymentAndSocketManagerXRPL.getInstances();
//        paymentManager.restartSocket();
//        boolean b = false;
//        while (!b) {
//            b = paymentManager.startSocket();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        StreamSubscriber subscriber = new MyStreamSubscriber();
//        Map<String, Object> parameters = new HashMap<>();
//
//        parameters.put("accounts", List.of(paymentManager.getClassicAddress(true)));
//
//
//        try {
//            Thread.sleep(1000);
//            paymentManager.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS), parameters, subscriber);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static volatile boolean FLAG = true;

    private PaymentAndSocketManagerXRPL paymentManager;
    private int time;



    public RestartSubscriberRun() {
        this.time = 1000;
    }

    public RestartSubscriberRun(Integer time) {
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
        paymentManager = PaymentAndSocketManagerXRPL.getInstances();
        try {
            paymentManager.restartSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSocket() {
        boolean b = false;

        while (!b) {
            try {
                b = paymentManager.startSocket();
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
//            paymentManager.restartSubscribeTo();
            StreamSubscriber subscriber = new MyStreamSubscriber();
            Map<String, Object> parameters = new HashMap<>();

            parameters.put("accounts", List.of(paymentManager.getClassicAddress(true)));

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            paymentManager.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS), parameters, subscriber);

        } catch (Exception e) {
            FLAG = true;
            RestartSubscriberRun restartSubscriberRun = new RestartSubscriberRun(10000);
            new Thread(restartSubscriberRun).start();

            e.printStackTrace();
        }
    }
}