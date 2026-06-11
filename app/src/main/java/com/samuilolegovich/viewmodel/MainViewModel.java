package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.samuilolegovich.wallet.model.sockets.enums.StreamSubscriptionEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;
import com.samuilolegovich.wallet.subscribers.MyStreamSubscriber;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MainViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;



    public MainViewModel() {
        repository = WalletRepository.getInstance();
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



    // Действия

    public void loadBalance() {
        repository.loadBalance();
    }

    // Восстановить кошелёк → загрузить баланс → подключить сокет
    public void restoreAndInit(String seed) {
        executor.execute(() -> {
            try {
                Map<String, String> result = repository.restoreWallet(seed);
                if (result == null || !result.containsKey("Classic Address")) return;
                BigDecimal balance = repository.getBalance();
                repository.updateBalance(balance);
                connectSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void connectSocket() {
        repository.startSocket();
        try {
            Thread.sleep(1000);
            String address = repository.getClassicAddress();
            Map<String, Object> params = new HashMap<>();
            params.put("accounts", List.of(address));
            repository.subscribe(
                    EnumSet.of(StreamSubscriptionEnum.ACCOUNT_CHANNELS),
                    params,
                    new MyStreamSubscriber()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}