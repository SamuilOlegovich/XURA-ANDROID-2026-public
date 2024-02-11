package com.samuilolegovich.asyncAndRun.runnable;

import android.content.res.Configuration;
import android.content.res.Resources;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.view.Flasher;
import com.samuilolegovich.view.YourReferral;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;



public class NotifierRunTest implements Runnable {
    private TestModeEnum testModeEnum;

    private String YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;
    private String CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO;
    private String CONGRATULATIONS_YOUR_BET_IS_WON;
    private String YOUR_REFERRAL_CODE;

    Random random;



    public NotifierRunTest(TestModeEnum testModeEnum) {
        this.testModeEnum = testModeEnum;
        this.random = new Random();
        setLanguage();
    }



    private void setLanguage() {
        // Получаем ресурсы для текущего языка
        Resources resources = getResourcesForLocale();
        // Далее вы можете использовать ресурсы для доступа к строкам на текущем языке
        YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY = resources.getString(R.string.your_bet_is_lost_try_again);
        CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO = resources.getString(R.string.congratulations_yoyr_bet_is_won_loto);
        CONGRATULATIONS_YOUR_BET_IS_WON = resources.getString(R.string.congratulations_yoyr_bet_is_won);
        YOUR_REFERRAL_CODE = resources.getString(R.string.your_referral_code);
    }


    private Resources getResourcesForLocale() {
        Configuration config = MainActivity.MAIN_ACTIVITY.getResources().getConfiguration();
        config.setLocale(MainActivity.newLocale);
        return new Resources(MainActivity.MAIN_ACTIVITY.getAssets(),
                MainActivity.MAIN_ACTIVITY.getResources().getDisplayMetrics(), config);
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

        } else {
            calculateForGuessTheNumber(random.nextInt(1000) + 1);

        }
    }


    private void calculateForGuessTheNumber(int i) {
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
        String amountWin = ((Double.parseDouble(Flasher.TEST_SAND_AMOUNT)) * 2.0) + "";
        String lotto = (random.nextInt(10001 - 4000) + 4000) + "";
        MainActivity.MAIN_ACTIVITY.setLottoNow(lotto);

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
