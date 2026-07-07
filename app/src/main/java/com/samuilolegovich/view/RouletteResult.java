package com.samuilolegovich.view;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.Lotto;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;

/**
 * Экран результата рулетки (и всех игр на базе RouletteWheelView), открываемый когда
 * ответ сервера пришёл после того, как игрок покинул Flasher. Крутит колесо кратко
 * и останавливает его на числе, полученном от сервера.
 */
@AndroidEntryPoint
public class RouletteResult extends BaseActivity {

    public static final String ROULETTE_RESULT_CLASS = ".view.RouletteResult";

    /** Выигравшее число (0–36), скопированное из Flasher.NUMBER_BET перед запуском. */
    public static volatile int     PENDING_NUMBER = 0;
    /** Текст результата: «Вы выиграли X XRP» или «Ваша ставка проиграла». */
    public static volatile String  MASSAGE        = "";
    /** true = победа, false = поражение. */
    public static volatile boolean IS_WIN         = false;

    private static final long SPIN_BEFORE_STOP_MS = 1200L;

    private RouletteWheelView wheelView;
    private TextView          winInfo, numberInfo, infoTwo, infoThree, tvCountdown;
    private View              btnBackToGame, llContinueBet;

    private MediaPlayer       spinPlayer;
    private MediaPlayer       resultPlayer;
    private AudioFocusRequest audioFocusRequest;
    private Handler           countdownHandler;
    private Runnable          countdownRunnable;
    private final Handler     uiHandler = new Handler(Looper.getMainLooper());

    private String CONGRATULATIONS, DONT_GIVE_UP, BET_LOST, BET_WON;

    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        if (spinPlayer == null) return;
        switch (change) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (spinPlayer.isPlaying()) spinPlayer.pause();
                if (resultPlayer != null && resultPlayer.isPlaying()) resultPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                spinPlayer.setVolume(0.1f, 0.1f);
                if (resultPlayer != null) resultPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                spinPlayer.setVolume(1f, 1f);
                if (resultPlayer != null) resultPlayer.setVolume(1f, 1f);
                break;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flasher);

        bindViews();
        setLanguage();
        startWheelAndSound();

        uiHandler.postDelayed(this::revealResult, SPIN_BEFORE_STOP_MS);
    }



    private void bindViews() {
        wheelView     = findViewById(R.id.roulette_wheel);
        winInfo       = findViewById(R.id.last_text_view);
        numberInfo    = findViewById(R.id.number_info_text);
        infoTwo       = findViewById(R.id.last_text_view_two);
        infoThree     = findViewById(R.id.last_text_view_tree);
        tvCountdown   = findViewById(R.id.tv_countdown);
        btnBackToGame = findViewById(R.id.btn_back_to_game);
        llContinueBet = findViewById(R.id.ll_continue_bet);

        llContinueBet.setVisibility(View.GONE);
        btnBackToGame.setOnClickListener(v -> { cancelCountdown(); onBackPressed(); });
    }

    private void setLanguage() {
        CONGRATULATIONS = getString(R.string.congratulations);
        DONT_GIVE_UP    = getString(R.string.dont_give_up);
        BET_LOST        = getString(R.string.bet_lost);
        BET_WON         = getString(R.string.bet_won);
    }

    private void startWheelAndSound() {
        wheelView.setCenterColor(0xFF000000);
        wheelView.startSpinning();

        spinPlayer = MediaPlayer.create(this, R.raw.roulette_spin);
        if (spinPlayer != null) {
            spinPlayer.setLooping(true);
            spinPlayer.setVolume(1f, 1f);
            if (AudioHelper.isSoundEnabled(this)) spinPlayer.start();
        }
    }

    /** Останавливает колесо на итоговом числе и показывает результат. */
    private void revealResult() {
        int number = PENDING_NUMBER;
        wheelView.stopAtNumber(number, () -> showGameResult(number));
    }

    private void showGameResult(int number) {
        stopSound();
        boolean win = IS_WIN;
        resultPlayer = MediaPlayer.create(this, win ? R.raw.win : R.raw.lost);
        if (resultPlayer != null && AudioHelper.isSoundEnabled(this)) resultPlayer.start();

        wheelView.setCenterColor(sectorColor(number));

        numberInfo.setText(String.valueOf(number));
        numberInfo.setTextColor(0xFFFFB000);
        numberInfo.setVisibility(View.VISIBLE);

        winInfo.setText(win ? BET_WON : BET_LOST);
        winInfo.setVisibility(View.VISIBLE);
        winInfo.post(() -> applyGradient(winInfo, win));

        infoThree.setText(win ? CONGRATULATIONS : DONT_GIVE_UP);
        infoThree.setVisibility(View.VISIBLE);

        infoTwo.setText(MASSAGE);
        infoTwo.setVisibility(View.VISIBLE);

        btnBackToGame.setVisibility(View.VISIBLE);
        tvCountdown.setVisibility(View.VISIBLE);
        startCountdown();

        if (win) startWinConfetti(); else startLostAshes();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private int sectorColor(int n) {
        if (n == 0)                                          return 0xFF007040; // зелёный
        if (Lotto.learnTheColorOfNumber(String.valueOf(n))) return 0xFF111111; // чёрный
        return 0xFFC81030;                                                      // красный
    }

    private void applyGradient(TextView tv, boolean win) {
        float w = tv.getWidth();
        if (w == 0) return;
        int[] colors = win
                ? new int[]{ 0xFFFFE040, 0xFFFFB000, 0xFFFF6A00, 0xFFFFB000, 0xFFFFE040 }
                : new int[]{ 0xFFFF3060, 0xFF990022, 0xFF666688, 0xFF333355 };
        tv.getPaint().setShader(
                new LinearGradient(0, 0, w, 0, colors, null, Shader.TileMode.CLAMP));
        tv.invalidate();
    }

    private void stopSound() {
        if (spinPlayer != null) {
            try { spinPlayer.stop(); } catch (Exception ignored) {}
        }
    }

    private void startWinConfetti() {
        KonfettiView kv = findViewById(R.id.konfetti_view);
        if (kv == null) return;
        kv.start(Arrays.asList(
            new PartyFactory(new Emitter(4000L, TimeUnit.MILLISECONDS).perSecond(50))
                .angle(Angle.BOTTOM).spread(Spread.ROUND).setSpeedBetween(2f, 9f)
                .colors(Arrays.asList(0xFFFFE040, 0xFF00D4FF, 0xFFD020A0, 0xFFFFB000, 0xFFFFFFFF, 0xFF9020D0))
                .position(0.0, 0.0, 1.0, 0.0).build()
        ));
    }

    private void startLostAshes() {
        KonfettiView kv = findViewById(R.id.konfetti_view);
        if (kv == null) return;
        kv.start(Arrays.asList(
            new PartyFactory(new Emitter(4500L, TimeUnit.MILLISECONDS).perSecond(30))
                .angle(Angle.TOP).spread(80).setSpeedBetween(3f, 8f).setDamping(0.98f).timeToLive(5500L)
                .colors(Arrays.asList(0xFF8B0000, 0xFF2D2D2D, 0xFF4A0E1C, 0xFF1C1C1C, 0xFF5C0A1A))
                .position(0.0, 1.0, 1.0, 1.0).build()
        ));
    }

    // ─── Countdown ──────────────────────────────────────────────────────────

    private void startCountdown() {
        final int[] secs = { 10 };
        tvCountdown.setText(getString(R.string.flasher_return_in, secs[0]));
        countdownHandler  = new Handler(Looper.getMainLooper());
        countdownRunnable = new Runnable() {
            @Override public void run() {
                secs[0]--;
                if (secs[0] <= 0) { onBackPressed(); }
                else {
                    tvCountdown.setText(getString(R.string.flasher_return_in, secs[0]));
                    countdownHandler.postDelayed(this, 1000);
                }
            }
        };
        countdownHandler.postDelayed(countdownRunnable, 1000);
    }

    private void cancelCountdown() {
        if (countdownHandler != null && countdownRunnable != null)
            countdownHandler.removeCallbacks(countdownRunnable);
    }

    // ─── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        cancelCountdown();
        uiHandler.removeCallbacksAndMessages(null);
        if (wheelView != null) wheelView.stopSpinning();
        stopSound();
        if (resultPlayer != null) resultPlayer.stop();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacksAndMessages(null);
        if (wheelView != null) wheelView.stopSpinning();
        stopSound();
        if (resultPlayer != null && resultPlayer.isPlaying()) resultPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        if (spinPlayer != null) { spinPlayer.release(); spinPlayer = null; }
        if (resultPlayer != null) { resultPlayer.release(); resultPlayer = null; }
        PENDING_NUMBER = 0;
    }
}
