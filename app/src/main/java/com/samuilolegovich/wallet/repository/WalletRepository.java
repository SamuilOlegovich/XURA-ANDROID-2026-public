package com.samuilolegovich.wallet.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.XuraApp;
import com.samuilolegovich.viewmodel.NavigationEvent;
import com.samuilolegovich.wallet.model.paymentmanager.PaymentAndSocketManagerXRPL;
import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.model.sockets.interfaces.CommandListener;
import com.samuilolegovich.wallet.subscribers.interfaces.StreamSubscriber;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Singleton-репозиторий, оборачивающий {@link PaymentAndSocketManagerXRPL} слоем
 * LiveData для UI-слоя (ViewModel/Activity): хранит и публикует баланс, текст лото
 * и события навигации, а также ведёт отдельный виртуальный баланс для тестового
 * (нереального) режима игры.
 */
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



    /** Получает singleton-экземпляр менеджера платежей и сокета. */
    private WalletRepository() {
        manager = PaymentAndSocketManagerXRPL.getInstances();
    }

    /** Возвращает единственный экземпляр репозитория, создавая его при первом обращении. */
    public static synchronized WalletRepository getInstance() {
        if (instance == null) {
            instance = new WalletRepository();
        }
        return instance;
    }



    // LiveData — для подписки из ViewModel / Activity

    /** Возвращает LiveData текущего баланса кошелька. */
    public LiveData<BigDecimal> getBalanceLiveData() { return balanceLiveData; }
    /** Возвращает LiveData текста лото (результата игры), отображаемого в UI. */
    public LiveData<String> getLottoTextLiveData() { return lottoTextLiveData; }
    /** Возвращает LiveData событий навигации/уведомлений для UI. */
    public LiveData<NavigationEvent> getNavigationEventLiveData() { return navigationEventLiveData; }
    /** Возвращает LiveData готовности кошелька (true после успешного создания/восстановления). */
    public LiveData<Boolean> getWalletReadyLiveData() { return walletReadyLiveData; }



    // Обновление состояния из фоновых потоков (postValue безопасен вне главного потока)

    /** Публикует новое значение баланса в LiveData и, если активен тестовый режим, сохраняет его как виртуальный баланс. */
    public void updateBalance(BigDecimal balance) {
        balanceLiveData.postValue(balance);
        if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            saveTestBalance(balance);
        }
    }

    /** Публикует новый текст лото (результата игры) в LiveData. */
    public void setLottoNow(String lotto) { lottoTextLiveData.postValue(lotto); }
    /** Публикует новое событие навигации/уведомления с сообщением, текстом лото и типом события. */
    public void notifyEvent(String message, String lotto, int type) {
        navigationEventLiveData.postValue(new NavigationEvent(type, message, lotto));
    }



    // Баланс

    /** Асинхронно загружает баланс (реальный или тестовый, в зависимости от режима игры) и публикует его в LiveData. */
    public void loadBalance() {
        executor.execute(() -> updateBalance(getBalance()));
    }

    /** Асинхронно запрашивает реальный баланс кошелька в сети независимо от режима игры (используется после faucet и swipe-обновления в DEV/testnet). */
    public void loadNetworkBalance() {
        executor.execute(() -> {
            BigDecimal balance = manager.getBalance(true);
            balanceLiveData.postValue(balance);
            // Сохраняем только если получили реальное значение — 0 обычно означает, что запрос не удался, а не что кошелёк пуст
            if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)
                    && balance.compareTo(BigDecimal.ZERO) > 0) {
                saveTestBalance(balance);
            }
        });
    }

    /** Возвращает текущий баланс: реальный сетевой в реальном режиме игры либо сохранённый виртуальный баланс в тестовом режиме. */
    public BigDecimal getBalance() {
        if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            return manager.getBalance(true);
        }
        return loadTestBalance();
    }

    /** Возвращает полный реальный баланс кошелька в сети без учёта режима игры и без вычета резерва активации. */
    public BigDecimal getAllBalance() {
        return manager.getAllBalance(true);
    }

    // Виртуальный баланс для тест-режима

    /** Уменьшает сохранённый виртуальный тестовый баланс на указанную сумму и публикует обновлённое значение. */
    public void deductTestBalance(BigDecimal amount) {
        updateBalance(loadTestBalance().subtract(amount));
    }

    /** Увеличивает сохранённый виртуальный тестовый баланс на указанную сумму и публикует обновлённое значение. */
    public void creditTestBalance(BigDecimal amount) {
        updateBalance(loadTestBalance().add(amount));
    }

    /** Сбрасывает виртуальный тестовый баланс до значения по умолчанию ({@link #DEFAULT_TEST_BALANCE}). */
    public void resetTestBalance() {
        updateBalance(new BigDecimal(DEFAULT_TEST_BALANCE));
    }

    /** Читает сохранённый виртуальный тестовый баланс из SharedPreferences, возвращая значение по умолчанию при отсутствии или повреждении данных. */
    private BigDecimal loadTestBalance() {
        String stored = testPrefs().getString(KEY_TEST_BALANCE, DEFAULT_TEST_BALANCE);
        try { return new BigDecimal(stored); }
        catch (Exception e) { return new BigDecimal(DEFAULT_TEST_BALANCE); }
    }

    /** Сохраняет виртуальный тестовый баланс в SharedPreferences. */
    private void saveTestBalance(BigDecimal balance) {
        testPrefs().edit().putString(KEY_TEST_BALANCE, balance.toPlainString()).apply();
    }

    /** Возвращает SharedPreferences, в которых хранится виртуальный тестовый баланс. */
    private SharedPreferences testPrefs() {
        return XuraApp.get().getSharedPreferences(TEST_PREFS, Context.MODE_PRIVATE);
    }



    // Кошелёк

    /** Создаёт новый реальный кошелёк через менеджер платежей. */
    public Map<String, String> createNewWallet() {
        return manager.createNewWallet(true);
    }

    /** Восстанавливает кошелёк из сид-фразы и, при успехе, помечает кошелёк готовым в LiveData. */
    public Map<String, String> restoreWallet(String seed) {
        Map<String, String> result = manager.connectAnExistingWallet(seed, true);
        if (result != null && result.containsKey("Classic Address")) {
            walletReadyLiveData.postValue(true);
        }
        return result;
    }

    /** Возвращает классический адрес текущего кошелька. */
    public String getClassicAddress() {
        return manager.getClassicAddress(true);
    }

    /** Возвращает публичный ключ текущего кошелька. */
    public String getPublicKey() {
        return manager.getPublicKey(true);
    }

    /** Возвращает приватный ключ текущего кошелька. */
    public String getPrivateKey() {
        return manager.getPrivateKey(true);
    }

    /** Возвращает X-адрес текущего кошелька. */
    public String getXAddress() {
        return manager.getXAddress(true);
    }

    /** Возвращает сид-фразу текущего кошелька. */
    public String getSeed() {
        return manager.getSeed(true);
    }



    // Платежи

    /** Отправляет обычный платёж с указанного кошелька на адрес получателя. */
    public boolean sendPayment(String address, BigDecimal amount) {
        return manager.sendPayment(address, amount, true);
    }

    /** Отправляет платёж с мемо на адрес получателя. */
    public boolean sendPayment(String address, String memo, BigDecimal amount) {
        return manager.sendPayment(address, memo, amount, true);
    }

    /** Отправляет платёж с destination tag на адрес получателя. */
    public boolean sendPayment(String address, Integer tag, BigDecimal amount) {
        return manager.sendPayment(address, tag, amount, true);
    }



    // Сокет

    /** Открывает WebSocket-соединение для подписок и команд XRPL. */
    public boolean startSocket() {
        return manager.startSocket();
    }

    /** Пересоздаёт WebSocket-соединение. */
    public void restartSocket() {
        manager.restartSocket();
    }

    /** Подписывается на указанные потоки событий с параметрами через сокет. */
    public void subscribe(EnumSet<StreamSubscriptionEnum> streams,
                          Map<String, Object> parameters,
                          StreamSubscriber subscriber) throws Exception {
        manager.subscribe(streams, parameters, subscriber);
    }

    /** Отписывается от указанных потоков событий. */
    public void unsubscribe(EnumSet<StreamSubscriptionEnum> streams) throws Exception {
        manager.unsubscribe(streams);
    }

    /** Закрывает WebSocket-соединение. */
    public void closeSocket() {
        manager.closeSocket();
    }

    /** Отправляет произвольную команду в сокет с параметрами и слушателем ответа. */
    public String sendCommand(String command, Map<String, Object> parameters,
                              CommandListener listener) throws Exception {
        return manager.sendCommand(command, parameters, listener);
    }
}