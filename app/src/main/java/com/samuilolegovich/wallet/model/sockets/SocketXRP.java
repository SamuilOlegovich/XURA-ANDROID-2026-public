package com.samuilolegovich.wallet.model.sockets;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.async.runnable.RestartSubscriberRun;
import com.samuilolegovich.wallet.model.paymentmanager.PaymentAndSocketManagerXRPL;
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
 * Клиент WebSocket для реестра XRP (XRPL).
 * Подпишитесь на потоки событий с помощью {@link #subscribe} или отправьте
 * команду и дождитесь ответа с помощью {@link #sendCommand}.
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



    /** Создаёт WebSocket-клиент для указанного URI сервера (соединение не открывается автоматически). */
    public SocketXRP(URI serverUri) {
        super(serverUri);
    }

    /** Создаёт WebSocket-клиент, разбирая переданную строку как URI сервера. */
    public SocketXRP(String serverUri) throws URISyntaxException {
        this(new URI(serverUri));
    }



    /** Отправляет команду без дополнительных параметров и регистрирует слушателя для ответа. */
    public String sendCommand(String command,
                              CommandListener listener) throws Exception {
        return sendCommand(command, null, listener);
    }

    /** Формирует и отправляет JSON-команду с уникальным id и параметрами, регистрируя слушателя, который будет вызван при получении ответа с этим id. */
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
        }

        send(request.toString());
        commandListeners.put(id, listener);
        return id;
    }

    /** Подписывается на указанные потоки событий без дополнительных параметров, сохраняя подписчика для каждого потока. */
    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          StreamSubscriber subscriber) throws Exception {
        checkOpen();
        LOG.info("Subscribing to: {}", streams);
        send(composeSubscribe(CMD_SUBSCRIBE, streams));
        streams.forEach(t -> activeSubscriptions.put(t, subscriber));
    }

    /** Подписывается на указанные потоки событий с дополнительными параметрами запроса, сохраняя подписчика для каждого потока. */
    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          Map<String, Object> parameters,
                          StreamSubscriber subscriber) throws Exception {
        checkOpen();
        LOG.info("Subscribing to: {}", streams);
        send(composeSubscribe(CMD_SUBSCRIBE, parameters, streams));
        streams.forEach(t -> activeSubscriptions.put(t, subscriber));
    }



    /** Отписывается от указанных потоков событий и удаляет их из активных подписок. */
    public void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        checkOpen();
        LOG.info("Unsubscribing from: {}", streams);
        send(composeSubscribe(CMD_UNSUBSCRIBE, streams));
        streams.forEach(activeSubscriptions::remove);
    }

    /** Возвращает набор всех потоков событий, на которые сейчас есть активная подписка. */
    public EnumSet<StreamSubscriptionEnum> getActiveSubscriptions() {
        return activeSubscriptions.isEmpty()
                ? EnumSet.noneOf(StreamSubscriptionEnum.class)
                : EnumSet.copyOf(activeSubscriptions.keySet());
    }

    /** Помечает, что соединение нужно закрыть, как только не останется активных подписок и ожидающих ответа команд. */
    public void closeWhenComplete() {
        closeWhenComplete = true;
    }

    /** Логирует отправляемое сообщение и делегирует его базовой реализации WebSocketClient. */
    @Override
    public void send(String message) {
        LOG.info("Sending message: {}", message);
        super.send(message);
    }

    /** Логирует заголовки рукопожатия и факт успешного открытия соединения. */
    @Override
    public void onOpen(ServerHandshake handshake) {
        handshake.iterateHttpFields().forEachRemaining(LOG::debug);
        LOG.info("XRP ledger client opened");
    }

    /** Разбирает входящее сообщение: для каждой константы подписки, чей тип сообщения совпадает с пришедшим, проверяет, есть ли на неё активная подписка, и при наличии вызывает подписчика (так события с неоднозначным типом, например "transaction", доходят до того, кто на них реально подписан); ответ команды передаёт зарегистрированному слушателю; закрывает соединение, если оно было помечено для закрытия и больше нет активных подписок/ожидающих команд. */
    @Override
    public void onMessage(String message) {
        long start = System.currentTimeMillis();
        LOG.info("XRPL client received a message:\n{}", message);
        try {
            JSONObject json = new JSONObject(message);

            if (json.has(ATTRIBUTE_TYPE)) {
                for (StreamSubscriptionEnum subscription
                        : StreamSubscriptionEnum.byMessageType(json.getString(ATTRIBUTE_TYPE))) {
                    StreamSubscriber subscriber = activeSubscriptions.get(subscription);
                    if (subscriber != null) {
                        subscriber.onSubscription(subscription, json);
                    }
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

    /** При закрытии соединения очищает все подписки и слушателей команд и пытается перезапустить сокет (если перезапуск разрешён флагом). */
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

    /** При ошибке сокета логирует исключение и очищает все подписки и слушателей команд. */
    @Override
    public void onError(Exception exception) {
        LOG.info("********************** XRP onError **********************");
        LOG.error("XRP ledger client error {}", exception);
        // Очистить activeSubscriptions и commandListeners?
        // Всегда ли за onError следует onClose?
        activeSubscriptions.clear();
        commandListeners.clear();
    }


    /** Формирует JSON-команду подписки/отписки со списком имён потоков, без дополнительных параметров. */
    private String composeSubscribe(String command, EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        JSONObject request = new JSONObject();
        request.put(COMMAND, command);
        request.put(STREAMS, streams.stream()
                .map(StreamSubscriptionEnum::getName)
                .collect(Collectors.toList()));
        return request.toString();
    }


    /** Формирует JSON-команду подписки/отписки со списком имён потоков и дополнительными параметрами запроса. */
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
        }
        return request.toString().replaceAll("\"\\[", "[\"").replaceAll("]\"", "\"]");
        // тут я вставил кастыль - разобраться и исправить
        //  Sending message: {"command":"subscribe","streams":"[ledger]","accounts":"[rsG3xqRQSnxfYfF9foHfy7fNEZZctDc3Dx]"}
        //  {"streams":["ledger"],"accounts":["rnxYi2nuJiS1AnYmXN75JHJr8MWQZLRPSx"],"command":"subscribe"}
    }

    /** Проверяет, что соединение открыто, иначе выбрасывает {@link InvalidStateException}. */
    private void checkOpen() throws InvalidStateException {
        if (!isOpen()) {
            // тут сделать востановления сокета и вывод информации на экран что он не работет если он отвалился
            throw new InvalidStateException();
        }
    }

    /** Запускает фоновую задачу переподключения сокета с задержкой, зависящей от кода закрытия соединения (если перезапуск разрешён флагом {@code RestartSubscriberRun.FLAG}). */
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
