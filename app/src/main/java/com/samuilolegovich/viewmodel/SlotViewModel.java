package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.config.NetworkConfig;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;



/**
 * ViewModel игры «Слот-машина»: валидирует ставку, отправляет платёж
 * с мемо «BET:SLOT:{amount}:{referral}» на игровой сервер в реальном режиме,
 * либо просто списывает тестовый баланс в тренировочном.
 */
@HiltViewModel
public class SlotViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<GameBetError> errorLiveData   = new MutableLiveData<>();
    private final MutableLiveData<String>       betSuccessLiveData = new MutableLiveData<>();



    @Inject
    public SlotViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    public LiveData<BigDecimal> getBalance()    { return repository.getBalanceLiveData(); }
    public LiveData<GameBetError> getError()    { return errorLiveData; }
    public LiveData<String> getBetSuccess()     { return betSuccessLiveData; }

    public void loadBalance() {
        executor.execute(() -> repository.updateBalance(repository.getBalance()));
    }

    /** Размещает ставку: валидация → отправка (live) или списание баланса (trial) → публикация результата. */
    public void placeBet(String rawAmount, String referral) {
        executor.execute(() -> {
            GameBetError error = validateAmount(rawAmount);
            if (error != null) { errorLiveData.postValue(error); return; }

            String amount = prepareAmount(rawAmount);
            String memo   = "BET:SLOT:" + amount + ":" + (referral != null ? referral : "0");

            boolean success;
            if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                success = repository.sendPayment(
                        NetworkConfig.SERVER_ROULETTE,
                        memo,
                        new BigDecimal(amount));
            } else {
                success = true;
            }

            if (success) {
                if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                    repository.updateBalance(repository.getBalance());
                } else {
                    try { repository.deductTestBalance(new BigDecimal(amount)); }
                    catch (Exception ignored) {}
                }
                betSuccessLiveData.postValue(amount);
            } else {
                errorLiveData.postValue(GameBetError.PAYMENT_FAILED);
            }
        });
    }

    private GameBetError validateAmount(String raw) {
        if (raw == null || raw.isEmpty()) return GameBetError.INVALID_AMOUNT;
        BigDecimal val;
        try { val = new BigDecimal(raw.replace(",", ".")); }
        catch (NumberFormatException e) { return GameBetError.INVALID_AMOUNT; }
        if (val.compareTo(BigDecimal.ZERO) == 0)                                  return GameBetError.AMOUNT_IS_ZERO;
        if (val.compareTo(new BigDecimal(StringEnum.MIN_BET_SLOT.getValue())) < 0) return GameBetError.BET_TOO_LOW;
        if (val.compareTo(new BigDecimal(StringEnum.MAX_BET_SLOT.getValue())) > 0) return GameBetError.BET_TOO_HIGH;
        BigDecimal balance = repository.getBalanceLiveData().getValue();
        if (balance != null && val.compareTo(balance) > 0)                         return GameBetError.INSUFFICIENT_BALANCE;
        return null;
    }

    private String prepareAmount(String raw) {
        String s = raw.replace(",", ".");
        int dot = s.indexOf('.');
        if (dot >= 0 && s.length() > dot + 2) s = s.substring(0, dot + 2);
        return s;
    }

    @Override
    protected void onCleared() { executor.shutdown(); }
}
