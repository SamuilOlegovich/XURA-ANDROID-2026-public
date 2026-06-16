package com.samuilolegovich.wallet.model.paymentmanager.interfaces;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Контракт менеджера платежей и кошелька XRPL: отправка платежей (обычных, с мемо
 * или с destination tag), управление реальным/тестовым кошельком (создание,
 * восстановление, подключение существующего), доступ к ключевым данным и балансу.
 * Параметр {@code isReal} во всех методах выбирает, с каким из двух кошельков
 * (реальным или тестовым) работать.
 */
public interface PaymentManager {
    /** Отправляет платёж с мемо (используется для ставок и платежей с памяткой). */
    boolean sendPayment(String address, String memo, BigDecimal numberOfXRP, boolean isReal);
    /** Отправляет платёж с числовым destination tag. */
    boolean sendPayment(String address, Integer tag, BigDecimal numberOfXRP, boolean isReal);
    /** Отправляет обычный платёж без мемо и тега. */
    boolean sendPayment(String address, BigDecimal numberOfXRP, boolean isReal);
    /** Переключает активный кошелёк (реальный/тестовый), используемый последующими операциями. */
    void setterWallet(boolean isReal);

    /** Подключает уже существующий кошелёк по сид-фразе без его пересоздания. */
    Map<String, String> connectAnExistingWallet(String seed, boolean isReal);
    /** Восстанавливает кошелёк из сид-фразы. */
    Map<String, String> restoreWallet(String seed, boolean isReal);
    /** Создаёт новый кошелёк XRPL. */
    Map<String, String> createNewWallet(boolean isReal);

    /** Возвращает классический XRPL-адрес кошелька. */
    String getClassicAddress(boolean isReal);
    /** Возвращает приватный ключ кошелька. */
    String getPrivateKey(boolean isReal);
    /** Возвращает публичный ключ кошелька. */
    String getPublicKey(boolean isReal);
    /** Возвращает X-адрес кошелька. */
    String getXAddress(boolean isReal);
    /** Возвращает сид-фразу кошелька. */
    String getSeed(boolean isReal);

    /** Возвращает полный баланс кошелька без вычета резерва. */
    BigDecimal getAllBalance(boolean isReal);
    /** Возвращает доступный для трат баланс кошелька. */
    BigDecimal getBalance(boolean isReal);

    /** Сообщает, является ли указанный кошелёк тестовым. */
    boolean isTest(boolean isReal);
}
