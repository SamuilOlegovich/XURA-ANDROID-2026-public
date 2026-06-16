package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;



/**
 * Обрабатывает входящее от XRPL-сокета сообщение о платёжной транзакции и обновляет
 * баланс кошелька, вычитая из итогового остатка резерв активации сокета.
 */
public class UpdateBalanceRun implements Runnable {
    private String stringMassage;



    /** Сохраняет полученное от сокета JSON-сообщение о транзакции для последующего разбора. */
    public UpdateBalanceRun(String massage) {
        this.stringMassage = massage;
    }



    /** Извлекает из сообщения итоговый баланс счёта после транзакции (FinalFields.Balance) и передаёт его на обновление. */
    @Override
    public void run() {
        try {
            JSONObject message = new JSONObject(stringMassage);
            String balance = message.getJSONObject("meta")
                    .getJSONArray("AffectedNodes")
                    .getJSONObject(1)
                    .getJSONObject("ModifiedNode")
                    .getJSONObject("FinalFields")
                    .getString("Balance");
            update(balance);
        } catch (JSONException e ) {
            e.printStackTrace();
        }
    }


    /** Переводит баланс из дропс в XRP, вычитая резерв активации сокета, и передаёт его в WalletRepository. */
    private void update(String s) {
        BigDecimal one = new BigDecimal(s);
        BigDecimal tow = new BigDecimal(StringEnum.ACTIVATION_PAYMENT_SOCKET.getValue());
        BigDecimal balance = one.subtract(tow).divide(BigDecimal.valueOf(1_000_000L), MathContext.DECIMAL128);
        WalletRepository.getInstance().updateBalance(balance);
    }
}
