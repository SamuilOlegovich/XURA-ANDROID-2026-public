package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.R;
import com.samuilolegovich.XuraApp;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.view.Flasher;
import com.samuilolegovich.view.YourReferral;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;



public class NotifierRun implements Runnable {
    private String stringMassage;

    private String YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;
    private String CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO;
    private String CONGRATULATIONS_YOUR_BET_IS_WON;
    private String YOUR_REFERRAL_CODE;



    public NotifierRun(String massage) {
        this.stringMassage = massage;
        setLanguage();
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


    private void setLanguage() {
        android.content.res.Resources resources = XuraApp.getLocalizedResources();
        YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY = resources.getString(R.string.your_bet_is_lost_try_again);
        CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO = resources.getString(R.string.congratulations_yoyr_bet_is_won_loto);
        CONGRATULATIONS_YOUR_BET_IS_WON = resources.getString(R.string.congratulations_yoyr_bet_is_won);
        YOUR_REFERRAL_CODE = resources.getString(R.string.your_referral_code);
    }


    private void responseToBet(JSONObject message) {
        try {
            String tagResponse = message.getJSONObject("transaction").getInt("DestinationTag") + "";
            String amountWin = new BigDecimal(message.getJSONObject("meta").getString("delivered_amount"))
                    .divide(BigDecimal.valueOf(1_000_000L), MathContext.DECIMAL128)
                    .toString();

            if (tagResponse.length() > 3) {
                String tag = tagResponse.substring(0, 3);
                String lotto = tagResponse.substring(3);
                WalletRepository.getInstance().setLottoNow(lotto);

                if (tag.equals(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue())) {
                    responseToBet(YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY, lotto, 1);

                } else if (tag.equals(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue())) {
                    responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON, amountWin), lotto, 2);

                } else if (tag.equals(StringEnum.LOTTO_WIN_GUESS_THE_COLOR.getValue())) {
                    responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO, amountWin), lotto, 2);


                }  else if (tag.equals(StringEnum.BECOME_A_REFERRAL.getValue())) {
                    YourReferral.CODE = lotto;
                    String s = YOUR_REFERRAL_CODE + " \n" + tag;
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
                case 1:
                    Flasher.FLASHER.stopGame(text, false);
                    break;
                case 2:
                    Flasher.FLASHER.stopGame(text, true);
                    break;
                case 3:
                    WalletRepository.getInstance().notifyEvent(text, lotto, i);
                    break;
            }
        } else {
            WalletRepository.getInstance().notifyEvent(text, lotto, i);
        }
    }
}
