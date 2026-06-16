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
 * ViewModel игры "угадай цвет": валидирует размер ставки, отправляет платёж с
 * мемо-тегом ("BET:RED"/"BET:BLK") на сервер цвета в реальном режиме или
 * списывает тестовый баланс в тестовом, оповещая Activity об ошибке либо успехе.
 */
@HiltViewModel
public class GuessColorViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<GameBetError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> betSuccessLiveData = new MutableLiveData<>();



    /** Создаёт ViewModel с внедрённым репозиторием кошелька и собственным фоновым executor'ом. */
    @Inject
    public GuessColorViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    /** Возвращает LiveData текущего баланса кошелька. */
    public LiveData<BigDecimal> getBalance() { return repository.getBalanceLiveData(); }
    /** Возвращает LiveData ошибки валидации/отправки последней ставки. */
    public LiveData<GameBetError> getError() { return errorLiveData; }
    /** Возвращает LiveData с суммой последней успешно принятой ставки. */
    public LiveData<String> getBetSuccess() { return betSuccessLiveData; }



    /** Запускает обновление баланса кошелька на фоновом потоке. */
    public void loadBalance() {
        executor.execute(() -> repository.updateBalance(repository.getBalance()));
    }

    /** Проверяет и размещает ставку на выбранный цвет: формирует мемо-тег с реферальным кодом, отправляет платёж (или списывает тестовый баланс) и публикует результат. */
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
                        NetworkConfig.SERVER_COLOR,
                        memo,
                        new BigDecimal(amount));
            } else {
                success = true;
            }

            if (success) {
                if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                    repository.updateBalance(repository.getBalance());
                } else {
                    repository.deductTestBalance(new BigDecimal(amount));
                }
                betSuccessLiveData.postValue(amount);
            } else {
                errorLiveData.postValue(GameBetError.PAYMENT_FAILED);
            }
        });
    }



    /** Проверяет сумму ставки: корректность формата, ненулевое значение, достаточность баланса и попадание в допустимый диапазон ставок. */
    private GameBetError validateAmount(String amount) {
        if (amount == null || amount.isEmpty()) return GameBetError.INVALID_AMOUNT;

        BigDecimal a;
        try { a = new BigDecimal(amount); }
        catch (NumberFormatException e) { return GameBetError.INVALID_AMOUNT; }

        if (a.compareTo(BigDecimal.ZERO) == 0) return GameBetError.AMOUNT_IS_ZERO;

        BigDecimal balance = repository.getBalanceLiveData().getValue();
        if (balance != null && a.compareTo(balance) > 0) return GameBetError.INSUFFICIENT_BALANCE;

        if (a.compareTo(new BigDecimal(StringEnum.MAX_BET_GUESS_THE_COLOR.getValue())) > 0)
            return GameBetError.BET_TOO_HIGH;
        if (a.compareTo(new BigDecimal(StringEnum.MIN_BET_GUESS_THE_COLOR.getValue())) < 0)
            return GameBetError.BET_TOO_LOW;

        return null;
    }

    /** Обрезает сумму до 6 знаков после запятой, чтобы избежать слишком длинного числа в мемо-теге платежа. */
    private String prepareAmount(String amount) {
        if (amount.contains(".")) {
            int i = amount.indexOf(".");
            int max = i + 6;
            if (max < amount.length()) return amount.substring(0, max + 1);
        }
        return amount;
    }



    /** Останавливает фоновый executor при уничтожении ViewModel. */
    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}