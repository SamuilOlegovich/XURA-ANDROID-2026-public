package com.samuilolegovich.wallet.model.sockets;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.asyncAndRun.runnable.RestartSubscriberRun;
import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.model.sockets.exceptions.InvalidStateException;
import com.samuilolegovich.wallet.model.sockets.interfaces.CommandListener;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



/**
 * A WebSocket client for the XRP ledger.
 * Subscribe to streams with subscribe() or send commands and wait for
 * a response with sendCommand().
 * Клиент WebSocket для реестра XRP.
 * Подпишитесь на потоки с помощью subscribe() или отправьте команды и дождитесь ответа с помощью sendCommand().
 */
public class SocketXRP extends WebSocketClient {
    private final Map<StreamSubscriptionEnum, StreamSubscriber> activeSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, CommandListener> commandListeners = new ConcurrentHashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(SocketXRP.class);


    private static final String CMD_UNSUBSCRIBE = "unsubscribe";
    private static final String CMD_SUBSCRIBE = "subscribe";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_ID = "id";
    private static final String COMMAND = "command";
    private static final String STREAMS = "streams";

    private volatile boolean closeWhenComplete;
    private PaymentAndSocketManagerXRPL paymentAndSocketManagerXRPL;



    public SocketXRP(URI serverUri) {
        super(serverUri);
    }

    public SocketXRP(String serverUri) throws URISyntaxException {
        this(new URI(serverUri));
    }



    public String sendCommand(String command,
                              CommandListener listener) throws Exception {
        return sendCommand(command, null, listener);
    }

    public String sendCommand(String command,
                              Map<String, Object> parameters,
                              CommandListener listener) throws Exception {
        checkOpen();
        String id = UUID.randomUUID().toString();

        JSONObject request = new JSONObject();
        request.put(COMMAND, command);
        request.put("id", id);

        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((paramString, paramObject) -> {
                try {
                    request.put(paramString, paramObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
//            parameters.forEach(request::put);
        }

        send(request.toString());
        commandListeners.put(id, listener);
        return id;
    }

    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          StreamSubscriber subscriber) throws Exception {
        checkOpen();
        LOG.info("Subscribing to: {}", streams);
        send(composeSubscribe(CMD_SUBSCRIBE, streams));
        streams.forEach(t -> activeSubscriptions.put(t, subscriber));
    }

    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          Map<String, Object> parameters,
                          StreamSubscriber subscriber) throws Exception {
        checkOpen();
        LOG.info("Subscribing to: {}", streams);
        send(composeSubscribe(CMD_SUBSCRIBE, parameters, streams));
        streams.forEach(t -> activeSubscriptions.put(t, subscriber));
    }



    public void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        checkOpen();
        LOG.info("Unsubscribing from: {}", streams);
        send(composeSubscribe(CMD_UNSUBSCRIBE, streams));
        streams.forEach(activeSubscriptions::remove);
    }

    public EnumSet<StreamSubscriptionEnum> getActiveSubscriptions() {
        return activeSubscriptions.isEmpty()
                ? EnumSet.noneOf(StreamSubscriptionEnum.class)
                : EnumSet.copyOf(activeSubscriptions.keySet());
    }

    public void closeWhenComplete() {
        closeWhenComplete = true;
    }

    @Override
    public void send(String message) {
        LOG.info("Sending message: {}", message);
        super.send(message);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        handshake.iterateHttpFields().forEachRemaining(LOG::debug);
        LOG.info("XRP ledger client opened");
    }

    @Override
    public void onMessage(String message) {
        long start = System.currentTimeMillis();
        LOG.info("XRPL client received a message:\n{}", message);
        try {
            JSONObject json = new JSONObject(message);

            if (json.has(ATTRIBUTE_TYPE)
                    && (StreamSubscriptionEnum.byMessageType(json.getString(ATTRIBUTE_TYPE))
                    != null)) {

                StreamSubscriptionEnum subscription = StreamSubscriptionEnum
                        .byMessageType(json.getString(ATTRIBUTE_TYPE));
                StreamSubscriber subscriber = activeSubscriptions.get(subscription);

                if (subscriber != null) {
                    subscriber.onSubscription(subscription, json);
                }
            } else if (json.has(ATTRIBUTE_ID)
                    && commandListeners.get(json.getString(ATTRIBUTE_ID)) != null) {
                commandListeners.get(json.getString(ATTRIBUTE_ID)).onResponse(json);
                commandListeners.remove(json.getString(ATTRIBUTE_ID));
            }

            if (closeWhenComplete && commandListeners.isEmpty() && activeSubscriptions.isEmpty()) {
                close();
            }

            LOG.info("Ledger message processed in {}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOG.info("********************** XRP onClose **********************");
        LOG.info("XRP ledger client closed (code {}), reason given: {}", code, reason);
        // XRP ledger client closed (code -1), reason given: Unable to resolve host "xrplcluster.com": No address associated with hostname
        // XRP ledger client closed (code 1006), reason given: The connection was closed because the other endpoint did not respond with a pong in time. For more information check: https://github.com/TooTallNate/Java-WebSocket/wiki/Lost-connection-detection

        activeSubscriptions.clear();
        commandListeners.clear();
        restartSocket(code);
    }

    @Override
    public void onError(Exception exception) {
        LOG.info("********************** XRP onError **********************");
        LOG.error("XRP ledger client error {}", exception);
        // Очистить activeSubscriptions и commandListeners?
        // Всегда ли за onError следует onClose?
        activeSubscriptions.clear();
        commandListeners.clear();
    }


    private String composeSubscribe(String command, EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        JSONObject request = new JSONObject();
        request.put(COMMAND, command);
        request.put(STREAMS, streams.stream()
                .map(StreamSubscriptionEnum::getName)
                .collect(Collectors.toList()));
        return request.toString();
    }


    // ************************** Перепроверить этот метод *****************************************
    private String composeSubscribe(String command,
                                    Map<String, Object> parameters,
                                    EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        JSONObject request = new JSONObject();
        request.put(COMMAND, command);
        request.put(STREAMS, streams.stream()
                .map(StreamSubscriptionEnum::getName)
                .collect(Collectors.toList()));

        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((paramString, paramObject) -> {
                try {
                    request.put(paramString, paramObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
//                    parameters.forEach(request::put);
        }
        return request.toString().replaceAll("\"\\[", "[\"").replaceAll("]\"", "\"]");
        // тут я вставил кастыль - разобраться и исправить
        //  Sending message: {"command":"subscribe","streams":"[ledger]","accounts":"[rsG3xqRQSnxfYfF9foHfy7fNEZZctDc3Dx]"}
        //  {"streams":["ledger"],"accounts":["rnxYi2nuJiS1AnYmXN75JHJr8MWQZLRPSx"],"command":"subscribe"}
    }

    private void checkOpen() throws InvalidStateException {
        if (!isOpen()) {
            // тут сделать востановления сокета и вывод информации на экран что он не работет если он отвалился
            throw new InvalidStateException();
        }
    }

    private void restartSocket(int code) {
        if (RestartSubscriberRun.FLAG) {
            RestartSubscriberRun restartSubscriberRun;
            if (code == 1006) {
                restartSubscriberRun = new RestartSubscriberRun(5000);
            } else if (code == -1) {
                restartSubscriberRun = new RestartSubscriberRun(20000);
            } else {
                restartSubscriberRun = new RestartSubscriberRun();
            }
            AppExecutors.io().execute(restartSubscriberRun);
        }
    }
}
