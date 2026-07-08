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
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.BetInputFilter;
import com.samuilolegovich.utils.GameSoundPool;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.viewmodel.GameBetError;
import com.samuilolegovich.viewmodel.SlotViewModel;

import java.util.Locale;

import static com.samuilolegovich.view.SlotFlasher.SLOT_FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameSlot.RULES_SLOT_CLASS;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SlotGame extends BaseActivity {
    public static final String SLOT_GAME_CLASS = ".SlotGame";

    private static final String STYLE_CHIPS  = "chips";
    private static final String STYLE_SLIDER = "slider";

    private static final int MAX_BET_TENTHS     = 1000; // 100.0 XRP × 10
    private static final int DEFAULT_BET_TENTHS = 10;   // 1.0 XRP

    private static final int[] REEL_ORDER_LEFT   = {0, 2, 4, 1, 5, 3, 6};
    private static final int[] REEL_ORDER_CENTER = {3, 0, 5, 2, 6, 1, 4};
    private static final int[] REEL_ORDER_RIGHT  = {5, 1, 3, 6, 0, 4, 2};

    private SlotViewModel viewModel;
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

    // UI
    private TextView       tvBalance;
    private View           btnSpin;
    private View           tvRulesLink;
    private View           styleChipsContainer;
    private View           styleSliderContainer;
    private TextInputLayout tilBetField;
    private EditText        etBet;
    private TextView        tvBetPlusMinus;
    private MaterialButton  btnBetMinus;
    private MaterialButton  btnBetPlus;
    private Slider          sliderBet;
    private ChipGroup       chipGroup;
    private TextView        tvBetInputError;

    private ImageView spinIcon;
    private View      spinProgress;

    private SlotReelView reelPreviewLeft;
    private SlotReelView reelPreviewCenter;
    private SlotReelView reelPreviewRight;

    private int betTenths = DEFAULT_BET_TENTHS;
    private final Handler  pmHandler = new Handler(Looper.getMainLooper());
    private Runnable pmRunnable;

    private String ERR_ZERO;
    private String ERR_TOO_LOW;
    private String ERR_TOO_HIGH;
    private String ERR_BALANCE;
    private String ERR_INVALID;
    private String ERR_PAYMENT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slot_game_page);

        viewModel   = new ViewModelProvider(this).get(SlotViewModel.class);
        preferences = PrefsHelper.get(this);

        bindViews();
        setLanguage();
        loadReferral();
        setupPlusMinusButtons();
        setupSliderListener();
        setupBetFieldWatcher();
        setupChips();
        setupListeners();
        setupObservers();
        setupBottomNav();

        tvBetPlusMinus.setText(formatTenths(betTenths) + " XRP");
        viewModel.loadBalance();
    }


    private void bindViews() {
        tvBalance            = findViewById(R.id.tv_balance);
        btnSpin              = findViewById(R.id.btn_spin);
        tvRulesLink          = findViewById(R.id.tv_rules_link);
        styleChipsContainer  = findViewById(R.id.style_chips_container);
        styleSliderContainer = findViewById(R.id.style_slider_container);
        tilBetField          = findViewById(R.id.til_bet_field);
        etBet                = findViewById(R.id.et_bet);
        tvBetPlusMinus       = findViewById(R.id.tv_bet_plus_minus);
        btnBetMinus          = findViewById(R.id.btn_bet_minus);
        btnBetPlus           = findViewById(R.id.btn_bet_plus);
        sliderBet            = findViewById(R.id.slider_bet);
        chipGroup            = findViewById(R.id.chip_group_amounts);
        tvBetInputError      = findViewById(R.id.tv_bet_input_error);

        spinIcon     = findViewById(R.id.spin_icon);
        spinProgress = findViewById(R.id.spin_progress);

        reelPreviewLeft   = findViewById(R.id.reel_preview_left);
        reelPreviewCenter = findViewById(R.id.reel_preview_center);
        reelPreviewRight  = findViewById(R.id.reel_preview_right);
    }

    private void setLanguage() {
        ERR_ZERO    = getString(R.string.it_is_not_possible_to_send_null);
        ERR_TOO_LOW = getString(R.string.bet_cannot_be_less_than) + " " + StringEnum.MIN_BET_SLOT.getValue() + " XRP";
        ERR_TOO_HIGH= getString(R.string.bet_cannot_be_more_than) + " " + StringEnum.MAX_BET_SLOT.getValue() + " XRP";
        ERR_BALANCE = getString(R.string.your_account_is_not_enough_to_send);
        ERR_INVALID = getString(R.string.payment_amount_is_incorrect);
        ERR_PAYMENT = getString(R.string.bet_is_made_expect_the_result);
    }

    private void loadReferral() {
        myReferral = preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "0");
    }

    // ─── Bet input style ─────────────────────────────────────────────────────

    /** Показывает нужный контейнер в зависимости от настройки пользователя. */
    private void applyBetInputStyle() {
        String style = preferences.getString(
                StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
        styleChipsContainer.setVisibility(STYLE_CHIPS.equals(style)  ? View.VISIBLE : View.GONE);
        styleSliderContainer.setVisibility(STYLE_SLIDER.equals(style) ? View.VISIBLE : View.GONE);
    }

    // ─── Bet controls ────────────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private void setupPlusMinusButtons() {
        if (btnBetMinus == null || btnBetPlus == null) return;

        // Force magenta icon tint regardless of M3 theme overrides
        ColorStateList slotTint = ColorStateList.valueOf(0xFFD020A0);
        btnBetMinus.setIconTint(slotTint);
        btnBetPlus.setIconTint(slotTint);

        btnBetMinus.setOnClickListener(v -> { soundSelect(); changeBetBy(-1); });
        btnBetPlus.setOnClickListener(v ->  { soundSelect(); changeBetBy(+1); });
        // return false so the click event still fires after the long-press is scheduled
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
        if (sliderBet != null)
            sliderBet.setValue(Math.max(sliderBet.getValueFrom(),
                    Math.min(sliderBet.getValueTo(), betTenths / 10.0f)));
        clearError();
    }

    private void setupSliderListener() {
        if (sliderBet == null) return;
        sliderBet.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser) return;
            betTenths = Math.round(value * 10);
            tvBetPlusMinus.setText(formatTenths(betTenths) + " XRP");
            clearError();
        });
    }

    /** Ограничивает текстовый ввод: не более 1 знака после точки, значение ≤ MAX_BET. */
    private void setupBetFieldWatcher() {
        if (etBet == null) return;
        etBet.setFilters(new android.text.InputFilter[]{ new BetInputFilter(MAX_BET_TENTHS / 10.0) });
        etBet.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) { clearError(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        etBet.setOnFocusChangeListener((v, focused) -> { if (focused) clearError(); });
    }

    private void setupChips() {
        if (chipGroup == null) return;
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            soundSelect();
            int tenths = 0;
            if      (checkedIds.contains(R.id.chip_01)) tenths = 1;
            else if (checkedIds.contains(R.id.chip_05)) tenths = 5;
            else if (checkedIds.contains(R.id.chip_1))  tenths = 10;
            else if (checkedIds.contains(R.id.chip_5))  tenths = 50;
            else if (checkedIds.contains(R.id.chip_10)) tenths = 100;
            else if (checkedIds.contains(R.id.chip_20)) tenths = 200;
            if (tenths == 0) return;

            String style = preferences.getString(
                    StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
            if (STYLE_SLIDER.equals(style)) {
                betTenths = Math.min(tenths, MAX_BET_TENTHS);
                tvBetPlusMinus.setText(formatTenths(betTenths) + " XRP");
                if (sliderBet != null)
                    sliderBet.setValue(Math.min(betTenths / 10.0f, sliderBet.getValueTo()));
            } else {
                if (etBet != null) etBet.setText(formatTenths(tenths));
            }
            clearError();
            if (soundPool != null) soundPool.playBet(SlotGame.this);
        });
    }

    private String formatTenths(int tenths) {
        if (tenths % 10 == 0) return String.valueOf(tenths / 10);
        return String.format(Locale.US, "%.1f", tenths / 10.0);
    }

    // ─── Listeners & Observers ───────────────────────────────────────────────

    private void setupListeners() {
        btnSpin.setOnClickListener(v -> onSpinClicked());
        tvRulesLink.setOnClickListener(v -> { soundNav(); startActivity(new Intent(RULES_SLOT_CLASS)); });
    }

    private void onSpinClicked() {
        soundSelect();
        btnSpin.setClickable(false);
        spinIcon.setVisibility(View.GONE);
        spinProgress.setVisibility(View.VISIBLE);
        String betStr = resolveCurrentBet();
        Flasher.TEST_MODE_ENUM   = TestModeEnum.SLOT_GAME;
        Flasher.TEST_SAND_AMOUNT = betStr;
        viewModel.placeBet(betStr, myReferral);
    }

    private void resetSpinButton() {
        btnSpin.setClickable(true);
        spinIcon.setVisibility(View.VISIBLE);
        spinProgress.setVisibility(View.GONE);
    }

    private String resolveCurrentBet() {
        String style = preferences.getString(
                StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
        if (STYLE_SLIDER.equals(style)) return formatTenths(betTenths);
        String raw = etBet != null ? etBet.getText().toString().trim() : "";
        return raw.isEmpty() ? "0" : raw;
    }

    private void setupObservers() {
        viewModel.getBalance().observe(this, balance -> {
            if (tvBalance != null && balance != null)
                tvBalance.setText(String.format(Locale.US, "%.2f XRP", balance.doubleValue()));
        });

        viewModel.getBetSuccess().observe(this, amount -> {
            SlotFlasher.BET_AMOUNT = amount;
            startActivity(new Intent(SLOT_FLASHER_CLASS));
        });

        viewModel.getError().observe(this, err -> {
            if (err == null) return;
            resetSpinButton();
            showError(errorText(err));
        });
    }

    private String errorText(GameBetError err) {
        switch (err) {
            case AMOUNT_IS_ZERO:       return ERR_ZERO;
            case BET_TOO_LOW:          return ERR_TOO_LOW;
            case BET_TOO_HIGH:         return ERR_TOO_HIGH;
            case INSUFFICIENT_BALANCE: return ERR_BALANCE;
            case PAYMENT_FAILED:       return ERR_PAYMENT;
            default:                   return ERR_INVALID;
        }
    }

    private void showError(String msg) {
        if (tilBetField != null)     tilBetField.setError(msg);
        if (tvBetInputError != null) { tvBetInputError.setText(msg); tvBetInputError.setVisibility(View.VISIBLE); }
    }

    private void clearError() {
        if (tilBetField != null)     tilBetField.setError(null);
        if (tvBetInputError != null) tvBetInputError.setVisibility(View.GONE);
    }

    // ─── Reel preview ────────────────────────────────────────────────────────

    private void startPreviewReels() {
        if (reelPreviewLeft != null) {
            reelPreviewLeft.setReelOrder(REEL_ORDER_LEFT);
            reelPreviewLeft.post(() -> reelPreviewLeft.startSpin());
        }
        if (reelPreviewCenter != null) {
            reelPreviewCenter.setReelOrder(REEL_ORDER_CENTER);
            reelPreviewCenter.post(() -> reelPreviewCenter.startSpin());
        }
        if (reelPreviewRight != null) {
            reelPreviewRight.setReelOrder(REEL_ORDER_RIGHT);
            reelPreviewRight.post(() -> reelPreviewRight.startSpin());
        }
    }

    private void stopPreviewReels() {
        if (reelPreviewLeft   != null) reelPreviewLeft.cancelAnim();
        if (reelPreviewCenter != null) reelPreviewCenter.cancelAnim();
        if (reelPreviewRight  != null) reelPreviewRight.cancelAnim();
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        preferences = PrefsHelper.get(this);
        applyBetInputStyle();
        viewModel.loadBalance();
        resetSpinButton();
        startPreviewReels();

        soundPool     = new GameSoundPool(this);
        noisyReceiver = AudioHelper.registerNoisyReceiver(this,
                () -> { if (casinoMediaPlayer != null && casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause(); });
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);

        if (casinoMediaPlayer == null) casinoMediaPlayer = MediaPlayer.create(this, R.raw.in_casino);
        if (casinoMediaPlayer != null) {
            casinoMediaPlayer.setLooping(true);
            casinoMediaPlayer.setVolume(0.5f, 0.5f);
            if (AudioHelper.isSoundEnabled(this)) casinoMediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreviewReels();
        pmHandler.removeCallbacks(pmRunnable);
        if (casinoMediaPlayer != null && casinoMediaPlayer.isPlaying()) casinoMediaPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (casinoMediaPlayer != null) { casinoMediaPlayer.release(); casinoMediaPlayer = null; }
    }
}
