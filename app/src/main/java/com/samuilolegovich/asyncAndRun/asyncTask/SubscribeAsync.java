package com.samuilolegovich.asyncAndRun.asyncTask;

import android.os.AsyncTask;

import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.subscribers.MyStreamSubscriber;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscribeAsync extends AsyncTask<String, Void, Boolean> {
    private PaymentAndSocketManagerXRPL paymentManager;
    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Boolean doInBackground(String... arg) {
        Map<String, Object> parameters = new HashMap<>();
        StreamSubscriber subscriber = new MyStreamSubscriber();
        paymentManager = PaymentAndSocketManagerXRPL.getInstances();
        String a = paymentManager.getClassicAddress(true);
        parameters.put("accounts", List.of(a));

        try {
            Thread.sleep(5000);
            paymentManager.subscribe(EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS), parameters, subscriber);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean map) {
    }
}
