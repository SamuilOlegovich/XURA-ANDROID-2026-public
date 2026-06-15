package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import androidx.lifecycle.ViewModelProvider;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.runnable.GenColorRun;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.viewmodel.GameBetError;
import com.samuilolegovich.viewmodel.GuessColorViewModel;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameGuessTheColor.RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class GuessTheColorGame extends BaseActivity {
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

    // Сохраняем цвет ставки до получения ответа от ViewModel
    private boolean pendingColor;
    private boolean lastBetWasRed = true;
    private String myReferral;

    private TextView                  nameGameTextViewTwo;
    private TextView                  yourBalanceTextView;
    private View                      rulesOfTheGameLink;
    private TextView                  nameGameTextView;
    private TextView                  balance;
    private View                      black;
    private View                      red;
    private EditText                  bet;
    private ChipGroup                 chipGroupAmounts;
    private TextInputLayout           tilBetField;
    private ImageView                 redIcon;
    private ImageView                 blackIcon;
    private CircularProgressIndicator redProgress;
    private CircularProgressIndicator blackProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guess_the_color_game_page);

        viewModel = new ViewModelProvider(this).get(GuessColorViewModel.class);
        View root = findViewById(android.R.id.content);

        setButtons();
        setLanguage();
        getReferral();
        listeners();

        viewModel.getBalance().observe(this, b ->
                balance.setText(b.stripTrailingZeros().toPlainString() + "  XRP"));

        viewModel.getError().observe(this, error -> {
            if (error == null) return;
            setBettingState(false);
            errorMediaPlayer.start();
            String msg;
            switch (error) {
                case INVALID_AMOUNT:       msg = PAYMENT_AMOUNT_IS_INCORRECT; break;
                case AMOUNT_IS_ZERO:       msg = IT_IS_NOT_POSSIBLE_TO_SEND_NULL; break;
                case INSUFFICIENT_BALANCE: msg = YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND; break;
                case BET_TOO_HIGH:         msg = BET_CANNOT_BE_MORE_THAN + StringEnum.MAX_BET_GUESS_THE_COLOR.getValue() + " XRP"; break;
                case BET_TOO_LOW:          msg = BET_CANNOT_BE_LESS_THAN + StringEnum.MIN_BET_GUESS_THE_COLOR.getValue() + " XRP"; break;
                case TAG_TOO_LARGE:        msg = TAG_KNOWLEDGE_CANNOT_BE_MORE; break;
                default:                   msg = WRONG_DESTINATION_ADDRESS; break;
            }
            tilBetField.setError(msg);
        });

        viewModel.getBetSuccess().observe(this, preparedAmount -> {
            if (preparedAmount == null) return;
            setBettingState(false);
            tilBetField.setError(null);
            bet.setText("");
            setBetParam(preparedAmount, pendingColor);
            goToAnotherPage(FLASHER_CLASS);
            showSnackbar(root, BET_IS_MADE_EXPECT_THE_RESULT, SnackbarType.INFO);
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
        tilBetField = findViewById(R.id.til_bet_field);
        chipGroupAmounts = findViewById(R.id.chip_group_amounts);
        redIcon = findViewById(R.id.red_icon);
        blackIcon = findViewById(R.id.black_icon);
        redProgress = findViewById(R.id.red_progress);
        blackProgress = findViewById(R.id.black_progress);

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

        yourBalanceTextView.setText(R.string.your_balance);
        nameGameTextView.setText(R.string.guess_the_color);
    }


    private void listeners() {
        chipGroupAmounts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if      (checkedIds.contains(R.id.chip_01_xrp)) bet.setText("0.1");
            else if (checkedIds.contains(R.id.chip_05_xrp)) bet.setText("0.5");
            else if (checkedIds.contains(R.id.chip_1_xrp))  bet.setText("1");
            else if (checkedIds.contains(R.id.chip_5_xrp))  bet.setText("5");
            else if (checkedIds.contains(R.id.chip_10_xrp)) bet.setText("10");
            else if (checkedIds.contains(R.id.chip_20_xrp)) bet.setText("20");
        });

        bet.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilBetField.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        rulesOfTheGameLink.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS);
        });


        black.setOnClickListener(v -> {
            pulse(v);
            lastBetWasRed = false;
            setBettingState(true);
            betMediaPlayer.start();
            pendingColor = true;
            Flasher.COLOR_BET = true;
            viewModel.placeBet(
                    bet.getText().toString(),
                    StringEnum.TAG_BLACK_GUESS_THE_NUMBER.getValue(),
                    myReferral);
        });

        red.setOnClickListener(v -> {
            pulse(v);
            lastBetWasRed = true;
            setBettingState(true);
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
        preferences = PrefsHelper.get(this);
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


    private void setBettingState(boolean betting) {
        runOnUiThread(() -> {
            red.setEnabled(!betting);
            black.setEnabled(!betting);
            red.setAlpha(betting ? 0.7f : 1f);
            black.setAlpha(betting ? 0.7f : 1f);
            if (lastBetWasRed) {
                redIcon.setVisibility(betting ? View.GONE : View.VISIBLE);
                redProgress.setVisibility(betting ? View.VISIBLE : View.GONE);
            } else {
                blackIcon.setVisibility(betting ? View.GONE : View.VISIBLE);
                blackProgress.setVisibility(betting ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void goThread() {
        AppExecutors.io().execute(new GenColorRun());
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
        viewModel.loadBalance();
        goThread();
    }

    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        GenColorRun.FLAG = false;
        super.onBackPressed();
    }
}