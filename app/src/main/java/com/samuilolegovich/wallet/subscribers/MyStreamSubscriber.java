package com.samuilolegovich.wallet.subscribers;

import android.content.Context;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.asyncAndRun.runnable.NotifierRun;
import com.samuilolegovich.asyncAndRun.runnable.UpdateBalanceRun;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MyStreamSubscriber implements StreamSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(MyStreamSubscriber.class);
    Context context;



    @Override
    public void onSubscription(StreamSubscriptionEnum subscription, JSONObject message) {
        LOG.info("subscription returned a {} message", subscription.getMessageType());

        try {
            if (message.has("type") && message.getString("type").equals("transaction")) {
                String messageString = message.toString();

                AppExecutors.io().execute(new UpdateBalanceRun(messageString));
                AppExecutors.io().execute(new NotifierRun(messageString));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
