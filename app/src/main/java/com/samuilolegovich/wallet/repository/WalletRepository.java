package com.samuilolegovich.wallet.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.XuraApp;
import com.samuilolegovich.viewmodel.NavigationEvent;
import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.model.sockets.interfaces.CommandListener;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class WalletRepository {
    private static WalletRepository instance;
    private final PaymentAndSocketManagerXRPL manager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<BigDecimal> balanceLiveData = new MutableLiveData<>(new BigDecimal("0.000000"));
    private final MutableLiveData<String> lottoTextLiveData = new MutableLiveData<>("");
    private final MutableLiveData<NavigationEvent> navigationEventLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> walletReadyLiveData = new MutableLiveData<>(false);

    private static final String TEST_PREFS       = "xura_test";
    private static final String KEY_TEST_BALANCE = "test_virtual_balance";
    public  static final String DEFAULT_TEST_BALANCE = "1000.000000";



    private WalletRepository() {
        manager = PaymentAndSocketManagerXRPL.getInstances();
    }

    public static synchronized WalletRepository getInstance() {
        if (instance == null) {
            instance = new WalletRepository();
        }
        return instance;
    }



    // LiveData — для подписки из ViewModel / Activity

    public LiveData<BigDecimal> getBalanceLiveData() { return balanceLiveData; }
    public LiveData<String> getLottoTextLiveData() { return lottoTextLiveData; }
    public LiveData<NavigationEvent> getNavigationEventLiveData() { return navigationEventLiveData; }
    public LiveData<Boolean> getWalletReadyLiveData() { return walletReadyLiveData; }



    // Обновление состояния из фоновых потоков (postValue безопасен вне главного потока)

    public void updateBalance(BigDecimal balance) {
        balanceLiveData.postValue(balance);
        if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            saveTestBalance(balance);
        }
    }

    public void setLottoNow(String lotto) { lottoTextLiveData.postValue(lotto); }
    public void notifyEvent(String message, String lotto, int type) {
        navigationEventLiveData.postValue(new NavigationEvent(type, message, lotto));
    }



    // Баланс

    public void loadBalance() {
        executor.execute(() -> updateBalance(getBalance()));
    }

    // Fetches real on-chain balance regardless of game mode (used after faucet and swipe-refresh in DEV/testnet)
    public void loadNetworkBalance() {
        executor.execute(() -> {
            BigDecimal balance = manager.getBalance(true);
            balanceLiveData.postValue(balance);
            // Only persist if we got a real value — 0 usually means query failed, not empty wallet
            if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)
                    && balance.compareTo(BigDecimal.ZERO) > 0) {
                saveTestBalance(balance);
            }
        });
    }

    public BigDecimal getBalance() {
        if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            return manager.getBalance(true);
        }
        return loadTestBalance();
    }

    public BigDecimal getAllBalance() {
        return manager.getAllBalance(true);
    }

    // Виртуальный баланс для тест-режима

    public void deductTestBalance(BigDecimal amount) {
        updateBalance(loadTestBalance().subtract(amount));
    }

    public void creditTestBalance(BigDecimal amount) {
        updateBalance(loadTestBalance().add(amount));
    }

    public void resetTestBalance() {
        updateBalance(new BigDecimal(DEFAULT_TEST_BALANCE));
    }

    private BigDecimal loadTestBalance() {
        String stored = testPrefs().getString(KEY_TEST_BALANCE, DEFAULT_TEST_BALANCE);
        try { return new BigDecimal(stored); }
        catch (Exception e) { return new BigDecimal(DEFAULT_TEST_BALANCE); }
    }

    private void saveTestBalance(BigDecimal balance) {
        testPrefs().edit().putString(KEY_TEST_BALANCE, balance.toPlainString()).apply();
    }

    private SharedPreferences testPrefs() {
        return XuraApp.get().getSharedPreferences(TEST_PREFS, Context.MODE_PRIVATE);
    }



    // Кошелёк

    public Map<String, String> createNewWallet() {
        return manager.createNewWallet(true);
    }

    public Map<String, String> restoreWallet(String seed) {
        Map<String, String> result = manager.connectAnExistingWallet(seed, true);
        if (result != null && result.containsKey("Classic Address")) {
            walletReadyLiveData.postValue(true);
        }
        return result;
    }

    public String getClassicAddress() {
        return manager.getClassicAddress(true);
    }

    public String getPublicKey() {
        return manager.getPublicKey(true);
    }

    public String getPrivateKey() {
        return manager.getPrivateKey(true);
    }

    public String getXAddress() {
        return manager.getXAddress(true);
    }

    public String getSeed() {
        return manager.getSeed(true);
    }



    // Платежи

    public boolean sendPayment(String address, BigDecimal amount) {
        return manager.sendPayment(address, amount, true);
    }

    public boolean sendPayment(String address, String memo, BigDecimal amount) {
        return manager.sendPayment(address, memo, amount, true);
    }

    public boolean sendPayment(String address, Integer tag, BigDecimal amount) {
        return manager.sendPayment(address, tag, amount, true);
    }



    // Сокет

    public boolean startSocket() {
        return manager.startSocket();
    }

    public void restartSocket() {
        manager.restartSocket();
    }

    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          Map<String, Object> parameters,
                          StreamSubscriber subscriber) throws Exception {
        manager.subscribe(streams, parameters, subscriber);
    }

    public void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        manager.unsubscribe(streams);
    }

    public void closeSocket() {
        manager.closeSocket();
    }

    public String sendCommand(String command, Map<String, Object> parameters,
                              CommandListener listener) throws Exception {
        return manager.sendCommand(command, parameters, listener);
    }
}