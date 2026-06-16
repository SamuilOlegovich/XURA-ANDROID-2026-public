package com.samuilolegovich.wallet.subscribers;

import android.content.Context;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.async.runnable.NotifierRun;
import com.samuilolegovich.async.runnable.UpdateBalanceRun;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Подписчик на поток транзакций XRPL WebSocket: при получении сообщения о новой
 * транзакции запускает на фоне обновление баланса кошелька и обработку
 * (оповещение игрока о выигрыше/проигрыше через {@link NotifierRun}).
 */
public class StreamSubscriberImpl implements StreamSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(StreamSubscriberImpl.class);
    Context context;



    /** Если входящее сообщение — транзакция, запускает обновление баланса и обработчик уведомлений на фоновых потоках. */
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
