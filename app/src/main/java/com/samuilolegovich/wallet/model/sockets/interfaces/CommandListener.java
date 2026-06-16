package com.samuilolegovich.wallet.model.sockets.interfaces;

import org.json.JSONObject;

/** Слушатель ответа на отправленную через WebSocket команду XRPL. */
public interface CommandListener {
    /** Вызывается при получении ответа сервера на отправленную команду. */
    void onResponse(JSONObject response);
}
