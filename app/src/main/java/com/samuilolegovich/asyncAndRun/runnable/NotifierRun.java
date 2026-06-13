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
import java.nio.charset.StandardCharsets;



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
            // входящая транзакция с memo (ответ сервера игроку)
            if (message.getJSONObject("transaction").has("Memos")
                    && !message.getJSONObject("transaction").getString("Destination")
                    .equals(StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue())) {
                responseToBet(message);
            }
        } catch (JSONException e) {
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
            String hexMemo = message.getJSONObject("transaction")
                    .getJSONArray("Memos")
                    .getJSONObject(0)
                    .getJSONObject("Memo")
                    .getString("MemoData");
            String memoText = new String(hexToBytes(hexMemo), StandardCharsets.UTF_8).toUpperCase();

            String amountWin = new BigDecimal(message.getJSONObject("meta").getString("delivered_amount"))
                    .divide(BigDecimal.valueOf(1_000_000L), MathContext.DECIMAL128)
                    .toString();

            // формат memo ответа сервера: CMD:serverNumber
            String[] parts = memoText.split(":", 2);
            String cmd          = parts[0];
            String serverNumber = parts.length > 1 ? parts[1] : "0";

            WalletRepository.getInstance().setLottoNow(serverNumber);

            switch (cmd) {
                case "LOSE":
                    Flasher.NUMBER_BET = serverNumber;
                    responseToBet(YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY, serverNumber, 1);
                    break;
                case "WIN":
                    Flasher.NUMBER_BET = serverNumber;
                    responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON, amountWin), serverNumber, 2);
                    break;
                case "JKPT":
                case "LOTO":
                    Flasher.NUMBER_BET = serverNumber;
                    responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO, amountWin), serverNumber, 2);
                    break;
                case "REF":
                    YourReferral.CODE = serverNumber;
                    responseToBet(YOUR_REFERRAL_CODE + " \n" + serverNumber, serverNumber, 3);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }


    private void responseToBet(String text, String serverNumber, int i) {
        if (Flasher.VISIBLE_ON_SCREEN && Flasher.FLASHER != null) {
            switch (i) {
                case 1:
                    Flasher.FLASHER.stopGame(text, false);
                    break;
                case 2:
                    Flasher.FLASHER.stopGame(text, true);
                    break;
                case 3:
                    WalletRepository.getInstance().notifyEvent(text, serverNumber, i);
                    break;
            }
        } else {
            WalletRepository.getInstance().notifyEvent(text, serverNumber, i);
        }
    }
}
