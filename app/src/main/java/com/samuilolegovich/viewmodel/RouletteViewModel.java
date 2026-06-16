package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.config.NetworkConfig;
import com.samuilolegovich.enums.RouletteBetCode;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;



/**
 * ViewModel игры в рулетку: принимает сразу несколько одновременных ставок,
 * валидирует каждую и их сумму, отправляет один платёж с мемо, кодирующим все
 * ставки компактными кодами {@link RouletteBetCode}, в реальном режиме, либо
 * пропускает сетевой вызов в демо-режиме (результат считается локально через
 * NotifierRunForTrialGame).
 */
@HiltViewModel
public class RouletteViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<GameBetError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> betSuccessLiveData = new MutableLiveData<>();



    /** Создаёт ViewModel с внедрённым репозиторием кошелька и собственным фоновым executor'ом. */
    @Inject
    public RouletteViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    /** Возвращает LiveData текущего баланса кошелька. */
    public LiveData<BigDecimal> getBalance() { return repository.getBalanceLiveData(); }
    /** Возвращает LiveData ошибки валидации/отправки последней ставки. */
    public LiveData<GameBetError> getError() { return errorLiveData; }
    /** Публикует суммарную сумму ставки (XRP, строкой) при успешной отправке ставки. */
    public LiveData<String> getBetSuccess() { return betSuccessLiveData; }



    /** Запускает обновление баланса кошелька на фоновом потоке. */
    public void loadBalance() {
        executor.execute(() -> repository.updateBalance(repository.getBalance()));
    }

    /**
     * Размещает одну или несколько ставок на столе рулетки.
     *
     * В РЕАЛЬНОМ режиме: отправляет единый XRP-платёж на сервер. Мемо кодирует
     * каждую ставку компактными кодами из {@link RouletteBetCode}:
     *   BET:R:n5@1.5,r@2.0,d1@0.5:referralCode
     * Итоговая отправляемая сумма = сумма всех отдельных ставок.
     *
     * В ДЕМО-режиме: сетевой вызов не выполняется — результат вычисляется
     * локально через существующий NotifierRunForTrialGame (без изменений).
     *
     * @param bets      упорядоченная карта тег ставки → сумма (например "N:5" → 1.5)
     * @param referral  реферальный код игрока, добавляемый в мемо
     */
    public void placeBets(LinkedHashMap<String, BigDecimal> bets, String referral) {
        executor.execute(() -> {
            if (bets == null || bets.isEmpty()) {
                errorLiveData.postValue(GameBetError.NO_NUMBER_SELECTED);
                return;
            }

            // Проверяем каждую позицию (только минимум) и накапливаем общую сумму
            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : bets.entrySet()) {
                BigDecimal amount = roundToDrops(entry.getValue());
                GameBetError err = validateSingleBetAmount(amount);
                if (err != null) {
                    errorLiveData.postValue(err);
                    return;
                }
                total = total.add(amount);
            }

            // Общая сумма по всем позициям не должна превышать MAX_BET_ROULETTE
            if (total.compareTo(new BigDecimal(StringEnum.MAX_BET_ROULETTE.getValue())) > 0) {
                errorLiveData.postValue(GameBetError.BET_TOO_HIGH);
                return;
            }

            // Проверяем общую сумму относительно баланса
            BigDecimal balance = repository.getBalanceLiveData().getValue();
            if (balance != null && total.compareTo(balance) > 0) {
                errorLiveData.postValue(GameBetError.INSUFFICIENT_BALANCE);
                return;
            }

            boolean success;
            if (Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                String memo = buildMemo(bets, referral);
                success = repository.sendPayment(
                        NetworkConfig.SERVER_ROULETTE,
                        memo,
                        total);
            } else {
                success = true;
            }

            if (success) {
                repository.updateBalance(repository.getBalance());
                betSuccessLiveData.postValue(total.toPlainString());
            } else {
                errorLiveData.postValue(GameBetError.PAYMENT_FAILED);
            }
        });
    }

    /**
     * Строит строку мемо XRP-платежа, кодирующую все размещённые ставки.
     *
     * Формат:  BET:R:{код1}@{сумма1},{код2}@{сумма2},...:{referral}
     * Пример:  BET:R:n5@1.5,r@2.0,d1@0.5:ref123
     */
    private String buildMemo(LinkedHashMap<String, BigDecimal> bets, String referral) {
        StringBuilder sb = new StringBuilder("BET:R:");
        boolean first = true;
        for (Map.Entry<String, BigDecimal> entry : bets.entrySet()) {
            if (!first) sb.append(",");
            sb.append(RouletteBetCode.tagToCode(entry.getKey()));
            sb.append("@");
            sb.append(entry.getValue().stripTrailingZeros().toPlainString());
            first = false;
        }
        sb.append(":").append(referral != null ? referral : "0");
        return sb.toString();
    }

    /** Проверяет сумму одной отдельной ставки: ненулевое значение и не ниже минимальной ставки на рулетку. */
    private GameBetError validateSingleBetAmount(BigDecimal a) {
        if (a == null) return GameBetError.INVALID_AMOUNT;
        if (a.compareTo(BigDecimal.ZERO) == 0) return GameBetError.AMOUNT_IS_ZERO;
        if (a.compareTo(new BigDecimal(StringEnum.MIN_BET_ROULETTE.getValue())) < 0)
            return GameBetError.BET_TOO_LOW;
        return null;
    }

    /** Обрезает значение до 6 знаков после запятой (точность дропов XRP). */
    private BigDecimal roundToDrops(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        String s = value.toPlainString();
        int dot = s.indexOf('.');
        if (dot >= 0 && dot + 7 < s.length()) s = s.substring(0, dot + 7);
        try { return new BigDecimal(s); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }



    /** Останавливает фоновый executor при уничтожении ViewModel. */
    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
