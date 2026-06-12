package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;



@HiltViewModel
public class GuessColorViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<GameBetError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> betSuccessLiveData = new MutableLiveData<>();



    @Inject
    public GuessColorViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    public LiveData<BigDecimal> getBalance() { return repository.getBalanceLiveData(); }
    public LiveData<GameBetError> getError() { return errorLiveData; }
    public LiveData<String> getBetSuccess() { return betSuccessLiveData; }



    public void loadBalance() {
        executor.execute(() -> repository.updateBalance(repository.getBalance()));
    }

    public void placeBet(String rawAmount, String colorTag, String myReferral) {
        executor.execute(() -> {
            String amount = prepareAmount(rawAmount);
            GameBetError error = validateAmount(amount);
            if (error != null) {
                errorLiveData.postValue(error);
                return;
            }

            String memoCmd = colorTag.equals(StringEnum.TAG_RED_GUESS_THE_COLOR.getValue())
                    ? "BET:RED" : "BET:BLK";
            String memo = memoCmd + ":" + myReferral;

            boolean success;
            if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                success = repository.sendPayment(
                        StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue(),
                        memo,
                        new BigDecimal(amount));
            } else {
                success = true;
            }

            if (success) {
                repository.updateBalance(repository.getBalance());
                betSuccessLiveData.postValue(amount);
            } else {
                errorLiveData.postValue(GameBetError.PAYMENT_FAILED);
            }
        });
    }



    private GameBetError validateAmount(String amount) {
        if (amount == null || amount.isEmpty()) return GameBetError.INVALID_AMOUNT;

        BigDecimal a;
        try { a = new BigDecimal(amount); }
        catch (NumberFormatException e) { return GameBetError.INVALID_AMOUNT; }

        if (a.compareTo(BigDecimal.ZERO) == 0) return GameBetError.AMOUNT_IS_ZERO;

        if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            BigDecimal balance = repository.getBalanceLiveData().getValue();
            if (balance != null && a.compareTo(balance) > 0) return GameBetError.INSUFFICIENT_BALANCE;
        }

        if (a.compareTo(new BigDecimal(StringEnum.MAX_BET_GUESS_THE_COLOR.getValue())) > 0)
            return GameBetError.BET_TOO_HIGH;
        if (a.compareTo(new BigDecimal(StringEnum.MIN_BET_GUESS_THE_COLOR.getValue())) < 0)
            return GameBetError.BET_TOO_LOW;

        return null;
    }

    private String prepareAmount(String amount) {
        if (amount.contains(".")) {
            int i = amount.indexOf(".");
            int max = i + 6;
            if (max < amount.length()) return amount.substring(0, max + 1);
        }
        return amount;
    }



    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}