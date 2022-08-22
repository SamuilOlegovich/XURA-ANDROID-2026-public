package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.view.GuessTheColorGame;
import com.samuilolegovich.view.GuessTheNumberGame;
import com.samuilolegovich.view.SendPayment;

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
        BigDecimal balance = one.subtract(tow).divide(BigDecimal.valueOf(MainActivity.ONE_XRP_IN_DROPS), MathContext.DECIMAL128);
        if (MainActivity.MAIN_ACTIVITY != null) {
            MainActivity.MAIN_ACTIVITY.updateBalance(balance);
        }
        if (SendPayment.SEND_PAYMENT != null) {
            SendPayment.SEND_PAYMENT.updateBalance(balance);
        }
        if (GuessTheColorGame.GUESS_THE_COLOR_GAME != null) {
            GuessTheColorGame.GUESS_THE_COLOR_GAME.updateBalance(balance);
        }
        if (GuessTheNumberGame.GUESS_THE_NUMBER_GAME != null) {
            GuessTheNumberGame.GUESS_THE_NUMBER_GAME.updateBalance(balance);
        }
    }
}
