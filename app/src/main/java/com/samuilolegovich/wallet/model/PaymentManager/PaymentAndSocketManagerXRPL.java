package com.samuilolegovich.wallet.model.PaymentManager;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.enums.BooleanEnum;
import com.samuilolegovich.wallet.model.PaymentManager.interfaces.PaymentManager;
import com.samuilolegovich.wallet.model.PaymentManager.interfaces.Presets;
import com.samuilolegovich.wallet.model.PaymentManager.interfaces.SocketManager;
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



public class PaymentAndSocketManagerXRPL implements PaymentManager, SocketManager, Presets {
    private static PaymentAndSocketManagerXRPL instance = null;
    private SocketXRP socket;
    private WalletXRP wallet;



    public static synchronized PaymentAndSocketManagerXRPL getInstances() {
        if (instance == null) {
            Locale.setDefault(Locale.ENGLISH);
            instance = new PaymentAndSocketManagerXRPL();
        }
        return instance;
    }



    private PaymentAndSocketManagerXRPL() {
            this.socket = (SocketXRP) createNewSocket(true);
            this.wallet = new WalletXRP();
    }

    private PaymentAndSocketManagerXRPL(boolean b) {
        this.socket = (SocketXRP) createNewSocket(true);
        this.wallet = new WalletXRP();
    }

    public void restartSocket() {
        this.socket = (SocketXRP) createNewSocket(true);
    }


    // подумаь может можно сделать более элегантную проверку на нулл -- чтобы не предавать его дадльше
    private WebSocketClient createNewSocket(boolean b) {
        try {
            return new SocketXRP(com.samuilolegovich.config.NetworkConfig.getWssUrl());
        } catch (URISyntaxException e) { e.printStackTrace(); }
        return null;
    }


    // Payment *********************************************************************************************************

    @Override
    public boolean sendPayment(String address, String memo, BigDecimal numberOfXRP, boolean isReal) {
        if (isReal && wallet != null) { return wallet.sendPaymentToAddressXRP(address, memo, numberOfXRP); }
        return false;
    }

    @Override
    public boolean sendPayment(String address, Integer tag, BigDecimal numberOfXRP, boolean isReal) {
        if (isReal && wallet != null) { return wallet.sendPaymentToAddressXRP(address, tag, numberOfXRP); }
        return false;
    }

    @Override
    public boolean sendPayment(String address, BigDecimal numberOfXRP, boolean isReal) {
        if (isReal && wallet != null) { return wallet.sendPaymentToAddressXRP(address, numberOfXRP); }
        return false;
    }

    @Override
    // подключить существующий кошелек
    public Map<String, String> connectAnExistingWallet(String seed, boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.restoreWallet(seed);
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, String> createNewWallet(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.createNewWallet();
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, String> restoreWallet(String seed, boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.restoreWallet(seed);
        }
        return new HashMap<>();
    }

    @Override
    public void setterWallet(boolean isReal) {
        if (isReal && wallet != null) {

        }
    }

    @Override
    public String getClassicAddress(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.classicAddress().toString();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    @Override
    public String getPrivateKey(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.privateKey().get();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    @Override
    public String getXAddress(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.xAddress().toString();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    @Override
    public String getPublicKey(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.publicKey();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    @Override
    public String getSeed(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.getSeed();
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    @Override
    public boolean isTest(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.isTest();
        }
        return false;
    }

    @Override
    public BigDecimal getBalance(boolean isReal) {
        BigDecimal allBalance = getAllBalance(isReal);
        BigDecimal activationPayment = new BigDecimal(StringEnum.ACTIVATION_PAYMENT.getValue());
        int compareTo = allBalance.compareTo(activationPayment);
        if (compareTo <= 0) { return new BigDecimal("0.000000"); }
        return allBalance.subtract(activationPayment);
    }

    @Override
    public BigDecimal getAllBalance(boolean isReal) {
        if (isReal && wallet != null) {
            return wallet.getBalance();
        }
        return BigDecimal.ZERO;
    }



    // Socket **********************************************************************************************************

    @Override
    public String sendCommand(String command, 
                              CommandListener listener) throws Exception {
        if (socket != null) {
            return socket.sendCommand(command, null, listener);
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    @Override
    public String sendCommand(String command, 
                              Map<String, Object> parameters, 
                              CommandListener listener) throws Exception {
        if (socket != null) {
            return socket.sendCommand(command, parameters, listener);
        }
        return StringEnum.WALLET_NOT_ACTIVATED.getValue();
    }

    @Override
    public void subscribe(EnumSet<StreamSubscriptionEnum> streams, 
                          StreamSubscriber subscriber) throws Exception {
        if (socket != null) {
            socket.subscribe(streams, subscriber);
        }
    }

    @Override
    public void subscribe(EnumSet<StreamSubscriptionEnum> streams, 
                          Map<String, Object> parameters,
                          StreamSubscriber subscriber) throws Exception {
        if (socket != null) {
            socket.subscribe(streams, parameters, subscriber);
        }
    }

    @Override
    public void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        socket.unsubscribe(streams);
    }

    @Override
    public EnumSet<StreamSubscriptionEnum> getActiveSubscriptions() {
        if (socket != null) {
            return socket.getActiveSubscriptions();
        }
        return null;
    }

    @Override
    public void closeWhenComplete() {
        socket.closeWhenComplete();
    }

    public boolean startSocket() {
        if (socket != null) {
            try {
                javax.net.ssl.SSLSocketFactory sslSocketFactory =
                        com.samuilolegovich.config.NetworkConfig.IS_TESTNET
                                ? com.samuilolegovich.wallet.myClient.SslUtil.trustAllSocketFactory()
                                : com.samuilolegovich.wallet.myClient.SslUtil.pinnedSocketFactory(
                                        com.samuilolegovich.wallet.myClient.SslUtil.WSS_MAINNET_PINS);
                socket.setSocketFactory(sslSocketFactory);
                return socket.connectBlocking(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    // Presets *********************************************************************************************************


    public void setPresets(BooleanEnum enums, boolean b) {
        BooleanEnum.setValue(enums, b);
    }

    public void setPresets(StringEnum enums, String s) {
        StringEnum.setValue(enums, s);
    }

}
