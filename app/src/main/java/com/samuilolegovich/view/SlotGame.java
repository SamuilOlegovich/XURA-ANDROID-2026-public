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
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.GameSoundPool;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.viewmodel.GameBetError;
import com.samuilolegovich.viewmodel.SlotViewModel;

import java.util.Locale;

import static com.samuilolegovich.view.SlotFlasher.SLOT_FLASHER_CLASS;
import static com.samuilolegovich.view.RulesOfTheGameSlot.RULES_SLOT_CLASS;

import dagger.hilt.android.AndroidEntryPoint;



/**
 * Экран ставки на слот-машину: ввод суммы (chips / slider), поле реферала,
 * кнопка «КРУТИТЬ» — отправляет ставку через {@link SlotViewModel} и открывает {@link SlotFlasher}.
 */
@AndroidEntryPoint
public class SlotGame extends BaseActivity {
    public static final String SLOT_GAME_CLASS = ".SlotGame";

    private static final String STYLE_CHIPS  = "chips";
    private static final String STYLE_SLIDER = "slider";

    private static final int MAX_BET_TENTHS     = 1000;
    private static final int DEFAULT_BET_TENTHS = 10;

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

    // Preview reels (animated on slot screen)
    private static final int[] REEL_ORDER_LEFT   = {0, 2, 4, 1, 5, 3, 6};
    private static final int[] REEL_ORDER_CENTER  = {3, 0, 5, 2, 6, 1, 4};
    private static final int[] REEL_ORDER_RIGHT   = {5, 1, 3, 6, 0, 4, 2};

    // Bet values matching chip buttons order (0.1, 0.5, 1, 5, 10)
    private static final String[] BET_VALUES = {"0.1", "0.5", "1", "5", "10"};
    private static final int DEFAULT_BET_IDX = 2; // "1" XRP

    // UI
    private TextView tvBalance;
    private View     btnSpin;
    private View     tvRulesLink;
    private View     styleChipsContainer;
    private View     styleSliderContainer;
    private EditText etBet;
    private Slider   sliderBet;
    private MaterialButton btnBetMinus;
    private MaterialButton btnBetPlus;
    private TextView tvBetPlusMinus;
    private TextView tvBetInputError;

    private SlotReelView reelPreviewLeft;
    private SlotReelView reelPreviewCenter;
    private SlotReelView reelPreviewRight;
    private View[] betBtns;


    private int betTenths = DEFAULT_BET_TENTHS;
    private final Handler pmHandler = new Handler(Looper.getMainLooper());
    private Runnable pmRunnable;

    // Localised error strings
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

        viewModel    = new ViewModelProvider(this).get(SlotViewModel.class);
        preferences  = PrefsHelper.get(this);

        bindViews();
        setLanguage();
        loadReferral();
        setupBetStyle();
        setupChips();
        setupSliderButtons();
        setupListeners();
        setupObservers();
        setupBottomNav();

        viewModel.loadBalance();
    }



    private void bindViews() {
        tvBalance            = findViewById(R.id.tv_balance);
        btnSpin              = findViewById(R.id.btn_spin);
        tvRulesLink          = findViewById(R.id.tv_rules_link);
        styleChipsContainer  = findViewById(R.id.style_chips_container);
        styleSliderContainer = findViewById(R.id.style_slider_container);
        etBet                = findViewById(R.id.et_bet);
        sliderBet            = findViewById(R.id.slider_bet);
        btnBetMinus          = findViewById(R.id.btn_bet_minus);
        btnBetPlus           = findViewById(R.id.btn_bet_plus);
        tvBetPlusMinus       = findViewById(R.id.tv_bet_plus_minus);
        tvBetInputError      = findViewById(R.id.tv_bet_input_error);

        reelPreviewLeft   = findViewById(R.id.reel_preview_left);
        reelPreviewCenter = findViewById(R.id.reel_preview_center);
        reelPreviewRight  = findViewById(R.id.reel_preview_right);

        int[] chipIds = {R.id.chip_01, R.id.chip_05, R.id.chip_1, R.id.chip_5, R.id.chip_10};
        betBtns = new View[chipIds.length];
        for (int i = 0; i < chipIds.length; i++) betBtns[i] = findViewById(chipIds[i]);
    }

    private void setLanguage() {
        ERR_ZERO    = getString(R.string.it_is_not_possible_to_send_null);
        ERR_TOO_LOW = getString(R.string.bet_cannot_be_less_than)  + " " + StringEnum.MIN_BET_SLOT.getValue() + " XRP";
        ERR_TOO_HIGH= getString(R.string.bet_cannot_be_more_than)  + " " + StringEnum.MAX_BET_SLOT.getValue() + " XRP";
        ERR_BALANCE = getString(R.string.your_account_is_not_enough_to_send);
        ERR_INVALID = getString(R.string.payment_amount_is_incorrect);
        ERR_PAYMENT = getString(R.string.bet_is_made_expect_the_result);

    }

    private void loadReferral() {
        myReferral = preferences.getString(StringEnum.APP_PREFERENCES_REFERRAL.getValue(), "0");
    }

    private void setupBetStyle() {
        String style = preferences.getString(StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
        if (STYLE_SLIDER.equals(style)) {
            styleChipsContainer.setVisibility(View.GONE);
            styleSliderContainer.setVisibility(View.VISIBLE);
            updateSliderDisplay();
        } else {
            styleChipsContainer.setVisibility(View.VISIBLE);
            styleSliderContainer.setVisibility(View.GONE);
        }
    }

    private void setupChips() {
        if (betBtns == null) return;
        for (int i = 0; i < betBtns.length; i++) {
            final int idx = i;
            if (betBtns[i] == null) continue;
            betBtns[i].setOnClickListener(v -> {
                selectBetButton(idx);
                clearError();
                if (soundPool != null) soundPool.playBet(SlotGame.this);
            });
        }
        selectBetButton(DEFAULT_BET_IDX);
    }

    private void selectBetButton(int idx) {
        if (betBtns == null || idx < 0 || idx >= betBtns.length) return;
        for (int i = 0; i < betBtns.length; i++) {
            if (betBtns[i] == null) continue;
            boolean sel = (i == idx);
            betBtns[i].setSelected(sel);
            if (betBtns[i] instanceof TextView)
                ((TextView) betBtns[i]).setTextColor(sel ? 0xFFFFFFFF : 0xFFD020A0);
        }
        if (etBet != null) etBet.setText(BET_VALUES[idx]);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSliderButtons() {
        if (sliderBet == null) return;
        sliderBet.addOnChangeListener((s, val, user) -> {
            betTenths = (int) val;
            updateSliderDisplay();
        });

        View.OnTouchListener repeatListener = (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    scheduleRepeat(v.getId());
                    v.performClick();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    pmHandler.removeCallbacks(pmRunnable);
                    return true;
            }
            return false;
        };
        if (btnBetMinus != null) btnBetMinus.setOnTouchListener(repeatListener);
        if (btnBetPlus  != null) btnBetPlus .setOnTouchListener(repeatListener);
    }

    private void scheduleRepeat(int viewId) {
        pmRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewId == R.id.btn_bet_minus) { betTenths = Math.max(1, betTenths - 1); }
                else                              { betTenths = Math.min(MAX_BET_TENTHS, betTenths + 1); }
                if (sliderBet != null) sliderBet.setValue(betTenths);
                updateSliderDisplay();
                pmHandler.postDelayed(this, 80);
            }
        };
        pmHandler.postDelayed(pmRunnable, 350);
    }

    private void updateSliderDisplay() {
        double xrp = betTenths / 10.0;
        if (tvBetPlusMinus != null)
            tvBetPlusMinus.setText(String.format(Locale.US, "%.1f XRP", xrp));
    }

    private void setupListeners() {
        btnSpin.setOnClickListener(v -> onSpinClicked());
        tvRulesLink.setOnClickListener(v -> startActivity(new Intent(RULES_SLOT_CLASS)));

        if (etBet != null) etBet.setOnFocusChangeListener((v, f) -> { if (f) clearError(); });
    }

    private void onSpinClicked() {
        String betStr = resolveCurrentBet();
        String ref    = resolveReferral();

        Flasher.TEST_MODE_ENUM = TestModeEnum.SLOT_GAME;
        Flasher.TEST_SAND_AMOUNT = betStr;

        viewModel.placeBet(betStr, ref);
    }

    private String resolveCurrentBet() {
        String style = preferences.getString(StringEnum.APP_PREFERENCES_BET_INPUT_STYLE.getValue(), STYLE_CHIPS);
        if (STYLE_SLIDER.equals(style)) {
            return String.format(Locale.US, "%.1f", betTenths / 10.0);
        }
        String raw = etBet != null ? etBet.getText().toString().trim() : "";
        return raw.isEmpty() ? "0" : raw;
    }

    private String resolveReferral() {
        return myReferral;
    }

    private void setupObservers() {
        viewModel.getBalance().observe(this, balance -> {
            if (tvBalance != null && balance != null)
                tvBalance.setText(String.format(Locale.US, "%.6f XRP", balance.doubleValue()));
        });

        viewModel.getBetSuccess().observe(this, amount -> {
            SlotFlasher.BET_AMOUNT = amount;
            startActivity(new Intent(SLOT_FLASHER_CLASS));
        });

        viewModel.getError().observe(this, err -> {
            if (err == null) return;
            showError(errorText(err));
        });
    }

    private String errorText(GameBetError err) {
        switch (err) {
            case AMOUNT_IS_ZERO:         return ERR_ZERO;
            case BET_TOO_LOW:            return ERR_TOO_LOW;
            case BET_TOO_HIGH:           return ERR_TOO_HIGH;
            case INSUFFICIENT_BALANCE:   return ERR_BALANCE;
            case PAYMENT_FAILED:         return ERR_PAYMENT;
            default:                     return ERR_INVALID;
        }
    }

    private void showError(String msg) {
        if (tvBetInputError != null) { tvBetInputError.setText(msg); tvBetInputError.setVisibility(View.VISIBLE); }
    }

    private void clearError() {
        if (tvBetInputError != null) tvBetInputError.setVisibility(View.GONE);
    }

    // ─── Audio ──────────────────────────────────────────────────────────────

    private void startPreviewReels() {
        if (reelPreviewLeft != null) {
            reelPreviewLeft.setReelOrder(REEL_ORDER_LEFT);
            reelPreviewLeft.startSpin();
        }
        if (reelPreviewCenter != null) {
            reelPreviewCenter.setReelOrder(REEL_ORDER_CENTER);
            reelPreviewCenter.startSpin();
        }
        if (reelPreviewRight != null) {
            reelPreviewRight.setReelOrder(REEL_ORDER_RIGHT);
            reelPreviewRight.startSpin();
        }
    }

    private void stopPreviewReels() {
        if (reelPreviewLeft   != null) reelPreviewLeft.cancelAnim();
        if (reelPreviewCenter != null) reelPreviewCenter.cancelAnim();
        if (reelPreviewRight  != null) reelPreviewRight.cancelAnim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadBalance();
        startPreviewReels();
        soundPool = new GameSoundPool(this);
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
