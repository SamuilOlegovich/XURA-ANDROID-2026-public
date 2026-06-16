package com.samuilolegovich.wallet.model.wallets.interfaces;

import java.math.BigDecimal;
import java.util.Map;

/** Базовый контракт кошелька XRPL: создание нового кошелька, получение баланса и сид-фразы. */
public interface MyWallets {
    /** Создаёт новый кошелёк XRPL и возвращает его ключевые данные (адрес, ключи, сид). */
    Map<String, String> createNewWallet();
    /** Возвращает текущий баланс кошелька в XRP. */
    BigDecimal getBalance();
    /** Возвращает сид-фразу кошелька. */
    String getSeed();
}
