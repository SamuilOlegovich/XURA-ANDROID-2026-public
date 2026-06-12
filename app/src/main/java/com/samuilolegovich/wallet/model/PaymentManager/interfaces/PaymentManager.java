package com.samuilolegovich.wallet.model.PaymentManager.interfaces;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentManager {
    boolean sendPayment(String address, String memo, BigDecimal numberOfXRP, boolean isReal);
    boolean sendPayment(String address, Integer tag, BigDecimal numberOfXRP, boolean isReal);
    boolean sendPayment(String address, BigDecimal numberOfXRP, boolean isReal);
    void setterWallet(boolean isReal);

    Map<String, String> connectAnExistingWallet(String seed, boolean isReal);
    Map<String, String> restoreWallet(String seed, boolean isReal);
    Map<String, String> createNewWallet(boolean isReal);

    String getClassicAddress(boolean isReal);
    String getPrivateKey(boolean isReal);
    String getPublicKey(boolean isReal);
    String getXAddress(boolean isReal);
    String getSeed(boolean isReal);

    BigDecimal getAllBalance(boolean isReal);
    BigDecimal getBalance(boolean isReal);

    boolean isTest(boolean isReal);
}
