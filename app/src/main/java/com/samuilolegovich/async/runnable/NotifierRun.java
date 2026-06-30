package com.samuilolegovich.async.runnable;

import com.samuilolegovich.R;
import com.samuilolegovich.XuraApp;
import com.samuilolegovich.config.NetworkConfig;
import com.samuilolegovich.view.Flasher;
import com.samuilolegovich.view.YourReferral;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;



/**
 * Обрабатывает входящее сообщение XRPL-сокета о транзакции с Memo от игрового сервера
 * (ставка реальными XRP в режиме "боевой игры") и показывает игроку результат:
 * выигрыш/проигрыш в лотерее или выдачу реферального кода.
 */
public class NotifierRun implements Runnable {
    private String stringMassage;

    private String YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;
    private String CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO;
    private String CONGRATULATIONS_YOUR_BET_IS_WON;
    private String YOUR_REFERRAL_REWARD;
    private String YOUR_REFERRAL_CODE;



    /** Сохраняет полученное от сокета JSON-сообщение и сразу подгружает локализованные тексты уведомлений. */
    public NotifierRun(String massage) {
        this.stringMassage = massage;
        setLanguage();
    }



    /** Если сообщение содержит входящую транзакцию с Memo от игрового сервера — разбирает её и формирует уведомление игроку. */
    @Override
    public void run() {
        try {
            JSONObject message = new JSONObject(stringMassage);
            // входящая транзакция с memo (ответ сервера игроку)
            if (message.getJSONObject("transaction").has("Memos")
                    && !message.getJSONObject("transaction").getString("Destination")
                    .equals(NetworkConfig.SERVER_ROULETTE)) {
                responseToBet(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /** Загружает локализованные строки уведомлений (проигрыш/выигрыш/реферальный код) для текущего языка приложения. */
    private void setLanguage() {
        android.content.res.Resources resources = XuraApp.getLocalizedResources();
        YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY = resources.getString(R.string.your_bet_is_lost_try_again);
        CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO = resources.getString(R.string.congratulations_your_bet_is_won_loto);
        CONGRATULATIONS_YOUR_BET_IS_WON = resources.getString(R.string.congratulations_your_bet_is_won);
        YOUR_REFERRAL_REWARD = resources.getString(R.string.referral_reward_message);
        YOUR_REFERRAL_CODE = resources.getString(R.string.your_referral_code);
    }


    /**
     * Извлекает из Memo транзакции команду и число сервера, формирует сообщение для игрока.
     *
     * Форматы:
     *   - рулетка:            RLT:{число}:{WIN|LOSE}
     *   - ежедневная лотерея: RLT:LOTTO:DAILY:{дата}       (пример: RLT:LOTTO:DAILY:2026-06-30)
     *   - супер-лотерея:      RLT:LOTTO:SUPER:{год-месяц}  (пример: RLT:LOTTO:SUPER:2026-06)
     *   - остальные игры:     CMD:{serverNumber}            (WIN/LOSE/JKPT/LOTO/REF)
     */
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

            // все форматы с префиксом RLT:
            if (memoText.startsWith("RLT:")) {
                String[] parts = memoText.split(":");

                if (parts.length >= 3 && "LOTTO".equals(parts[1])) {
                    // ежедневная: RLT:LOTTO:DAILY:2026-06-30
                    // супер:      RLT:LOTTO:SUPER:2026-06
                    String lottoDate = parts.length > 3 ? parts[3] : "";
                    Flasher.NUMBER_BET = "0";
                    WalletRepository.getInstance().setLottoNow(lottoDate);
                    responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO, amountWin), lottoDate, 2);

                } else if (parts.length >= 3 && "REF".equals(parts[1])) {
                    // реферал: RLT:REF:CODE:123   — выдача/восстановление кода
                    //          RLT:REF:REWARD:123  — вознаграждение рефереру
                    String refType = parts[2];
                    String refCode = parts.length > 3 ? parts[3] : "";
                    if ("CODE".equals(refType)) {
                        YourReferral.CODE = refCode;
                        responseToBet(YOUR_REFERRAL_CODE + " \n" + refCode, refCode, 3);
                    } else if ("REWARD".equals(refType)) {
                        // не прерываем Flasher — вознаграждение не связано с текущей ставкой
                        WalletRepository.getInstance().notifyEvent(
                                String.format(YOUR_REFERRAL_REWARD, refCode, amountWin), refCode, 2);
                    }

                } else {
                    // обычная рулетка: RLT:{число}:{WIN|LOSE}
                    String rltNumber  = parts.length > 1 ? parts[1] : "0";
                    String rltOutcome = parts.length > 2 ? parts[2] : "";
                    Flasher.NUMBER_BET = rltNumber;
                    WalletRepository.getInstance().setLottoNow(rltNumber);
                    if ("WIN".equals(rltOutcome)) {
                        responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON, amountWin), rltNumber, 2);
                    } else {
                        responseToBet(YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY, rltNumber, 1);
                    }
                }
                return;
            }

            // остальные игры: CMD:{serverNumber}
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


    /** Преобразует hex-строку MemoData в массив байт для последующего декодирования в текст. */
    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }


    /**
     * Доставляет готовый текст результата игроку: если экран игры открыт — показывает его прямо там
     * (stopGame), иначе (или для реферального кода) кладёт уведомление в WalletRepository,
     * чтобы оно было показано позже, когда подходящий экран станет видимым.
     */
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
