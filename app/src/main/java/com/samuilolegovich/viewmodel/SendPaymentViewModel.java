package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;



/**
 * ViewModel экрана отправки платежа: валидирует адрес получателя, сумму и
 * необязательный destination tag, затем отправляет XRP-платёж через
 * {@link WalletRepository} и оповещает Activity о результате.
 */
@HiltViewModel
public class SendPaymentViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<SendError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> paymentSuccessLiveData = new MutableLiveData<>();



    /** Создаёт ViewModel с внедрённым репозиторием кошелька и собственным фоновым executor'ом. */
    @Inject
    public SendPaymentViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    /** Возвращает LiveData баланса кошелька из репозитория. */
    public LiveData<BigDecimal> getBalance() {
        return repository.getBalanceLiveData();
    }

    /** Возвращает LiveData ошибки валидации/отправки последнего платежа. */
    public LiveData<SendError> getError() {
        return errorLiveData;
    }

    /** Возвращает LiveData успешности последней отправки платежа. */
    public LiveData<Boolean> getPaymentSuccess() {
        return paymentSuccessLiveData;
    }



    /** Загружает баланс и публикует его в LiveData репозитория. */
    public void loadBalance() {
        repository.loadBalance();
    }

    /** Проверяет адрес/сумму/тег, отправляет платёж (с тегом назначения или без) и публикует результат успеха либо ошибки. */
    public void sendPayment(String address, String amount, String tag) {
        executor.execute(() -> {
            SendError error = validate(address, amount, tag);
            if (error != null) {
                errorLiveData.postValue(error);
                return;
            }

            String preparedAmount = prepareAmount(amount);
            boolean success;
            if (tag == null || tag.isEmpty()) {
                success = repository.sendPayment(address, new BigDecimal(preparedAmount));
            } else {
                success = repository.sendPayment(address, Integer.parseInt(tag), new BigDecimal(preparedAmount));
            }

            if (success) {
                repository.updateBalance(repository.getBalance());
                paymentSuccessLiveData.postValue(true);
            } else {
                errorLiveData.postValue(SendError.PAYMENT_FAILED);
            }
        });
    }



    /** Проверяет адрес получателя (длина), сумму (формат, ненулевое значение, достаточность баланса) и destination tag (длина и числовой диапазон). */
    private SendError validate(String address, String amount, String tag) {
        if (address == null || address.length() < 33) return SendError.WRONG_ADDRESS;
        if (amount == null || amount.isEmpty()) return SendError.INVALID_AMOUNT;

        BigDecimal amountDecimal;
        try {
            amountDecimal = new BigDecimal(prepareAmount(amount));
        } catch (NumberFormatException e) {
            return SendError.INVALID_AMOUNT;
        }

        if (amountDecimal.compareTo(BigDecimal.ZERO) == 0) return SendError.AMOUNT_IS_ZERO;

        BigDecimal currentBalance = repository.getBalanceLiveData().getValue();
        if (currentBalance != null && amountDecimal.compareTo(currentBalance) > 0) {
            return SendError.INSUFFICIENT_BALANCE;
        }

        if (tag != null && tag.length() > 11) return SendError.TAG_TOO_LONG;

        if (tag != null && !tag.isEmpty()) {
            try {
                if (Long.parseLong(tag) >= Integer.MAX_VALUE) return SendError.TAG_TOO_LARGE;
            } catch (NumberFormatException e) {
                return SendError.TAG_TOO_LONG;
            }
        }

        return null;
    }

    /** Обрезает сумму до 6 знаков после запятой (точность дропов XRP). */
    private String prepareAmount(String amount) {
        if (amount.contains(".")) {
            int i = amount.indexOf(".");
            int max = i + 6;
            if (max < amount.length()) {
                return amount.substring(0, max + 1);
            }
        }
        return amount;
    }



    /** Останавливает фоновый executor при уничтожении ViewModel. */
    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}