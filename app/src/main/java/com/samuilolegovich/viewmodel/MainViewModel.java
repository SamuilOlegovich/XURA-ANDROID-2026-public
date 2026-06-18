package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;



/**
 * ViewModel главного экрана приложения: предоставляет Activity LiveData-потоки баланса,
 * текста лотереи, событий навигации и готовности кошелька, делегируя всю фактическую
 * работу с XRPL в {@link WalletRepository}.
 */
@HiltViewModel
public class MainViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;
    private final MutableLiveData<BigDecimal> realBalanceLiveData = new MutableLiveData<>();



    /** Создаёт ViewModel с внедрённым репозиторием кошелька и собственным фоновым executor'ом. */
    @Inject
    public MainViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    // LiveData для Activity

    /** Возвращает LiveData реального XRP-баланса кошелька (всегда сетевой, независимо от режима игры). */
    public LiveData<BigDecimal> getBalance() {
        return realBalanceLiveData;
    }

    /** Возвращает LiveData текста лотерейного номера/джекпота. */
    public LiveData<String> getLottoText() {
        return repository.getLottoTextLiveData();
    }

    /** Возвращает LiveData событий навигации (переход на экран выигрыша/проигрыша/реферала). */
    public LiveData<NavigationEvent> getNavigationEvent() {
        return repository.getNavigationEventLiveData();
    }

    /** Возвращает LiveData готовности кошелька к работе (инициализирован и подключён). */
    public LiveData<Boolean> getWalletReady() {
        return repository.getWalletReadyLiveData();
    }



    // Действия

    /** Загружает реальный XRP-баланс из сети и публикует его в LiveData кошелька. */
    public void loadBalance() {
        executor.execute(() -> realBalanceLiveData.postValue(repository.getRealBalance()));
    }

    /** Сбрасывает событие навигации после обработки, чтобы оно не доставилось повторно следующему наблюдателю. */
    public void clearNavigationEvent() {
        repository.clearNavigationEvent();
    }

    /** Восстанавливает кошелёк из сид-фразы и загружает реальный баланс; сокет запускает XrplSocketService. */
    public void restoreAndInit(String seed) {
        executor.execute(() -> {
            try {
                Map<String, String> result = repository.restoreWallet(seed);
                if (result == null || !result.containsKey("Classic Address")) return;
                realBalanceLiveData.postValue(repository.getRealBalance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    /** Останавливает фоновый executor при уничтожении ViewModel. */
    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}