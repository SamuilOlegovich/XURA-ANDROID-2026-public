package com.samuilolegovich.wallet.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    public void updateBalance(BigDecimal balance) { balanceLiveData.postValue(balance); }
    public void setLottoNow(String lotto) { lottoTextLiveData.postValue(lotto); }
    public void notifyEvent(String message, String lotto, int type) {
        navigationEventLiveData.postValue(new NavigationEvent(type, message, lotto));
    }



    // Баланс

    // Загрузить баланс асинхронно и обновить LiveData
    public void loadBalance() {
        executor.execute(() -> updateBalance(getBalance()));
    }

    public BigDecimal getBalance() {
        return manager.getBalance(true);
    }

    public BigDecimal getAllBalance() {
        return manager.getAllBalance(true);
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