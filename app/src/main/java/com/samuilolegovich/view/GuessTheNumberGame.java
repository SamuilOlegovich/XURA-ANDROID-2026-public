package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.runnable.GenNumberRun;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.viewmodel.GameBetError;
import com.samuilolegovich.viewmodel.GuessNumberViewModel;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameGuessTheNumber.RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




@AndroidEntryPoint
public class GuessTheNumberGame extends BaseActivity {
    public static final String GUESS_THE_NUMBER_GAME_CLASS = ".GuessTheNumberGame";

    public static volatile boolean VISIBLE_ON_SCREEN = false;

    private String GUESSED_NUMBER_SHOULD_NOT_BE_LESS_THAN;
    private String YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND;
    private String IT_IS_NOT_POSSIBLE_TO_SEND_NULL;
    private String BET_IS_MADE_EXPECT_THE_RESULT;
    private String TAG_KNOWLEDGE_CANNOT_BE_MORE;
    private String PAYMENT_AMOUNT_IS_INCORRECT;
    private String WRONG_DESTINATION_ADDRESS;
    private String BET_CANNOT_BE_MORE_THAN;
    private String BET_CANNOT_BE_LESS_THAN;

    private GuessNumberViewModel viewModel;
    private SharedPreferences preferences;
    private MediaPlayer casinoMediaPlayer;
    private MediaPlayer errorMediaPlayer;
    private MediaPlayer betMediaPlayer;
    private String myReferral;

    // Выбранная цифра — сохраняем до получения ответа от ViewModel
    private TextView selectedNumView = null;
    private int selectedNumber = 0;

    private TextView nameGameTextViewTree;
    private TextView nameGameTextViewTwo;
    private TextView nameGameTextView;
    private View placeBetLinc;
    private TextView rulesInfo;
    private TextView balance;
    private EditText bet;
    private ChipGroup chipGroupAmounts;
    private TextInputLayout tilBetField;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guess_the_number_game_page);

        viewModel = new ViewModelProvider(this).get(GuessNumberViewModel.class);
        View root = findViewById(android.R.id.content);

        setButtons();
        setLanguage();
        getReferral();
        listeners();

        viewModel.getBalance().observe(this, b ->
                balance.setText(b.stripTrailingZeros().toPlainString() + "  XRP"));

        viewModel.getError().observe(this, error -> {
            if (error == null) return;
            errorMediaPlayer.start();
            String msg;
            switch (error) {
                case NO_NUMBER_SELECTED:   msg = GUESSED_NUMBER_SHOULD_NOT_BE_LESS_THAN; break;
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
            tilBetField.setError(null);
            resetNumberSelection();
            bet.setText("");
            setBetParam(preparedAmount, String.valueOf(selectedNumber));
            goToAnotherPage(FLASHER_CLASS);
            showSnackbar(root, BET_IS_MADE_EXPECT_THE_RESULT, SnackbarType.INFO);
        });

        viewModel.loadBalance();
        goThread();
    }



    private void setButtons() {
        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        errorMediaPlayer = MediaPlayer.create(this, R.raw.error);
        betMediaPlayer = MediaPlayer.create(this, R.raw.bet);

        nameGameTextViewTree = findViewById(R.id.guess_the_number_game_text_view_tree);
        nameGameTextViewTwo = findViewById(R.id.guess_the_number_game_text_view_tow);
        nameGameTextView = findViewById(R.id.guess_the_number_game_text_view);
        rulesInfo = findViewById(R.id.rules_of_the_game_link);
        balance = findViewById(R.id.your_balance_xrp_text);
        placeBetLinc = findViewById(R.id.place_bet_linc);
        bet = findViewById(R.id.bet_field);
        tilBetField = findViewById(R.id.til_bet_field);
        chipGroupAmounts = findViewById(R.id.chip_group_amounts);

        casinoMediaPlayer.setVolume(0.5f, 0.5f);
        casinoMediaPlayer.setLooping(true);
        casinoMediaPlayer.start();

        setupNumberGrid();
    }


    private void setupNumberGrid() {
        GridLayout grid = findViewById(R.id.numbers_grid);
        int cellH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        Typeface font = ResourcesCompat.getFont(this, R.font.montserrat);

        for (int i = 1; i <= 36; i++) {
            final int num = i;
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(i));
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(getResources().getColor(R.color.xura_text_primary, null));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tv.setTypeface(font, Typeface.BOLD);
            tv.setBackground(getDrawable(R.drawable.bg_num_button));

            GridLayout.LayoutParams p = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
            p.width = 0;
            p.height = cellH;
            p.setMargins(3, 3, 3, 3);
            tv.setLayoutParams(p);

            tv.setOnClickListener(v -> {
                if (selectedNumView != null) {
                    selectedNumView.setBackground(getDrawable(R.drawable.bg_num_button));
                }
                tv.setBackground(getDrawable(R.drawable.bg_button_primary));
                selectedNumView = tv;
                selectedNumber = num;
            });

            grid.addView(tv);
        }
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
        nameGameTextViewTwo.setText(R.string.and_get_36_times_more);
        nameGameTextView.setText(R.string.guess_the_number);
        nameGameTextViewTree.setText(R.string.your_balance);
        rulesInfo.setText(R.string.rules_of_the_game);
    }


    private void listeners() {
        chipGroupAmounts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_1_xrp))  bet.setText("1");
            else if (checkedIds.contains(R.id.chip_5_xrp))  bet.setText("5");
            else if (checkedIds.contains(R.id.chip_10_xrp)) bet.setText("10");
        });

        bet.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilBetField.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        rulesInfo.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS);
        });

        placeBetLinc.setOnClickListener(v -> {
            pulse(v);
            betMediaPlayer.start();
            viewModel.placeBet(bet.getText().toString(), selectedNumber, myReferral);
        });
    }


    private void getReferral() {
        preferences = PrefsHelper.get(this);
        myReferral = preferences.contains(StringEnum.APP_PREFERENCES_REFERRAL.getValue())
                ? preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "0")
                : "0";
    }


    private void resetNumberSelection() {
        if (selectedNumView != null) {
            selectedNumView.setBackground(getDrawable(R.drawable.bg_num_button));
            selectedNumView = null;
        }
        selectedNumber = 0;
    }


    @SuppressLint("SetTextI18n")
    private void setBetParam(String amount, String tag) {
        Flasher.TEST_MODE_ENUM = TestModeEnum.GUESS_THE_NUMBER_GAME;
        Flasher.COLOR_BET = Lotto.getRandomColorForNumber(tag);
        Flasher.TEST_SAND_AMOUNT = amount;
        Flasher.NUMBER_BET = tag;
    }


    private void goThread() {
        AppExecutors.io().execute(new GenNumberRun());
    }


    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        GenNumberRun.FLAG = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        GenNumberRun.FLAG = true;
        goThread();
    }

    @Override
    public void onBackPressed() {
        casinoMediaPlayer.stop();
        GenNumberRun.FLAG = false;
        super.onBackPressed();
    }
}