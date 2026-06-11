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



public class GuessNumberViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<GameBetError> errorLiveData = new MutableLiveData<>();
    // Публикует подготовленную сумму ставки при успехе — Activity использует её для setBetParam()
    private final MutableLiveData<String> betSuccessLiveData = new MutableLiveData<>();



    public GuessNumberViewModel() {
        repository = WalletRepository.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }



    public LiveData<BigDecimal> getBalance() { return repository.getBalanceLiveData(); }
    public LiveData<GameBetError> getError() { return errorLiveData; }
    public LiveData<String> getBetSuccess() { return betSuccessLiveData; }



    public void loadBalance() {
        executor.execute(() -> repository.updateBalance(repository.getBalance()));
    }

    public void placeBet(String rawAmount, int selectedNumber, String myReferral) {
        executor.execute(() -> {
            if (selectedNumber <= 0) {
                errorLiveData.postValue(GameBetError.NO_NUMBER_SELECTED);
                return;
            }

            String tagPrefix = buildNumberTag(selectedNumber);
            if (tagPrefix == null) {
                errorLiveData.postValue(GameBetError.NO_NUMBER_SELECTED);
                return;
            }

            String amount = prepareAmount(rawAmount);
            GameBetError error = validateAmount(amount);
            if (error != null) {
                errorLiveData.postValue(error);
                return;
            }

            String sendTag = tagPrefix + myReferral;
            boolean success;
            if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                try {
                    success = repository.sendPayment(
                            StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue(),
                            Integer.parseInt(sendTag),
                            new BigDecimal(amount));
                } catch (NumberFormatException e) {
                    errorLiveData.postValue(GameBetError.TAG_TOO_LARGE);
                    return;
                }
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



    // Возвращает null если число вне диапазона [MIN..MAX] или некорректно
    private String buildNumberTag(int number) {
        int min = Integer.parseInt(StringEnum.MIN_BET_GUESS_THE_NUMBER.getValue());
        int max = Integer.parseInt(StringEnum.MAX_BET_GUESS_THE_NUMBER.getValue());
        if (number >= min && number <= max) {
            return (100 + number) + "";
        }
        return null;
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