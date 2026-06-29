package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

import com.samuilolegovich.BaseActivity;
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
import com.samuilolegovich.viewmodel.GuessColorViewModel;

import java.util.Locale;

import static com.samuilolegovich.view.Flasher.FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameGuessTheColor.RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS;
import dagger.hilt.android.AndroidEntryPoint;



/**
 * Экран игры "Угадай цвет": пользователь делает ставку на чёрный или красный, сумма
 * проверяется и отправляется через {@link GuessColorViewModel}, а результат розыгрыша
 * показывается на экране {@link Flasher}. Поддерживает 4 стиля ввода ставки:
 * чипы, барабан, +/−, слайдер — выбирается в настройках.
 */
@AndroidEntryPoint
public class GuessTheColorGame extends BaseActivity {
    public static final String GUESS_THE_COLOR_GAME_CLASS = ".GuessTheColorGame";

    public static volatile boolean VISIBLE_ON_SCREEN = false;

    private static final String STYLE_CHIPS  = "chips";
    private static final String STYLE_SLIDER = "slider";

    private static final int MAX_BET_TENTHS = 1000; // 100.0 XRP × 10
    private static final int DEFAULT_BET_TENTHS = 10; // 1.0 XRP

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
    private GameSoundPool soundPool;
    private AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver noisyReceiver;

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

    private boolean pendingColor;
    private boolean lastBetWasRed = true;
    private String myReferral;

    // Core UI
    private TextView                  nameGameTextViewTwo;
    private TextView                  yourBalanceTextView;
    private View                      rulesOfTheGameLink;
    private TextView                  nameGameTextView;
    private TextView                  balance;
    private View                      black;
    private View                      red;
    private ImageView                 redIcon;
    private ImageView                 blackIcon;
    private CircularProgressIndicator redProgress;
    private CircularProgressIndicator blackProgress;

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
            soundPool.playError(this);
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
            showBetError(msg);
        });

        viewModel.getBetSuccess().observe(this, preparedAmount -> {
            if (preparedAmount == null) return;
            setBettingState(false);
            clearBetError();
            resetBetInput();
            setBetParam(preparedAmount, pendingColor);
            goToAnotherPage(FLASHER_CLASS);
            showSnackbar(root, BET_IS_MADE_EXPECT_THE_RESULT, SnackbarType.INFO);
        });

        viewModel.loadBalance();
    }



    private void setButtons() {
        nameGameTextViewTwo = findViewById(R.id.name_game_text_view_tow);
        yourBalanceTextView = findViewById(R.id.your_balance_text_view);
        rulesOfTheGameLink  = findViewById(R.id.rules_of_the_game_link);
        nameGameTextView    = findViewById(R.id.name_game_text_view);
        balance             = findViewById(R.id.your_balance_xrp_text);
        black               = findViewById(R.id.color_black);
        red                 = findViewById(R.id.color_red);
        redIcon             = findViewById(R.id.red_icon);
        blackIcon           = findViewById(R.id.black_icon);
        redProgress         = findViewById(R.id.red_progress);
        blackProgress       = findViewById(R.id.black_progress);

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

        casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        casinoMediaPlayer.setVolume(0.5f, 0.5f);
        casinoMediaPlayer.setLooping(true);

        soundPool = new GameSoundPool(this);
    }


    private void setLanguage() {
        YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND = getString(R.string.your_account_is_not_enough_to_send);
        IT_IS_NOT_POSSIBLE_TO_SEND_NULL    = getString(R.string.it_is_not_possible_to_send_null);
        BET_IS_MADE_EXPECT_THE_RESULT      = getString(R.string.bet_is_made_expect_the_result);
        TAG_KNOWLEDGE_CANNOT_BE_MORE       = getString(R.string.tag_knowledge_cannot_be_more);
        PAYMENT_AMOUNT_IS_INCORRECT        = getString(R.string.payment_amount_is_incorrect);
        WRONG_DESTINATION_ADDRESS          = getString(R.string.wrong_destination_address);
        BET_CANNOT_BE_MORE_THAN            = getString(R.string.bet_cannot_be_more_than);
        BET_CANNOT_BE_LESS_THAN            = getString(R.string.bet_cannot_be_less_than);
        nameGameTextViewTwo.setText(R.string.and_double_the_bet);
        yourBalanceTextView.setText(R.string.your_balance);
        nameGameTextView.setText(R.string.guess_the_color);
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

        rulesOfTheGameLink.setOnClickListener(v -> {
            pulse(v);
            goToAnotherPage(RULES_OF_THE_GAME_GUESS_THE_COLOR_CLASS);
        });

        black.setOnClickListener(v -> {
            pulse(v);
            lastBetWasRed = false;
            setBettingState(true);
            soundPool.playBet(this);
            pendingColor = true;
            Flasher.COLOR_BET = true;
            viewModel.placeBet(getBetAmount(), StringEnum.TAG_BLACK_GUESS_THE_NUMBER.getValue(), myReferral);
        });

        red.setOnClickListener(v -> {
            pulse(v);
            lastBetWasRed = true;
            setBettingState(true);
            soundPool.playBet(this);
            pendingColor = false;
            Flasher.COLOR_BET = false;
            viewModel.placeBet(getBetAmount(), StringEnum.TAG_RED_GUESS_THE_COLOR.getValue(), myReferral);
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
