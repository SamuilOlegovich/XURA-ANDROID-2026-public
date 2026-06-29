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
 * ViewModel игры "угадай число": проверяет выбранное число и размер ставки,
 * отправляет платёж с мемо-тегом в формате рулетки ("RLT:n{N}@{сумма}:ref")
 * на сервер чисел в реальном режиме или списывает тестовый баланс в тестовом.
 */
@HiltViewModel
public class GuessNumberViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<GameBetError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> betSuccessLiveData = new MutableLiveData<>();



    /** Создаёт ViewModel с внедрённым репозиторием кошелька и собственным фоновым executor'ом. */
    @Inject
    public GuessNumberViewModel(WalletRepository repository) {
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

    /** Проверяет выбранное число и сумму ставки, формирует мемо-тег с номером и реферальным кодом, отправляет платёж (или списывает тестовый баланс) и публикует результат. */
    public void placeBet(String rawAmount, int selectedNumber, String myReferral) {
        executor.execute(() -> {
            int min = Integer.parseInt(StringEnum.MIN_BET_GUESS_THE_NUMBER.getValue());
            int max = Integer.parseInt(StringEnum.MAX_BET_GUESS_THE_NUMBER.getValue());
            if (selectedNumber <= 0 || selectedNumber < min || selectedNumber > max) {
                errorLiveData.postValue(GameBetError.NO_NUMBER_SELECTED);
                return;
            }

            GameBetError error = validateAmount(rawAmount);
            if (error != null) {
                errorLiveData.postValue(error);
                return;
            }
            String amount = prepareAmount(rawAmount);

            String memo = "RLT:n" + selectedNumber + "@" + amount + ":" + myReferral;

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

    /** Обрезает сумму до 1 знака после точки (шаг 0.1 XRP). */
    private String prepareAmount(String amount) {
        if (amount.contains(".")) {
            int i = amount.indexOf(".");
            if (i + 1 < amount.length()) return amount.substring(0, i + 2);
        }
        return amount;
    }



    /** Останавливает фоновый executor при уничтожении ViewModel. */
    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}