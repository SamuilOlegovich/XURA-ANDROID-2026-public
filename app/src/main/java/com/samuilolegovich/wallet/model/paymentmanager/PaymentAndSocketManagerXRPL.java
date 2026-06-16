package com.samuilolegovich.wallet.model.paymentmanager;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.model.paymentmanager.interfaces.PaymentManager;
import com.samuilolegovich.wallet.model.paymentmanager.interfaces.SocketManager;
import com.samuilolegovich.wallet.model.sockets.SocketXRP;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.model.sockets.interfaces.CommandListener;
import com.samuilolegovich.wallet.model.wallets.WalletXRP;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import org.java_websocket.client.WebSocketClient;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;



/**
 * Синглтон, объединяющий менеджер платежей и менеджер WebSocket-соединения
 * для кошелька XRPL: делегирует операции с платежами и ключами в
 * {@link WalletXRP}, а операции с потоковыми подписками и командами —
 * в {@link SocketXRP}.
 */
public class PaymentAndSocketManagerXRPL implements PaymentManager, SocketManager {
    private static PaymentAndSocketManagerXRPL instance = null;
    private SocketXRP socket;
    private WalletXRP wallet;



    /** Возвращает единственный экземпляр менеджера, создавая его при первом обращении. */
    public static synchronized PaymentAndSocketManagerXRPL getInstances() {
        if (instance == null) {
            Locale.setDefault(Locale.ENGLISH);
            instance = new PaymentAndSocketManagerXRPL();
        }
        return instance;
    }



    /** Создаёт новый сокет и кошелёк при первой инициализации синглтона. */
    private PaymentAndSocketManagerXRPL() {
            this.socket = (SocketXRP) createNewSocket(true);
            this.wallet = new WalletXRP();
    }

    /** Альтернативный конструктор с тем же поведением, что и беспараметрический. */
    private PaymentAndSocketManagerXRPL(boolean b) {
        this.socket = (SocketXRP) createNewSocket(true);
        this.wallet = new WalletXRP();
    }

    /** Пересоздаёт WebSocket-соединение (например, после смены сети testnet/mainnet). */
    public void restartSocket() {
        this.socket = (SocketXRP) createNewSocket(true);
    }


    // подумать, может можно сделать более элегантную проверку на null — чтобы не передавать его дальше
    /** Создаёт новый WebSocket-клиент, подключённый к текущему WSS-адресу сети; при ошибке URI возвращает null. */
    private WebSocketClient createNewSocket(boolean b) {
        try {
            return new SocketXRP(com.samuilolegovich.config.NetworkConfig.getWssUrl());
        } catch (URISyntaxException e) { e.printStackTrace(); }
        return null;
    }


    // Payment *********************************************************************************************************

    /** Отправляет платёж с мемо через кошелёк, если запрошен реальный режим и кошелёк инициализирован. */
    @Override
    public boolean sendPayment(String address, String memo, BigDecimal numberOfXRP, boolean isReal) {
        if (isReal && wallet != null) { return wallet.sendPaymentToAddressXRP(address, memo, numberOfXRP); }
        return false;
    }

    /** Отправляет платёж с destination tag через кошелёк, если запрошен реальный режим и кошелёк инициализирован. */
    @Override
    public boolean sendPayment(String address, Integer tag, BigDecimal numberOfXRP, boolean isReal) {
        if (isReal && wallet != null) { return wallet.sendPaymentToAddressXRP(address, tag, numberOfXRP); }
        return false;
    }

    /** Отправляет обычный платёж через кошелёк, если запрошен реальный режим и кошелёк инициализирован. */
    @Override
    public boolean sendPayment(String address, BigDecimal numberOfXRP, boolean isReal) {
        if (isReal && wallet != null) { return wallet.sendPaymentToAddressXRP(address, numberOfXRP); }
        return false;
    }

    /** Подключает существующий кошелёк по сид-фразе (восстановление без создания нового кошелька). */
    @Override
    public Map<String, String> connectAnExistingWallet(String seed, boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.restoreWallet(seed);
        }
        return new HashMap<>();
    }

    /** Создаёт новый кошелёк через {@link WalletXRP}, если запрошен реальный режим. */
    @Override
    public Map<String, String> createNewWallet(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.createNewWallet();
        }
        return new HashMap<>();
    }

    /** Восстанавливает кошелёк из сид-фразы через {@link WalletXRP}, если запрошен реальный режим. */
    @Override
    public Map<String, String> restoreWallet(String seed, boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.restoreWallet(seed);
        }
        return new HashMap<>();
    }

    /** Переключение активного кошелька; в текущей реализации не выполняет действий. */
    @Override
    public void setterWallet(boolean isReal) {
        if (isReal && wallet != null) {

        }
    }

    /** Возвращает классический адрес кошелька либо строку "кошелёк не активирован". */
    @Override
    public String getClassicAddress(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.classicAddress().toString();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    /** Возвращает приватный ключ кошелька либо строку "кошелёк не активирован". */
    @Override
    public String getPrivateKey(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.privateKey().get();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    /** Возвращает X-адрес кошелька либо строку "кошелёк не активирован". */
    @Override
    public String getXAddress(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.xAddress().toString();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    /** Возвращает публичный ключ кошелька либо строку "кошелёк не активирован". */
    @Override
    public String getPublicKey(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.publicKey();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    /** Возвращает сид-фразу кошелька либо строку "кошелёк не активирован". */
    @Override
    public String getSeed(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.getSeed();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    /** Сообщает, является ли кошелёк тестовым. */
    @Override
    public boolean isTest(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.isTest();
        }
        return false;
    }

    /** Возвращает доступный для трат баланс: полный баланс за вычетом суммы резерва активации счёта (0, если баланс не превышает резерв). */
    @Override
    public BigDecimal getBalance(boolean isReal) {
        BigDecimal allBalance = getAllBalance(isReal);
        BigDecimal activationPayment = new BigDecimal(StringEnum.ACTIVATION_PAYMENT.getValue());
        int compareTo = allBalance.compareTo(activationPayment);
        if (compareTo <= 0) { return new BigDecimal("0.000000"); }
        return allBalance.subtract(activationPayment);
    }

    /** Возвращает полный баланс кошелька без вычета резерва. */
    @Override
    public BigDecimal getAllBalance(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.getBalance();
        }
        return BigDecimal.ZERO;
    }



    // Socket **********************************************************************************************************

    /** Отправляет команду без параметров через сокет, если он инициализирован; иначе возвращает строку "кошелёк не активирован". */
    @Override
    public String sendCommand(String command,
                              CommandListener listener) throws Exception {
        if (socket != null) {
            return socket.sendCommand(command, null, listener);
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    /** Отправляет команду с параметрами через сокет, если он инициализирован; иначе возвращает строку "кошелёк не активирован". */
    @Override
    public String sendCommand(String command,
                              Map<String, Object> parameters,
                              CommandListener listener) throws Exception {
        if (socket != null) {
            return socket.sendCommand(command, parameters, listener);
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    /** Подписывается на потоки событий без дополнительных параметров, если сокет инициализирован. */
    @Override
    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          StreamSubscriber subscriber) throws Exception {
        if (socket != null) {
            socket.subscribe(streams, subscriber);
        }
    }

    /** Подписывается на потоки событий с дополнительными параметрами запроса, если сокет инициализирован. */
    @Override
    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          Map<String, Object> parameters,
                          StreamSubscriber subscriber) throws Exception {
        if (socket != null) {
            socket.subscribe(streams, parameters, subscriber);
        }
    }

    /** Отписывается от указанных потоков событий через сокет. */
    @Override
    public void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        socket.unsubscribe(streams);
    }

    /** Возвращает набор активных подписок сокета, либо null, если сокет не инициализирован. */
    @Override
    public EnumSet<StreamSubscriptionEnum> getActiveSubscriptions() {
        if (socket != null) {
            return socket.getActiveSubscriptions();
        }
        return null;
    }

    /** Помечает сокет на закрытие после завершения всех активных подписок и команд. */
    @Override
    public void closeWhenComplete() {
        socket.closeWhenComplete();
    }

    /** Настраивает SSL (доверие всем сертификатам для testnet либо пиннинг для mainnet) и блокирующе открывает соединение сокета с таймаутом 3 секунды. */
    public boolean startSocket() {
        if (socket != null) {
            try {
                javax.net.ssl.SSLSocketFactory sslSocketFactory =
                        com.samuilolegovich.config.NetworkConfig.IS_TESTNET
                                ? com.samuilolegovich.wallet.client.SslUtil.trustAllSocketFactory()
                                : com.samuilolegovich.wallet.client.SslUtil.pinnedSocketFactory(
                                        com.samuilolegovich.wallet.client.SslUtil.WSS_MAINNET_PINS);
                socket.setSocketFactory(sslSocketFactory);
                return socket.connectBlocking(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /** Закрывает соединение сокета, если оно было инициализировано. */
    public void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
