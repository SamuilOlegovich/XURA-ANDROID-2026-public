package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.view.Flasher;
import com.samuilolegovich.view.GuessTheColorGame;
import com.samuilolegovich.view.GuessTheNumberGame;
import com.samuilolegovich.view.YourReferral;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;

public class NotifierRun implements Runnable {
    private String stringMassage;


    public NotifierRun(String massage) {
        this.stringMassage = massage;
    }

    @Override
    public void run() {
        try {
            JSONObject message = new JSONObject(stringMassage);
            // если это транзакция и есть тех и она входящаяя
            if (message.getJSONObject("transaction").has("DestinationTag")
                    && !message.getJSONObject("transaction").getString("Destination")
                    .equals(StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue())) {
                responseToBet(message);
            }
        } catch (JSONException e ) {
            e.printStackTrace();
        }
    }

    private void responseToBet(JSONObject message) {
        try {
            String tagResponse = message.getJSONObject("transaction").getInt("DestinationTag") + "";
            String amountWin = new BigDecimal(message.getJSONObject("meta").getString("delivered_amount"))
                    .divide(BigDecimal.valueOf(MainActivity.ONE_XRP_IN_DROPS), MathContext.DECIMAL128)
                    .toString();

            if (tagResponse.length() > 3) {
                String tag = tagResponse.substring(0, 3);
                String lotto = tagResponse.substring(3);
                MainActivity.MAIN_ACTIVITY.setLottoNow(lotto);

                if (tag.equals(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue())) {
                    String text = "YOUR BET IS LOST - TRY AGAIN AND YOU WILL BE LUCKY!";
                    responseToBet(text, lotto, 1);

                } else if (tag.equals(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue())) {
                    String s = "CONGRATULATIONS! YOUR BET IS WON! THE WIN IS - "
                            +  amountWin
                            + " XRP - PLAY AND KEEP WINING!";
                    responseToBet(s, lotto, 2);

                } else if (tag.equals(StringEnum.LOTTO_WIN_GUESS_THE_COLOR.getValue())) {
                    String s = "CONGRATULATIONS!  YOUR BET IS WON THE LOTTO! THE WIN IS - "
                            +  amountWin
                            + " XRP - PLAY AND KEEP WINING!";
                    responseToBet(s, lotto, 2);

                }  else if (tag.equals(StringEnum.BECOME_A_REFERRAL.getValue())) {
                    YourReferral.CODE = lotto;
                    String s = "YOUR REFERRAL CODE \n"
                            + tag;
                    responseToBet(s, lotto, 3);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void responseToBet(String text, String lotto, int i) {
        if (Flasher.VISIBLE_ON_SCREEN && Flasher.FLASHER != null) {
            switch (i) {
                case 1 :
                    Flasher.FLASHER.stopGame(text, false);
                    break;
                case 2 : {
                    Flasher.FLASHER.stopGame(text, true);
                    break;
                } case 3 : {
                    if (MainActivity.MAIN_ACTIVITY != null) {
                        MainActivity.MAIN_ACTIVITY.notifyAboutAnEvent(text, lotto, i);
                    }
                }
            }
        } else  if (MainActivity.MAIN_ACTIVITY != null) {
            MainActivity.MAIN_ACTIVITY.notifyAboutAnEvent(text, lotto, i);
        }
    }
}
