package com.samuilolegovich.view;

import android.annotation.SuppressLint;
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;

/**
 * Экран результата слот-машины, открываемый когда ответ сервера пришёл после того,
 * как игрок покинул SlotFlasher. Показывает краткий прокрут барабанов и останавливает
 * их на итоговых позициях с анимацией выигравших линий.
 */
@AndroidEntryPoint
public class SlotResult extends BaseActivity {

    public static final String SLOT_RESULT_CLASS = ".view.SlotResult";

    /** Стоп-позиции барабанов (0–83), копируются из SlotFlasher.STOP_POSITIONS перед запуском. */
    public static volatile int[]  PENDING_STOPS = null;
    /** Текст результата: «Вы выиграли X XRP» или «Ваша ставка проиграла». */
    public static volatile String MASSAGE       = "";
    /** true = победа, false = поражение. */
    public static volatile boolean IS_WIN       = false;

    private static final int REEL_STOP_DELAY_MS = 400;
    private static final int SPIN_BEFORE_STOP_MS = 1100; // краткий прокрут перед остановкой

    @SuppressLint("StaticFieldLeak")
    private SlotReelView    reelLeft, reelCenter, reelRight;
    private SlotPaylineView paylineView;
    private TextView        tvResultTitle, tvPayout, tvCongratulations, tvCountdown;
    private View            llContinueBet, btnBackToGame;

    private MediaPlayer          spinMediaPlayer;
    private MediaPlayer          resultMediaPlayer;
    private AudioFocusRequest    audioFocusRequest;
    private Handler              countdownHandler;
    private Runnable             countdownRunnable;
    private final Handler        uiHandler = new Handler(Looper.getMainLooper());

    private String STR_CONGRATULATIONS, STR_DONT_GIVE_UP, STR_BET_WON, STR_BET_LOST;

    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        if (spinMediaPlayer == null) return;
        switch (change) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (spinMediaPlayer.isPlaying()) spinMediaPlayer.pause(); break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                spinMediaPlayer.setVolume(0.1f, 0.1f); break;
            case AudioManager.AUDIOFOCUS_GAIN:
                spinMediaPlayer.setVolume(1f, 1f); break;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slot_flasher);

        bindViews();
        setLanguage();

        // Устанавливаем начальный визуальный порядок для краткого прокрута
        assignInitialReelOrders();
        startSpinAll();
        startSound();

        // Через SPIN_BEFORE_STOP_MS — останавливаем на известных позициях
        uiHandler.postDelayed(this::revealResult, SPIN_BEFORE_STOP_MS);
    }



    private void bindViews() {
        reelLeft          = findViewById(R.id.reel_left);
        reelCenter        = findViewById(R.id.reel_center);
        reelRight         = findViewById(R.id.reel_right);
        paylineView       = findViewById(R.id.payline_view);
        tvResultTitle     = findViewById(R.id.tv_result_title);
        tvPayout          = findViewById(R.id.tv_payout);
        tvCongratulations = findViewById(R.id.tv_congratulations);
        tvCountdown       = findViewById(R.id.tv_countdown);
        llContinueBet     = findViewById(R.id.ll_continue_bet);
        btnBackToGame     = findViewById(R.id.btn_back_to_game);

        llContinueBet.setVisibility(View.GONE);
        btnBackToGame.setOnClickListener(v -> { cancelCountdown(); onBackPressed(); });
    }

    private void setLanguage() {
        STR_CONGRATULATIONS = getString(R.string.congratulations);
        STR_DONT_GIVE_UP    = getString(R.string.dont_give_up);
        STR_BET_WON         = getString(R.string.bet_won);
        STR_BET_LOST        = getString(R.string.bet_lost);
    }

    private void assignInitialReelOrders() {
        reelLeft  .setReelOrder(new int[]{ 0, 2, 4, 1, 5, 3, 6 });
        reelCenter.setReelOrder(new int[]{ 3, 0, 5, 2, 6, 1, 4 });
        reelRight .setReelOrder(new int[]{ 5, 1, 3, 6, 0, 4, 2 });
    }

    private void startSpinAll() {
        reelLeft  .startSpin();
        reelCenter.startSpin();
        reelRight .startSpin();
    }

    private void startSound() {
        spinMediaPlayer = MediaPlayer.create(this, R.raw.roulette_spin);
        if (spinMediaPlayer != null) {
            spinMediaPlayer.setLooping(true);
            spinMediaPlayer.setVolume(1f, 1f);
            if (AudioHelper.isSoundEnabled(this)) spinMediaPlayer.start();
        }
    }

    /** Останавливает барабаны на итоговых позициях и показывает результат. */
    private void revealResult() {
        int[] stops = PENDING_STOPS;
        if (stops == null || stops.length != 3) {
            showResultUi();
            return;
        }

        int[][] matrix = SlotReelStrip.buildMatrix(stops[0], stops[1], stops[2]);

        reelLeft  .setReelOrder(buildVisualOrder(matrix[0][0], matrix[1][0], matrix[2][0]));
        reelCenter.setReelOrder(buildVisualOrder(matrix[0][1], matrix[1][1], matrix[2][1]));
        reelRight .setReelOrder(buildVisualOrder(matrix[0][2], matrix[1][2], matrix[2][2]));

        reelLeft.stopAtPosition(1, () ->
            uiHandler.postDelayed(() ->
                reelCenter.stopAtPosition(1, () ->
                    uiHandler.postDelayed(() ->
                        reelRight.stopAtPosition(1, () ->
                            uiHandler.postDelayed(this::showResultUi, 200)
                        ), REEL_STOP_DELAY_MS)
                ), REEL_STOP_DELAY_MS)
        );
    }

    private void showResultUi() {
        stopSound();
        boolean win = IS_WIN;
        resultMediaPlayer = MediaPlayer.create(this, win ? R.raw.win : R.raw.lost);
        if (resultMediaPlayer != null && AudioHelper.isSoundEnabled(this)) resultMediaPlayer.start();

        if (win) {
            reelLeft.setHighlightMiddle(true);
            reelCenter.setHighlightMiddle(true);
            reelRight.setHighlightMiddle(true);
            if (PENDING_STOPS != null) {
                int[][] matrix = SlotReelStrip.buildMatrix(PENDING_STOPS[0], PENDING_STOPS[1], PENDING_STOPS[2]);
                uiHandler.postDelayed(() -> paylineView.showWinLines(matrix), 350);
            }
        }

        tvResultTitle.setText(win ? STR_BET_WON : STR_BET_LOST);
        tvResultTitle.setVisibility(View.VISIBLE);
        tvResultTitle.post(() -> applyGradient(tvResultTitle, win));

        tvPayout.setText(MASSAGE);
        tvPayout.setVisibility(View.VISIBLE);

        tvCongratulations.setText(win ? STR_CONGRATULATIONS : STR_DONT_GIVE_UP);
        tvCongratulations.setVisibility(View.VISIBLE);

        btnBackToGame.setVisibility(View.VISIBLE);
        tvCountdown.setVisibility(View.VISIBLE);
        startCountdown();

        if (win) startWinConfetti(); else startLostAshes();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    /**
     * Строит визуальный reelOrder[7] с top/mid/bot на фиксированных позициях 0/1/2.
     * stopAtPosition(1) гарантирует правильный показ всех трёх ячеек.
     */
    private static int[] buildVisualOrder(int topSym, int midSym, int botSym) {
        int[] order = new int[7];
        order[0] = topSym; order[1] = midSym; order[2] = botSym;
        boolean[] placed = new boolean[7];
        placed[topSym] = true; placed[midSym] = true; placed[botSym] = true;
        int pos = 3;
        for (int sym = 0; sym < 7 && pos < 7; sym++) if (!placed[sym]) order[pos++] = sym;
        return order;
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
        if (spinMediaPlayer != null) {
            try { spinMediaPlayer.stop(); } catch (Exception ignored) {}
        }
    }

    private void startWinConfetti() {
        KonfettiView kv = findViewById(R.id.konfetti_view);
        if (kv == null) return;
        kv.start(Arrays.asList(
            new PartyFactory(new Emitter(4000L, TimeUnit.MILLISECONDS).perSecond(50))
                .angle(Angle.BOTTOM).spread(Spread.ROUND)
                .setSpeedBetween(2f, 9f)
                .colors(Arrays.asList(0xFFFFE040, 0xFF00D4FF, 0xFFD020A0, 0xFFFFB000, 0xFFFFFFFF, 0xFF9020D0))
                .position(0.0, 0.0, 1.0, 0.0)
                .build()
        ));
    }

    private void startLostAshes() {
        KonfettiView kv = findViewById(R.id.konfetti_view);
        if (kv == null) return;
        kv.start(Arrays.asList(
            new PartyFactory(new Emitter(4500L, TimeUnit.MILLISECONDS).perSecond(30))
                .angle(Angle.TOP).spread(80)
                .setSpeedBetween(3f, 8f).setDamping(0.98f).timeToLive(5500L)
                .colors(Arrays.asList(0xFF8B0000, 0xFF2D2D2D, 0xFF4A0E1C, 0xFF1C1C1C, 0xFF5C0A1A))
                .position(0.0, 1.0, 1.0, 1.0)
                .build()
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
        reelLeft.cancelAnim();
        reelCenter.cancelAnim();
        reelRight.cancelAnim();
        paylineView.reset();
        stopSound();
        if (resultMediaPlayer != null) resultMediaPlayer.stop();
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
        reelLeft.cancelAnim();
        reelCenter.cancelAnim();
        reelRight.cancelAnim();
        paylineView.reset();
        stopSound();
        if (resultMediaPlayer != null && resultMediaPlayer.isPlaying()) resultMediaPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        if (spinMediaPlayer != null) { spinMediaPlayer.release(); spinMediaPlayer = null; }
        if (resultMediaPlayer != null) { resultMediaPlayer.release(); resultMediaPlayer = null; }
        PENDING_STOPS = null;
    }
}
