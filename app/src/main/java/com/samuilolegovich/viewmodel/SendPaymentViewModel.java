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



@HiltViewModel
public class SendPaymentViewModel extends ViewModel {
    private final WalletRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<SendError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> paymentSuccessLiveData = new MutableLiveData<>();



    @Inject
    public SendPaymentViewModel(WalletRepository repository) {
        this.repository = repository;
        executor = Executors.newSingleThreadExecutor();
    }



    public LiveData<BigDecimal> getBalance() {
        return repository.getBalanceLiveData();
    }

    public LiveData<SendError> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getPaymentSuccess() {
        return paymentSuccessLiveData;
    }



    public void loadBalance() {
        executor.execute(() -> {
            BigDecimal balance = repository.getBalance();
            repository.updateBalance(balance);
        });
    }

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
                BigDecimal newBalance = repository.getBalance();
                repository.updateBalance(newBalance);
                paymentSuccessLiveData.postValue(true);
            } else {
                errorLiveData.postValue(SendError.PAYMENT_FAILED);
            }
        });
    }



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



    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}