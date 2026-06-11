package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.runnable.GenColorRun;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.viewmodel.GameBetError;
import com.samuilolegovich.viewmodel.GuessColorViewModel;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameGuessTheColor.RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS;



public class GuessTheColorGame extends AppCompatActivity {
    public static final String GUESS_THE_COLOR_GAME_CLASS = ".GuessTheColorGame";

    public static volatile boolean VISIBLE_ON_SCREEN = false;

    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String BET_IS_MADE_EXPECT_THE_RESULT;
    private String TAG_KNOWLEDGE_CANNOT_BE_MORE;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String BET_CANNOT_BE_MORE_THAN;
    private String BET_CANNOT_BE_LESS_THAN;

    private GuessColorViewModel viewModel;
    private SharedPreferences preferences;
    private MediaPlayer casinoMediaPlayer;
    private MediaPlayer errorMediaPlayer;
    private MediaPlayer betMediaPlayer;
    private Animation animTranslate;

    // Сохраняем цвет ставки до получения ответа от ViewModel
    private boolean pendingColor;
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

        viewModel = new ViewModelProvider(this).get(GuessColorViewModel.class);

        setButtons();
        setLanguage();
        getReferral();
        listeners();

        viewModel.getBalance().observe(this, b ->
                balance.setText(b.toString() + "  XRP"));

        viewModel.getError().observe(this, error -> {
            if (error == null) return;
            errorMediaPlayer.start();
            switch (error) {
                case INVALID_AMOUNT:       showToast(PAYMENT_AMOUNT_IS_INCORRECT); break;
                case AMOUNT_IS_ZERO:       showToast(IT_IS_NOT_POSSIBLE_TO_SEND_NULL); break;
                case INSUFFICIENT_BALANCE: showToast(YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND); break;
                case BET_TOO_HIGH:         showToast(BET_CANNOT_BE_MORE_THAN + StringEnum.MAX_BET_GUESS_THE_COLOR.getValue() + " XRP"); break;
                case BET_TOO_LOW:          showToast(BET_CANNOT_BE_LESS_THAN + StringEnum.MIN_BET_GUESS_THE_COLOR.getValue() + " XRP"); break;
                case TAG_TOO_LARGE:        showToast(TAG_KNOWLEDGE_CANNOT_BE_MORE); break;
                default:                   showToast(WRONG_DESTINATION_ADDRESS); break;
            }
        });

        viewModel.getBetSuccess().observe(this, preparedAmount -> {
            if (preparedAmount == null) return;
            bet.setText("");
            setBetParam(preparedAmount, pendingColor);
            goToAnotherPage(FLASHER_CLASS);
            showToast(BET_IS_MADE_EXPECT_THE_RESULT);
        });

        viewModel.loadBalance();
        goThread();
    }



    private void setButtons() {
        nameGameTextViewTwo = findViewById(R.id.name_game_text_view_tow);
        yourBalanceTextView = findViewById(R.id.your_balance_text_view);
        rulesOfTheGameLink = findViewById(R.id.rules_of_the_game_link);
        nameGameTextView = findViewById(R.id.name_game_text_view);
        balance = findViewById(R.id.your_balance_xrp_text);
        black = findViewById(R.id.color_black);
        red = findViewById(R.id.color_red);
        bet = findViewById(R.id.bet_field);

        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        errorMediaPlayer = MediaPlayer.create(this, R.raw.error);
        betMediaPlayer = MediaPlayer.create(this, R.raw.bet);

        casinoMediaPlayer.setVolume(0.5f, 0.5f);
        casinoMediaPlayer.setLooping(true);
        casinoMediaPlayer.start();
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

        rulesOfTheGameLink.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            goToAnotherPage(RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS);
        });

        black.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            betMediaPlayer.start();
            pendingColor = true;
            Flasher.COLOR_BET = true;
            viewModel.placeBet(
                    bet.getText().toString(),
                    StringEnum.TAG_BLACK_GUESS_THE_NUMBER.getValue(),
                    myReferral);
        });

        red.setOnClickListener(v -> {
            v.startAnimation(animTranslate);
            betMediaPlayer.start();
            pendingColor = false;
            Flasher.COLOR_BET = false;
            viewModel.placeBet(
                    bet.getText().toString(),
                    StringEnum.TAG_RED_GUESS_THE_COLOR.getValue(),
                    myReferral);
        });
    }


    private void getReferral() {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(), Context.MODE_PRIVATE);
        myReferral = preferences.contains(StringEnum.APP_PREFERENCES_REFERRAL.getValue())
                ? preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "0")
                : "0";
    }


    @SuppressLint("SetTextI18n")
    private void setBetParam(String amount, boolean color) {
        Flasher.NUMBER_BET = Lotto.getRandomNumberForColor(color) + "";
        Flasher.TEST_MODE_ENUM = TestModeEnum.GUESS_THE_COLOR_GAME;
        Flasher.TEST_SAND_AMOUNT = amount;
        Flasher.COLOR_BET = color;
    }


    private void showToast(String message) {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 110);
            toast.show();
        });
    }


    private void goThread() {
        new Thread(new GenColorRun()).start();
    }


    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        GenColorRun.FLAG = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        GenColorRun.FLAG = true;
        goThread();
    }

    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        GenColorRun.FLAG = false;
        super.onBackPressed();
    }
}