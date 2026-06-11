package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.asyncTask.GetBalanceAsync;
import com.samuilolegovich.asyncAndRun.asyncTask.SendPaymentAsync;
import com.samuilolegovich.asyncAndRun.runnable.GenColorRun;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.Lotto;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameGuessTheColor.RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS;



public class GuessTheColorGame extends AppCompatActivity {
    public static final String GUESS_THE_COLOR_GAME_CLASS = ".GuessTheColorGame";
    private static final int MAX_VOLUME = 100;

    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String BET_IS_MADE_EXPECT_THE_RESULT;
    private String TAG_KNOWLEDGE_CANNOT_BE_MORE;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String BET_CANNOT_BE_MORE_THAN;
    private String BET_CANNOT_BE_LESS_THAN;


    @SuppressLint("StaticFieldLeak")
    public static volatile GuessTheColorGame GUESS_THE_COLOR_GAME;
    public static volatile boolean VISIBLE_ON_SCREEN = false;

    private SharedPreferences preferences;
    private MediaPlayer casinoMediaPlayer;
    private MediaPlayer errorMediaPlayer;
    private MediaPlayer betMediaPlayer;
    private Animation animTranslate;
    private BigDecimal yourBalance;

    private String myReferral;

    private TextView nameGameTextViewTwo;
    private TextView yourBalanceTextView;
    private TextView rulesOfTheGameLink;
    private TextView nameGameTextView;
    private TextView balance;
    private TextView black;
    private TextView red;
    private EditText bet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.guess_the_color_game_page);
        setButtons();
        setLanguage();
        listeners();
        setBalance();
        getReferral();
        GUESS_THE_COLOR_GAME = this;
        goThread();
    }



    private void soundPlay(MediaPlayer mediaPlayer) {
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }


    private void setButtons() {
        nameGameTextViewTwo = (TextView) findViewById(R.id.name_game_text_view_tow);
        yourBalanceTextView = (TextView) findViewById(R.id.your_balance_text_view);
        rulesOfTheGameLink = (TextView) findViewById(R.id.rules_of_the_game_link);
        nameGameTextView = (TextView) findViewById(R.id.name_game_text_view);
        balance = (TextView) findViewById(R.id.your_balance_xrp_text);
        black = (TextView) findViewById(R.id.color_black);
        red = (TextView) findViewById(R.id.color_red);
        bet = (EditText) findViewById(R.id.bet_field);

        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        errorMediaPlayer = MediaPlayer.create(this, R.raw.error);
        betMediaPlayer = MediaPlayer.create(this, R.raw.bet);

        soundPlay(casinoMediaPlayer);
    }


    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL = getString(R.string.it_is_not_possible_to_send_null);
        BET_IS_MADE_EXPECT_THE_RESULT = getString(R.string.bet_is_made_expect_the_result);
        TAG_KNOWLEDGE_CANNOT_BE_MORE = getString(R.string.tag_knowledge_cannot_be_more);
        PAYMENT_AMOUNT_IS_INCORRECT = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS = getString(R.string.wrong_destination_address);
        BET_CANNOT_BE_MORE_THAN = getString(R.string.bet_cannot_be_more_than);
        BET_CANNOT_BE_LESS_THAN = getString(R.string.bet_cannot_be_less_than);
        nameGameTextViewTwo.setText(R.string.and_double_the_bet);
        rulesOfTheGameLink.setText(R.string.rules_of_the_game);
        yourBalanceTextView.setText(R.string.your_balance);
        nameGameTextView.setText(R.string.guess_the_color);
        black.setText(R.string.black);
        red.setText(R.string.red);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        rulesOfTheGameLink.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS);
                    }
                }
        );

        black.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        betMediaPlayer.start();
                        Flasher.COLOR_BET = true;
                        makeStackThread(StringEnum.TAG_BLACK_GUESS_THE_NUMBER.getValue(), true);
                    }
                }
        );

        red.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        betMediaPlayer.start();
                        Flasher.COLOR_BET = false;
                        makeStackThread(StringEnum.TAG_RED_GUESS_THE_COLOR.getValue(), false);
                    }
                }
        );
    }


    private void makeStackThread(String string, boolean b) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeStack(string, b);
            }
        }).start();
    }


    private void makeStack(String tag, boolean color) {
        String sendAmount = prepareTheShippingAmount(bet.getText().toString());
        String sendTeg = tag + myReferral;

        if (checkData(sendAmount, sendTeg)) {
            bet.setText("");
            setBetParam(sendAmount, color);
            goToAnotherPage(FLASHER_CLASS);
            makeToast(BET_IS_MADE_EXPECT_THE_RESULT);
        }
    }


    private void setBetParam(String sendAmount,
                             boolean color) {
        Flasher.NUMBER_BET = Lotto.getRandomNumberForColor(color) + "";
        Flasher.TEST_MODE_ENUM = TestModeEnum.GUESS_THE_COLOR_GAME;
        Flasher.TEST_SAND_AMOUNT = sendAmount;
        Flasher.COLOR_BET = color;

    }


    public void setColorAndText(String text, boolean b) {
        // goToAnotherPage
        new Thread() {
            public void run() {
                GUESS_THE_COLOR_GAME.runOnUiThread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    public void run() {
                        // result display removed
                    }
                });
            }
        }.start();
    }


    private boolean checkData(String sendAmount, String sendTeg) {
        if (sendAmount == null || sendAmount.length() < 1) {
            errorMediaPlayer.start();
            makeToast(PAYMENT_AMOUNT_IS_INCORRECT);
            return false;
        }

        if (new BigDecimal(sendAmount).compareTo(new BigDecimal("0.000000")) == 0) {
            errorMediaPlayer.start();
            makeToast( IT_IS_NOT_POSSIBLE_TO_SEND_NULL);
            return false;
        }

        if (MainActivity.IS_REAL_GAME_MODE && new BigDecimal(sendAmount).compareTo(yourBalance) > 0) {
            errorMediaPlayer.start();
            makeToast(YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND);
            return false;
        }

        if (sendTeg != null && !sendTeg.equals("") && Long.parseLong(sendTeg) >= Integer.MAX_VALUE) {
            errorMediaPlayer.start();
            makeToast(TAG_KNOWLEDGE_CANNOT_BE_MORE);
            return false;
        }

        if (new BigDecimal(sendAmount).compareTo(new BigDecimal(StringEnum.MAX_BET_GUESS_THE_COLOR.getValue())) > 0) {
            errorMediaPlayer.start();
            makeToast(BET_CANNOT_BE_MORE_THAN + StringEnum.MAX_BET_GUESS_THE_COLOR.getValue() + "XRP");
            return false;
        }

        if (new BigDecimal(sendAmount).compareTo(new BigDecimal(StringEnum.MIN_BET_GUESS_THE_COLOR.getValue())) < 0) {
            errorMediaPlayer.start();
            makeToast(BET_CANNOT_BE_LESS_THAN + StringEnum.MIN_BET_GUESS_THE_COLOR.getValue() + "XRP");
            return false;
        }

        return makePayment(sendAmount, sendTeg);
    }


    private boolean makePayment(String sendAmount, String sendTeg) {

        if (MainActivity.IS_REAL_GAME_MODE) {
            boolean b = false;
            AsyncTask<String, Void, Boolean> asyncTask = new SendPaymentAsync()
                    .execute(StringEnum.SERVER_ADDRESS_GUESS_THE_COLOR.getValue(), sendAmount, sendTeg);

            try {
                b = asyncTask.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            if (!b) {
                errorMediaPlayer.start();
                makeToast(WRONG_DESTINATION_ADDRESS);
            }

            return b;
        } else {
            return true;
        }
    }


    private void getReferral() {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        if (preferences.contains(StringEnum.APP_PREFERENCES_REFERRAL.getValue())) {
            myReferral = preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "");
        } else {
            myReferral = "0";
        }
    }


    private String prepareTheShippingAmount(String sendAmount) {
        if (sendAmount.contains(".")) {
            int i = sendAmount.indexOf(".");
            int max = i + 6;
            if (max < sendAmount.length()) {
                return sendAmount.substring(0, max + 1);
            }
        }
        return sendAmount;
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    private void makeToast(String massage) {
        new Thread() {
            public void run() {
                GUESS_THE_COLOR_GAME.runOnUiThread(new Runnable() {
                    public void run() {
                        //Do your UI operations like dialog opening or Toast here
                        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
                        toast.show();
                    }
                });
            }
        }.start();
    }


    private void goThread() {
        Runnable runnable = new GenColorRun();
        Thread thread = new Thread(runnable);
        thread.start();
    }


    @SuppressLint("SetTextI18n")
    private void setBalance() {
        AsyncTask<String, Void, BigDecimal> getBalanceAsync = new GetBalanceAsync().execute("");
        try {
            yourBalance = getBalanceAsync.get();
            balance.setText(yourBalance.toString() + "  XRP");
        } catch (ExecutionException | InterruptedException e) {
            balance.setText("0.000000  XRP");
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    public void updateBalance(BigDecimal bigDecimal) {
        yourBalance = bigDecimal;
        balance.setText(yourBalance.toString() + "  XRP");
    }


    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        GenColorRun.FLAG =  false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        GenColorRun.FLAG =  true;
        goThread();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        GenColorRun.FLAG =  false;
        super.onBackPressed();
    }

}
