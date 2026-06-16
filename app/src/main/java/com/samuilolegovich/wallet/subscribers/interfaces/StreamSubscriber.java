package com.samuilolegovich.wallet.subscribers.interfaces;

import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;

import org.json.JSONObject;

/** Подписчик на события потоковой подписки XRPL WebSocket. */
public interface StreamSubscriber {
    /** Вызывается при получении нового сообщения по подписанному потоку событий. */
    void onSubscription(StreamSubscriptionEnum subscription, JSONObject message);
}
