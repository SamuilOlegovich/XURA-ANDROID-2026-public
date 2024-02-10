package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.samuilolegovich.asyncAndRun.runnable.GenNumberRun;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Lotto;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameGuessTheNumber.RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;



public class GuessTheNumberGame extends AppCompatActivity {
    public static final String GUESS_THE_NUMBER_GAME_CLASS = ".GuessTheNumberGame";

    private String GUESSED_NUMBER_SHOULD_NOT_BE_LESS_THAN;
    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String BET_IS_MADE_EXPECT_THE_RESULT;
    private String TAG_KNOWLEDGE_CANNOT_BE_MORE;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String BET_CANNOT_BE_MORE_THAN;
    private String BET_CANNOT_BE_LESS_THAN;

    @SuppressLint("StaticFieldLeak")
    public static volatile GuessTheNumberGame GUESS_THE_NUMBER_GAME;
    public static volatile boolean VISIBLE_ON_SCREEN = false;

    private SharedPreferences preferences;
    private MediaPlayer casinoMediaPlayer;
    private MediaPlayer errorMediaPlayer;
    private MediaPlayer betMediaPlayer;
    private Animation animTranslate;
    private BigDecimal yourBalance;
    private String myReferral;

    private TextView nameGameTextViewTree;
    private TextView nameGameTextViewTwo;
    private TextView nameGameTextView;
    private TextView placeBetLinc;
    private EditText betNumber;
    private TextView rulesInfo;
    private TextView balance;
    private TextView outInfo;
    private EditText bet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.guess_the_number_game_page);
        setButtons();
        setLanguage();
        listeners();
        setBalance();
        getReferral();
        GUESS_THE_NUMBER_GAME = this;
        goThread();
    }



    private void soundPlay(MediaPlayer mediaPlayer) {
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }


    private void setButtons() {
        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        errorMediaPlayer = MediaPlayer.create(this, R.raw.error);
        betMediaPlayer = MediaPlayer.create(this, R.raw.bet);

        nameGameTextViewTree = (TextView) findViewById(R.id.guess_the_number_game_text_view_tree);
        nameGameTextViewTwo = (TextView) findViewById(R.id.guess_the_number_game_text_view_tow);
        nameGameTextView = (TextView) findViewById(R.id.guess_the_number_game_text_view);
        rulesInfo = (TextView) findViewById(R.id.rules_of_the_game_link);
        balance = (TextView) findViewById(R.id.your_balance_xrp_text);
        placeBetLinc = (TextView) findViewById(R.id.place_bet_linc);
        betNumber = (EditText) findViewById(R.id.bet_number_field);
        outInfo = (TextView) findViewById(R.id.number_info_text);
        bet = (EditText) findViewById(R.id.bet_field);

        soundPlay(casinoMediaPlayer);
    }


    private void setLanguage() {
        GUESSED_NUMBER_SHOULD_NOT_BE_LESS_THAN = getString(R.string.guessed_number_should_not_be_less_than);
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL = getString(R.string.it_is_not_possible_to_send_null);
        BET_IS_MADE_EXPECT_THE_RESULT = getString(R.string.bet_is_made_expect_the_result);
        TAG_KNOWLEDGE_CANNOT_BE_MORE = getString(R.string.tag_knowledge_cannot_be_more);
        PAYMENT_AMOUNT_IS_INCORRECT = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS = getString(R.string.wrong_destination_address);
        BET_CANNOT_BE_MORE_THAN = getString(R.string.bet_cannot_be_more_than);
        BET_CANNOT_BE_LESS_THAN = getString(R.string.bet_cannot_be_less_than);
        nameGameTextViewTwo.setText(R.string.and_get_a_hundred_times_more);
        nameGameTextView.setText(R.string.guess_the_number);
        nameGameTextViewTree.setText(R.string.your_balance);
        rulesInfo.setText(R.string.rules_of_the_game);
        placeBetLinc.setText(R.string.place_bet_linc);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        rulesInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        goToAnotherPage(RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS);
                    }
                }
        );

        placeBetLinc.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        betMediaPlayer.start();
                        makeStackThread();
                    }
                }
        );
    }


    private void makeStackThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeStack();
            }
        }).start();
    }


    private void makeStack() {
        String sendAmount = prepareTheShippingAmount(bet.getText().toString());
        String tag = betNumber.getText().toString();
        String tegNumber = testNumber(tag);

        if (myReferral == null) {
            myReferral = "0";
        }

        if (tegNumber != null && checkData(sendAmount, tegNumber + myReferral)) {
            betNumber.setText("");
            bet.setText("");
            makeToast(BET_IS_MADE_EXPECT_THE_RESULT);
            setBetParam(tag, true);
            goToAnotherPage(FLASHER_CLASS);
        } else {
            errorMediaPlayer.start();
            makeToast(GUESSED_NUMBER_SHOULD_NOT_BE_LESS_THAN);
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


    private void setBetParam(String tag, boolean color) {
        boolean b = Lotto.getRandomColorForNumber(tag);
        Flasher.COLOR_BET = Lotto.getRandomColorForNumber(tag);
        Flasher.NUMBER_BET = tag;
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

        if (new BigDecimal(sendAmount).compareTo(yourBalance) > 0) {
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


//    private boolean checkData(String sendAmount, String sendTeg) {
//        if (sendAmount == null) {
//            errorMediaPlayer.start();
//            makeToast("PAYMENT AMOUNT IS INCORRECT");
//            return false;
//        }
//        if (new BigDecimal(sendAmount).compareTo(yourBalance) > 0) {
//            errorMediaPlayer.start();
//            makeToast("YOUR ACCOUNT IS NOT ENOUGH TO SEND");
//            return false;
//        }
//        if (sendTeg != null && !sendTeg.equals("") && Long.parseLong(sendTeg) >= Integer.MAX_VALUE) {
//            errorMediaPlayer.start();
//            makeToast("TAG KNOWLEDGE CANNOT BE MORE - 2147483647");
//            return false;
//        }
//        return makePayment(sendAmount, sendTeg);
//    }


    private boolean makePayment(String sendAmount, String sendTeg) {
        AsyncTask<String, Void, Boolean> asyncTask = new SendPaymentAsync().execute(
                        StringEnum.SERVER_ADDRESS_GUESS_THE_NUMBER.getValue(), sendAmount, sendTeg);
        boolean b = false;

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
    }


    public void setColorAndText(String text, boolean b) {
        // goToAnotherPage
        new Thread() {
            public void run() {
                GUESS_THE_NUMBER_GAME.runOnUiThread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    public void run() {
                        if (b) {
                            outInfo.setText(text);
                            outInfo.setTextColor(Color.BLACK);
                        } else if (text.equalsIgnoreCase("00"))  {
                            outInfo.setText(text);
                            outInfo.setTextColor(Color.parseColor("#007143"));
                        } else {
                            outInfo.setText(text);
                            outInfo.setTextColor(Color.RED);
                        }
                    }
                });
            }
        }.start();
    }


    private void getReferral() {
        // tag 214 referral 7483647
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);

        if (preferences.contains(StringEnum.APP_PREFERENCES_REFERRAL.getValue())) {
            myReferral = preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "");
        } else {
            myReferral = "0";
        }
    }


    private String testNumber(String number) {
        if (!number.equals("")
                && number.length() > 0
                && number.length() < 3
                && !number.startsWith("0")) {
            long inTag = Long.parseLong(number);
            int min = Integer.parseInt(StringEnum.MIN_BET_GUESS_THE_NUMBER.getValue());
            int max = Integer.parseInt(StringEnum.MAX_BET_GUESS_THE_NUMBER.getValue());

            if (inTag >= min && inTag <= max) {
                long result = 100 + inTag;
                return result + "";
            }
        }

        return null;
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    private void makeToast(String massage) {
        new Thread() {
            public void run() {
                GUESS_THE_NUMBER_GAME.runOnUiThread(new Runnable() {
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


    @SuppressLint("SetTextI18n")
    private void goText(String s) {
        outInfo.setText(s);
    }


    @SuppressLint("SetTextI18n")
    private void setBalance() {
        AsyncTask<String, Void, BigDecimal> getBalanceAsync = new GetBalanceAsync().execute("");

        try {
            yourBalance = getBalanceAsync.get();
            balance.setText(yourBalance.toString() + "  XRP");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    public void updateBalance(BigDecimal bigDecimal) {
        yourBalance = bigDecimal;
        balance.setText(yourBalance.toString() + "  XRP");
    }


    private void goThread(){
        Runnable runnable = new GenNumberRun();
        Thread thread = new Thread(runnable);
        thread.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        GenNumberRun.FLAG =  false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        GenNumberRun.FLAG =  true;
        goThread();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        GenNumberRun.FLAG =  false;
        super.onBackPressed();
    }

}
