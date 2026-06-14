package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.XuraApp;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.Flasher;
import com.samuilolegovich.view.YourReferral;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.util.Random;



public class NotifierRunForTrialGame implements Runnable {
    private TestModeEnum testModeEnum;

    private String YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;
    private String CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO;
    private String CONGRATULATIONS_YOUR_BET_IS_WON;
    private String YOUR_REFERRAL_CODE;

    Random random;



    public NotifierRunForTrialGame(TestModeEnum testModeEnum) {
        this.testModeEnum = testModeEnum;
        this.random = new Random();
        setLanguage();
    }



    private void setLanguage() {
        android.content.res.Resources resources = XuraApp.getLocalizedResources();
        YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY = resources.getString(R.string.your_bet_is_lost_try_again);
        CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO = resources.getString(R.string.congratulations_your_bet_is_won_loto);
        CONGRATULATIONS_YOUR_BET_IS_WON = resources.getString(R.string.congratulations_your_bet_is_won);
        YOUR_REFERRAL_CODE = resources.getString(R.string.your_referral_code);
    }


    @Override
    public void run() {
        // Генерация случайного числа в диапазоне от 4000 до 10000
        Long timeSleep = (long) (random.nextInt(10001 - 4000) + 4000);

        try {
            Thread.sleep(timeSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (testModeEnum.equals(TestModeEnum.GUESS_THE_COLOR_GAME)) {
            calculateForGuessTheColor(random.nextInt(1000) + 1);
        } else if (testModeEnum.equals(TestModeEnum.ROULETTE_GAME)) {
            calculateForRoulette(random.nextInt(37)); // 0–36
        } else {
            calculateForGuessTheNumber(random.nextInt(36) + 1);
        }
    }


    private void calculateForRoulette(int winNumber) {
        Flasher.NUMBER_BET = String.valueOf(winNumber);

        String betTag = Flasher.ROULETTE_BET_TAG != null ? Flasher.ROULETTE_BET_TAG : "N:0";
        boolean win = evaluateRouletteBet(betTag, winNumber);

        double multiplier = Flasher.ROULETTE_WIN_MULTIPLIER;
        double amount = Double.parseDouble(
                Flasher.TEST_SAND_AMOUNT != null ? Flasher.TEST_SAND_AMOUNT : "1");
        String amountWin = String.valueOf(amount * multiplier);

        String lotto = String.valueOf(random.nextInt(10001 - 4000) + 4000);
        WalletRepository.getInstance().setLottoNow(lotto);

        if (win && !Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            try { WalletRepository.getInstance().creditTestBalance(new java.math.BigDecimal(amountWin)); }
            catch (Exception ignored) {}
        }

        String msg = win
                ? String.format(CONGRATULATIONS_YOUR_BET_IS_WON, amountWin)
                : YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;
        responseToBet(msg, lotto, win ? 2 : 1);
    }

    private boolean evaluateRouletteBet(String betTag, int n) {
        if (betTag.startsWith("N:")) {
            return Integer.parseInt(betTag.substring(2)) == n;
        }
        if (n == 0) return false; // 0 only wins straight-bet on 0
        switch (betTag) {
            case "RED":   return !Lotto.learnTheColorOfNumber(String.valueOf(n));
            case "BLACK": return  Lotto.learnTheColorOfNumber(String.valueOf(n));
            case "ODD":   return n % 2 != 0;
            case "EVEN":  return n % 2 == 0;
            case "LOW":   return n >= 1  && n <= 18;
            case "HIGH":  return n >= 19 && n <= 36;
            case "D1":    return n >= 1  && n <= 12;
            case "D2":    return n >= 13 && n <= 24;
            case "D3":    return n >= 25 && n <= 36;
            case "C1":    return n % 3 == 1; // 1,4,7,...,34
            case "C2":    return n % 3 == 2; // 2,5,8,...,35
            case "C3":    return n % 3 == 0; // 3,6,9,...,36
            default:      return false;
        }
    }

    private void calculateForGuessTheNumber(int i) {
        if (Flasher.NUMBER_BET.equalsIgnoreCase(i + "")) {
            responseToBet(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue());
        } else {
            responseToBet(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue());
        }
    }


    private void calculateForGuessTheColor(int randomNumber) {
        if (randomNumber < 500 && Flasher.COLOR_BET) {
            // победа
            responseToBet(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue());
        } else if (randomNumber >= 500 && Flasher.COLOR_BET) {
            // проигрыш
            responseToBet(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue());
        } else if (randomNumber < 500) {
            // проигрыш
            responseToBet(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue());
        } else {
            // победа
            responseToBet(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue());
        }
    }


    private void responseToBet(String tag) {
        String amountWin = testModeEnum.equals(TestModeEnum.GUESS_THE_NUMBER_GAME)
                ? ((Double.parseDouble(Flasher.TEST_SAND_AMOUNT)) * 36.0) + ""
                : ((Double.parseDouble(Flasher.TEST_SAND_AMOUNT)) * 2.0) + "";

        String lotto = (random.nextInt(10001 - 4000) + 4000) + "";
        WalletRepository.getInstance().setLottoNow(lotto);

        if (tag.equals(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue())) {
            responseToBet(YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY, lotto, 1);

        } else if (tag.equals(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue())) {
            if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                try { WalletRepository.getInstance().creditTestBalance(new java.math.BigDecimal(amountWin)); }
                catch (Exception ignored) {}
            }
            responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON, amountWin), lotto, 2);

        } else if (tag.equals(StringEnum.LOTTO_WIN_GUESS_THE_COLOR.getValue())) {
            if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                try { WalletRepository.getInstance().creditTestBalance(new java.math.BigDecimal(amountWin)); }
                catch (Exception ignored) {}
            }
            responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO, amountWin), lotto, 2);


        }  else if (tag.equals(StringEnum.BECOME_A_REFERRAL.getValue())) {
            YourReferral.CODE = lotto;
            String s = YOUR_REFERRAL_CODE + " \n" + tag;
            responseToBet(s, lotto, 3);
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
