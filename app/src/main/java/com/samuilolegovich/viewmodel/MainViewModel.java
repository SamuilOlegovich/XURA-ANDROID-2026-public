package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;



@HiltViewModel
public class MainViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;



    @Inject
    public MainViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    // LiveData для Activity

    public LiveData<BigDecimal> getBalance() {
        return repository.getBalanceLiveData();
    }

    public LiveData<String> getLottoText() {
        return repository.getLottoTextLiveData();
    }

    public LiveData<NavigationEvent> getNavigationEvent() {
        return repository.getNavigationEventLiveData();
    }

    public LiveData<Boolean> getWalletReady() {
        return repository.getWalletReadyLiveData();
    }



    // Действия

    public void loadBalance() {
        repository.loadBalance();
    }

    // Восстановить кошелёк и загрузить баланс; сокет запускает XrplSocketService
    public void restoreAndInit(String seed) {
        executor.execute(() -> {
            try {
                Map<String, String> result = repository.restoreWallet(seed);
                if (result == null || !result.containsKey("Classic Address")) return;
                BigDecimal balance = repository.getBalance();
                repository.updateBalance(balance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}