package com.samuilolegovich.wallet.model.sockets.interfaces;

import org.json.JSONObject;

public interface CommandListener {
    void onResponse(JSONObject response);
}
