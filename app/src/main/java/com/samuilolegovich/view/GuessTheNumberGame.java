package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

import com.samuilolegovich.BaseActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.GameSoundPool;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.viewmodel.GameBetError;
import com.samuilolegovich.viewmodel.GuessNumberViewModel;

import java.util.Locale;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameGuessTheNumber.RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS;
import dagger.hilt.android.AndroidEntryPoint;



/**
 * Экран игры "Угадай число": пользователь выбирает число от 1 до 36 на сетке,
 * делает ставку через {@link GuessNumberViewModel}, и при удачном угадывании получает
 * выигрыш с множителем x35. Поддерживает 4 стиля ввода ставки (чипы, барабан, +/−, слайдер).
 */
@AndroidEntryPoint
public class GuessTheNumberGame extends BaseActivity {
    public static final String GUESS_THE_NUMBER_GAME_CLASS = ".GuessTheNumberGame";

    public static volatile boolean VISIBLE_ON_SCREEN = false;

    private static final String STYLE_CHIPS  = "chips";
    private static final String STYLE_SLIDER = "slider";

    private static final int MAX_BET_TENTHS     = 360; // 36.0 XRP × 10
    private static final int DEFAULT_BET_TENTHS = 10;  // 1.0 XRP

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
    private GameSoundPool soundPool;
    private AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver noisyReceiver;
    private String myReferral;

    private final AudioManager.OnAudioFocusChangeListener focusListener = focusChange -> {
        if (casinoMediaPlayer == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                casinoMediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                casinoMediaPlayer.setVolume(0.5f, 0.5f);
                if (!casinoMediaPlayer.isPlaying() && AudioHelper.isSoundEnabled(this))
                    casinoMediaPlayer.start();
                break;
        }
    };

    private TextView selectedNumView = null;
    private int selectedNumber = 0;

    private TextView                  nameGameTextViewTree;
    private TextView                  nameGameTextViewTwo;
    private TextView                  nameGameTextView;
    private View                      placeBetLinc;
    private View                      rulesInfo;
    private TextView                  balance;
    private ImageView                 placeBetIcon;
    private CircularProgressIndicator placeBetProgress;

    // Bet input — CHIPS style
    private View          styleChipsContainer;
    private TextInputLayout tilBetField;
    private EditText       bet;
    private ChipGroup      chipGroupAmounts;

    // Bet input — SLIDER + +/− (combined)
    private View          styleSliderContainer;
    private Slider        sliderBet;
    private MaterialButton btnBetMinus;
    private MaterialButton btnBetPlus;
    private TextView       tvBetPlusMinus;
    private int            betTenths = DEFAULT_BET_TENTHS;
    private final Handler pmHandler = new Handler(Looper.getMainLooper());
    private Runnable pmRunnable;

    // Shared error for non-chip styles
    private TextView tvBetInputError;



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
            setBettingState(false);
            soundPool.playError(this);
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
            showBetError(msg);
        });

        viewModel.getBetSuccess().observe(this, preparedAmount -> {
            if (preparedAmount == null) return;
            setBettingState(false);
            clearBetError();
            setBetParam(preparedAmount, String.valueOf(selectedNumber));
            resetNumberSelection();
            resetBetInput();
            goToAnotherPage(FLASHER_CLASS);
            showSnackbar(root, BET_IS_MADE_EXPECT_THE_RESULT, SnackbarType.INFO);
        });

        viewModel.loadBalance();
    }



    private void setButtons() {
        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        casinoMediaPlayer.setVolume(0.5f, 0.5f);
        casinoMediaPlayer.setLooping(true);

        soundPool = new GameSoundPool(this);

        nameGameTextViewTree = findViewById(R.id.guess_the_number_game_text_view_tree);
        nameGameTextViewTwo  = findViewById(R.id.guess_the_number_game_text_view_tow);
        nameGameTextView     = findViewById(R.id.guess_the_number_game_text_view);
        rulesInfo            = findViewById(R.id.rules_of_the_game_link);
        balance              = findViewById(R.id.your_balance_xrp_text);
        placeBetLinc         = findViewById(R.id.place_bet_linc);
        placeBetIcon         = findViewById(R.id.place_bet_icon);
        placeBetProgress     = findViewById(R.id.place_bet_progress);

        // Chips style
        styleChipsContainer = findViewById(R.id.style_chips_container);
        tilBetField         = findViewById(R.id.til_bet_field);
        bet                 = findViewById(R.id.bet_field);
        chipGroupAmounts    = findViewById(R.id.chip_group_amounts);

        // Slider + +/− style (combined)
        styleSliderContainer = findViewById(R.id.style_slider_container);
        sliderBet            = findViewById(R.id.slider_bet);
        btnBetMinus          = findViewById(R.id.btn_bet_minus);
        btnBetPlus           = findViewById(R.id.btn_bet_plus);
        tvBetPlusMinus       = findViewById(R.id.tv_bet_plus_minus);

        tvBetInputError = findViewById(R.id.tv_bet_input_error);

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
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND     = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL        = getString(R.string.it_is_not_possible_to_send_null);
        BET_IS_MADE_EXPECT_THE_RESULT          = getString(R.string.bet_is_made_expect_the_result);
        TAG_KNOWLEDGE_CANNOT_BE_MORE           = getString(R.string.tag_knowledge_cannot_be_more);
        PAYMENT_AMOUNT_IS_INCORRECT            = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS              = getString(R.string.wrong_destination_address);
        BET_CANNOT_BE_MORE_THAN                = getString(R.string.bet_cannot_be_more_than);
        BET_CANNOT_BE_LESS_THAN                = getString(R.string.bet_cannot_be_less_than);
        nameGameTextViewTwo.setText(R.string.and_get_36_times_more);
        nameGameTextView.setText(R.string.guess_the_number);
        nameGameTextViewTree.setText(R.string.your_balance);
    }


    private void listeners() {
        chipGroupAmounts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int tenths = 0;
            if      (checkedIds.contains(R.id.chip_01_xrp)) tenths = 1;
            else if (checkedIds.contains(R.id.chip_05_xrp)) tenths = 5;
            else if (checkedIds.contains(R.id.chip_1_xrp))  tenths = 10;
            else if (checkedIds.contains(R.id.chip_5_xrp))  tenths = 50;
            else if (checkedIds.contains(R.id.chip_10_xrp)) tenths = 100;
            else if (checkedIds.contains(R.id.chip_20_xrp)) tenths = 200;
            if (tenths == 0) return;
            String style = preferences.getString(
                    StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
            switch (style) {
                case STYLE_SLIDER:
                    betTenths = Math.min(tenths, MAX_BET_TENTHS);
                    tvBetPlusMinus.setText(formatTenths(betTenths) + " XRP");
                    sliderBet.setValue(Math.min(betTenths / 10.0f, sliderBet.getValueTo()));
                    break;
                default:
                    bet.setText(formatTenths(tenths));
                    break;
            }
            clearBetError();
        });

        bet.addTextChangedListener(new android.text.TextWatcher() {
            private boolean editing = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearBetError();
            }
            @Override public void afterTextChanged(android.text.Editable s) {
                if (editing) return;
                String text = s.toString();
                int dot = text.indexOf('.');
                if (dot >= 0 && text.length() > dot + 2) {
                    editing = true;
                    s.replace(0, s.length(), text.substring(0, dot + 2));
                    editing = false;
                }
            }
        });

        setupPlusMinusButtons();
        setupSliderListener();

        rulesInfo.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(RULES_OF_THE_GAME_GUESS_THE_NUMBER_CLASS);
        });

        placeBetLinc.setOnClickListener(v -> {
            pulse(v);
            setBettingState(true);
            soundPool.playBet(this);
            viewModel.placeBet(getBetAmount(), selectedNumber, myReferral);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPlusMinusButtons() {
        btnBetMinus.setOnClickListener(v -> changeBetBy(-1));
        btnBetPlus.setOnClickListener(v -> changeBetBy(+1));

        btnBetMinus.setOnTouchListener((v, event) -> handlePmTouch(event, -1));
        btnBetPlus.setOnTouchListener((v, event) -> handlePmTouch(event, +1));
    }

    private boolean handlePmTouch(MotionEvent event, int delta) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pmRunnable = new Runnable() {
                @Override public void run() {
                    changeBetBy(delta);
                    pmHandler.postDelayed(this, 100);
                }
            };
            pmHandler.postDelayed(pmRunnable, 400);
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            pmHandler.removeCallbacks(pmRunnable);
        }
        return false;
    }

    private void changeBetBy(int delta) {
        betTenths = Math.max(1, Math.min(MAX_BET_TENTHS, betTenths + delta));
        tvBetPlusMinus.setText(formatTenths(betTenths) + " XRP");
        sliderBet.setValue(Math.max(sliderBet.getValueFrom(), Math.min(sliderBet.getValueTo(), betTenths / 10.0f)));
        clearBetError();
    }

    private void setupSliderListener() {
        sliderBet.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser) return;
            betTenths = Math.round(value * 10);
            tvBetPlusMinus.setText(formatTenths(betTenths) + " XRP");
            clearBetError();
        });
    }


    private void applyBetInputStyle() {
        String style = preferences.getString(
                StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);

        styleChipsContainer.setVisibility(STYLE_CHIPS.equals(style) ? View.VISIBLE : View.GONE);
        styleSliderContainer.setVisibility(STYLE_SLIDER.equals(style) ? View.VISIBLE : View.GONE);
    }


    private String getBetAmount() {
        String style = preferences.getString(
                StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
        switch (style) {
            case STYLE_SLIDER: return formatTenths(betTenths);
            default:           return bet.getText().toString();
        }
    }

    private String formatTenths(int tenths) {
        if (tenths % 10 == 0) return String.valueOf(tenths / 10);
        return String.format(Locale.US, "%.1f", tenths / 10.0);
    }


    private void showBetError(String msg) {
        String style = preferences.getString(
                StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
        if (STYLE_CHIPS.equals(style)) {
            tilBetField.setError(msg);
        } else {
            tilBetField.setError(null);
            tvBetInputError.setText(msg);
            tvBetInputError.setVisibility(View.VISIBLE);
        }
    }

    private void clearBetError() {
        tilBetField.setError(null);
        if (tvBetInputError != null) tvBetInputError.setVisibility(View.GONE);
    }

    private void resetBetInput() {
        String style = preferences.getString(
                StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
        switch (style) {
            case STYLE_SLIDER:
                betTenths = DEFAULT_BET_TENTHS;
                tvBetPlusMinus.setText(formatTenths(DEFAULT_BET_TENTHS) + " XRP");
                sliderBet.setValue(DEFAULT_BET_TENTHS / 10.0f);
                break;
            default:
                bet.setText("");
                break;
        }
        chipGroupAmounts.clearCheck();
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
        Flasher.TEST_MODE_ENUM   = TestModeEnum.GUESS_THE_NUMBER_GAME;
        Flasher.COLOR_BET        = Lotto.getRandomColorForNumber(tag);
        Flasher.TEST_SAND_AMOUNT = amount;
        Flasher.NUMBER_BET       = tag;
    }


    private void setBettingState(boolean betting) {
        runOnUiThread(() -> {
            placeBetLinc.setEnabled(!betting);
            placeBetLinc.setAlpha(betting ? 0.7f : 1f);
            placeBetIcon.setVisibility(betting ? View.GONE : View.VISIBLE);
            placeBetProgress.setVisibility(betting ? View.VISIBLE : View.GONE);
        });
    }

    private void goToAnotherPage(String namePage) {
        startActivity(new Intent(namePage));
    }


    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        pmHandler.removeCallbacksAndMessages(null);
        if (casinoMediaPlayer != null && casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        audioFocusRequest = null;
        noisyReceiver = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        preferences = PrefsHelper.get(this);
        applyBetInputStyle();
        viewModel.loadBalance();
        noisyReceiver = AudioHelper.registerNoisyReceiver(this,
                () -> { if (casinoMediaPlayer != null && casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause(); });
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);
        if (casinoMediaPlayer != null && AudioHelper.isSoundEnabled(this)) casinoMediaPlayer.start();
    }

    @Override
    public void onBackPressed() {
        if (casinoMediaPlayer != null) casinoMediaPlayer.stop();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pmHandler.removeCallbacksAndMessages(null);
        if (casinoMediaPlayer != null) { casinoMediaPlayer.release(); casinoMediaPlayer = null; }
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }
}
