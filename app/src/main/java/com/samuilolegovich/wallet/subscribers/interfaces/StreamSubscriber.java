package com.samuilolegovich.wallet.subscribers.interfaces;

import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;

import org.json.JSONObject;

public interface StreamSubscriber {
    void onSubscription(StreamSubscriptionEnum subscription, JSONObject message);
}
