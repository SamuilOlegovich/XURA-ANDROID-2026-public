package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;



public class UpdateBalanceRun implements Runnable {
    private String stringMassage;



    public UpdateBalanceRun(String massage) {
        this.stringMassage = massage;
    }



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


    private void update(String s) {
        BigDecimal one = new BigDecimal(s);
        BigDecimal tow = new BigDecimal(StringEnum.ACTIVATION_PAYMENT_SOCKET.getValue());
        BigDecimal balance = one.subtract(tow).divide(BigDecimal.valueOf(1_000_000L), MathContext.DECIMAL128);
        WalletRepository.getInstance().updateBalance(balance);
    }
}
