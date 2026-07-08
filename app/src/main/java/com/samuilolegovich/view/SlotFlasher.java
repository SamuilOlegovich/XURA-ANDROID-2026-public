package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
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

import androidx.core.content.ContextCompat;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.async.runnable.FlasherRun;
import com.samuilolegovich.async.runnable.NotifierRunForTrialGame;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SlotSpinSoundPlayer;

import java.security.SecureRandom;

import dagger.hilt.android.AndroidEntryPoint;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;



/**
 * Экран ожидания результата слот-машины: три барабана крутятся одновременно,
 * затем останавливаются слева направо (с задержкой 400 мс между ними).
 * После остановки показывает результат (WIN / LOSE) с обратным отсчётом.
 */
@AndroidEntryPoint
public class SlotFlasher extends BaseActivity {
    public static final String SLOT_FLASHER_CLASS = ".SlotFlasher";

    // Данные ставки, переданные из SlotGame
    public static volatile String  BET_AMOUNT = "0";
    // Стоп-позиции трёх барабанов (0–83), полученные от сервера или сгенерированные в trial-режиме
    public static volatile int[]   STOP_POSITIONS = null;
    // Результирующая матрица 3×3: [row][col], row 0=top 1=mid 2=bot, col 0=left 1=mid 2=right
    public static volatile int[][] RESULT_MATRIX = null;

    public static volatile boolean VISIBLE_ON_SCREEN = false;
    @SuppressLint("StaticFieldLeak")
    public static volatile SlotFlasher SLOT_FLASHER;

    private volatile boolean waitingForResult = true;
    private boolean resultShown   = false;
    private boolean resultUiShown = false;

    // Полная матрица 3×3 и результат — сохраняем для восстановления после onPause/onResume
    private int[][]  pendingMatrix     = null;
    private String   pendingResultText = null;
    private boolean  pendingWin        = false;

    private static final int REEL_STOP_DELAY_MS = 400; // задержка между остановкой барабанов

    private SlotReelView   reelLeft, reelCenter, reelRight;
    private SlotPaylineView paylineView;
    private TextView tvResultTitle;
    private TextView tvPayout;
    private TextView tvCongratulations;
    private TextView tvCountdown;
    private View llContinueBet;
    private View btnBackToGame;

    private SlotSpinSoundPlayer spinPlayer;
    private MediaPlayer resultMediaPlayer;
    private AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver noisyReceiver;

    private final AudioManager.OnAudioFocusChangeListener focusListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (spinPlayer != null) spinPlayer.pause();
                if (resultMediaPlayer != null && resultMediaPlayer.isPlaying()) resultMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (spinPlayer != null) spinPlayer.setVolume(0.1f);
                if (resultMediaPlayer != null) resultMediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (spinPlayer != null) spinPlayer.setVolume(1.0f);
                if (resultMediaPlayer != null) resultMediaPlayer.setVolume(1f, 1f);
                break;
        }
    };

    private Handler countdownHandler;
    private Runnable countdownRunnable;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private String STR_CONGRATULATIONS;
    private String STR_DONT_GIVE_UP;
    private String STR_BET_WON;
    private String STR_BET_LOST;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slot_flasher);

        SLOT_FLASHER = this;
        waitingForResult = true;

        bindViews();
        setLanguage();
        assignReelOrders();
        startSpinAll();
        startSound();
        startBetTimeout();

        // Для реального режима — ждём ответа сервера через FlasherRun
        AppExecutors.io().execute(new FlasherRun());

        // Для тестового режима — симулируем локально
        if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            AppExecutors.io().execute(new NotifierRunForTrialGame(TestModeEnum.SLOT_GAME));
        }
    }



    private void bindViews() {
        reelLeft         = findViewById(R.id.reel_left);
        reelCenter       = findViewById(R.id.reel_center);
        reelRight        = findViewById(R.id.reel_right);
        paylineView      = findViewById(R.id.payline_view);
        tvResultTitle    = findViewById(R.id.tv_result_title);
        tvPayout         = findViewById(R.id.tv_payout);
        tvCongratulations= findViewById(R.id.tv_congratulations);
        tvCountdown      = findViewById(R.id.tv_countdown);
        llContinueBet    = findViewById(R.id.ll_continue_bet);
        btnBackToGame    = findViewById(R.id.btn_back_to_game);

        btnBackToGame.setOnClickListener(v -> { cancelCountdown(); onBackPressed(); });
        llContinueBet.setOnClickListener(v -> onBackPressed());
    }

    private void setLanguage() {
        STR_CONGRATULATIONS = getString(R.string.congratulations);
        STR_DONT_GIVE_UP    = getString(R.string.dont_give_up);
        STR_BET_WON         = getString(R.string.bet_won);
        STR_BET_LOST        = getString(R.string.bet_lost);
    }

    private void assignReelOrders() {
        // Короткий визуальный стрип — для плавной анимации спина и остановки.
        // SlotReelStrip (84 позиции) используется только для определения символа-результата.
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
        spinPlayer = new SlotSpinSoundPlayer();
        if (AudioHelper.isSoundEnabled(this)) spinPlayer.start();
        // Клики синхронизированы с анимацией левого барабана
        reelLeft.setOnTickListener(() -> {
            if (spinPlayer != null) spinPlayer.playClick();
        });
    }

    /** Вызывается из NotifierRun / NotifierRunForTrialGame с результатом ставки. */
    public void stopGame(String text, boolean win) {
        runOnUiThread(() -> {
            if (resultShown) return;
            resultShown = true;
            waitingForResult = false;
            FlasherRun.FLAG = false;
            cancelBetTimeout();
            stopReelsSequentially(text, win);
        });
    }

    /**
     * Останавливает барабаны слева направо с паузой REEL_STOP_DELAY_MS между каждым.
     * Полная матрица 3×3 строится из STOP_POSITIONS (сервер) или RESULT_MATRIX (trial fallback).
     * Перед остановкой каждому барабану присваивается визуальный reelOrder с правильными
     * top/mid/bot — чтобы все три видимые ячейки соответствовали 84-позиционному стрипу.
     * Состояние сохраняется в полях для восстановления после onPause/onResume.
     */
    private void stopReelsSequentially(String resultText, boolean win) {
        int[][] matrix = resolveMatrix(win);
        pendingMatrix     = matrix;
        pendingResultText = resultText;
        pendingWin        = win;

        // Задаём визуальный порядок: reelOrder[0]=top, [1]=mid, [2]=bot по стрипу
        reelLeft  .setReelOrder(buildVisualOrder(matrix[0][0], matrix[1][0], matrix[2][0]));
        reelCenter.setReelOrder(buildVisualOrder(matrix[0][1], matrix[1][1], matrix[2][1]));
        reelRight .setReelOrder(buildVisualOrder(matrix[0][2], matrix[1][2], matrix[2][2]));

        // Останавливаем на индексе 1 (mid) — top и bot уже на позициях 0 и 2
        reelLeft.stopAtPosition(1, () -> {
            uiHandler.postDelayed(() -> {
                reelCenter.stopAtPosition(1, () -> {
                    uiHandler.postDelayed(() -> {
                        reelRight.stopAtPosition(1, () -> {
                            uiHandler.postDelayed(() -> showResult(resultText, win), 200);
                        });
                    }, REEL_STOP_DELAY_MS);
                });
            }, REEL_STOP_DELAY_MS);
        });
    }

    /** Строит полную матрицу 3×3: из STOP_POSITIONS (сервер), RESULT_MATRIX или случайного fallback. */
    private int[][] resolveMatrix(boolean win) {
        if (STOP_POSITIONS != null && STOP_POSITIONS.length == 3) {
            return SlotReelStrip.buildMatrix(STOP_POSITIONS[0], STOP_POSITIONS[1], STOP_POSITIONS[2]);
        }
        if (RESULT_MATRIX != null) {
            return RESULT_MATRIX;
        }
        // Полный fallback: средняя строка — осмысленная, верх/низ — случайные
        int[] mid = generateMiddleRow(null, win);
        SecureRandom rnd = new SecureRandom();
        int[][] m = new int[3][3];
        m[0] = new int[]{ rnd.nextInt(7), rnd.nextInt(7), rnd.nextInt(7) };
        m[1] = mid;
        m[2] = new int[]{ rnd.nextInt(7), rnd.nextInt(7), rnd.nextInt(7) };
        return m;
    }

    /**
     * Строит 7-элементный визуальный reelOrder с гарантированным расположением:
     * index 0 = topSym, index 1 = midSym, index 2 = botSym.
     * Оставшиеся 4 позиции заполняются символами, не вошедшими в top/mid/bot.
     * Корректно обрабатывает дублирующиеся символы в колонке.
     */
    private static int[] buildVisualOrder(int topSym, int midSym, int botSym) {
        int[] order = new int[7];
        order[0] = topSym;
        order[1] = midSym;
        order[2] = botSym;
        boolean[] placed = new boolean[7];
        placed[topSym] = true;
        placed[midSym] = true;
        placed[botSym] = true;
        int pos = 3;
        for (int sym = 0; sym < 7 && pos < 7; sym++) {
            if (!placed[sym]) order[pos++] = sym;
        }
        return order;
    }

    /** Показывает результат: подсветка, WIN/LOSE текст, обратный отсчёт. */
    private void showResult(String text, boolean win) {
        resultUiShown = true;
        stopSound();

        if (win) {
            reelLeft.setHighlightMiddle(true);
            reelCenter.setHighlightMiddle(true);
            reelRight.setHighlightMiddle(true);
            if (pendingMatrix != null) {
                uiHandler.postDelayed(() -> paylineView.showWinLines(pendingMatrix), 350);
            }
        }

        resultMediaPlayer = MediaPlayer.create(this, win ? R.raw.win : R.raw.lost);
        if (resultMediaPlayer != null && AudioHelper.isSoundEnabled(this)) resultMediaPlayer.start();

        tvResultTitle.setText(win ? STR_BET_WON : STR_BET_LOST);
        tvResultTitle.setVisibility(View.VISIBLE);
        tvResultTitle.post(() -> applyGradient(tvResultTitle, win));

        tvPayout.setText(text);
        tvPayout.setVisibility(View.VISIBLE);

        tvCongratulations.setText(win ? STR_CONGRATULATIONS : STR_DONT_GIVE_UP);
        tvCongratulations.setVisibility(View.VISIBLE);

        llContinueBet.setVisibility(View.GONE);
        btnBackToGame.setVisibility(View.VISIBLE);
        tvCountdown.setVisibility(View.VISIBLE);
        startCountdown();

        if (win) startWinConfetti(); else startLostAshes();
    }

    private void startLostAshes() {
        KonfettiView kv = findViewById(R.id.konfetti_view);
        if (kv == null) return;
        List<Party> parties = Arrays.asList(
            new PartyFactory(new Emitter(4500L, TimeUnit.MILLISECONDS).perSecond(30))
                .angle(Angle.TOP)
                .spread(80)
                .setSpeedBetween(3f, 8f)
                .setDamping(0.98f)
                .timeToLive(5500L)
                .colors(Arrays.asList(0xFF8B0000, 0xFF2D2D2D, 0xFF4A0E1C, 0xFF1C1C1C, 0xFF5C0A1A))
                .position(0.0, 1.0, 1.0, 1.0)
                .build()
        );
        kv.start(parties);
    }

    private void startWinConfetti() {
        KonfettiView kv = findViewById(R.id.konfetti_view);
        if (kv == null) return;
        List<Party> parties = Arrays.asList(
            new PartyFactory(new Emitter(4000L, TimeUnit.MILLISECONDS).perSecond(50))
                .angle(Angle.BOTTOM)
                .spread(Spread.ROUND)
                .setSpeedBetween(2f, 9f)
                .colors(Arrays.asList(0xFFFFE040, 0xFF00D4FF, 0xFFD020A0, 0xFFFFB000, 0xFFFFFFFF, 0xFF9020D0))
                .position(0.0, 0.0, 1.0, 0.0)
                .build()
        );
        kv.start(parties);
    }

    /** Возвращает символы для средней строки (row=1) каждого барабана. */
    private int[] generateMiddleRow(int[][] matrix, boolean win) {
        if (matrix != null && matrix.length >= 2 && matrix[1].length >= 3) {
            return matrix[1]; // middle row [1][0..2]
        }
        // Fallback: генерируем случайные символы (TRIAL mode без матрицы)
        SecureRandom rnd = new SecureRandom();
        if (win) {
            // Два одинаковых + один случайный (хотя бы частичный выигрыш)
            int sym = rnd.nextInt(7);
            return new int[]{ sym, sym, rnd.nextInt(7) };
        } else {
            // Три разных
            int a = rnd.nextInt(7);
            int b = (a + 1 + rnd.nextInt(5)) % 7;
            int c = (b + 1 + rnd.nextInt(5)) % 7;
            return new int[]{ a, b, c };
        }
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
        if (spinPlayer != null) spinPlayer.stop();
    }

    // ─── Timeout ────────────────────────────────────────────────────────────

    private void startBetTimeout() {
        int secs = PrefsHelper.get(this).getInt(
                StringEnum.APP_PREFERENCES_BET_TIMEOUT.getValue(), 120);
        timeoutHandler  = new Handler(Looper.getMainLooper());
        timeoutRunnable = this::showTimeout;
        timeoutHandler.postDelayed(timeoutRunnable, secs * 1000L);
    }

    private void cancelBetTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null)
            timeoutHandler.removeCallbacks(timeoutRunnable);
    }

    private void showTimeout() {
        runOnUiThread(() -> {
            if (resultShown) return;
            resultShown = true;
            waitingForResult = false;
            FlasherRun.FLAG  = false;
            stopSound();
            reelLeft.cancelAnim();
            reelCenter.cancelAnim();
            reelRight.cancelAnim();
            tvResultTitle.getPaint().setShader(null);
            tvResultTitle.setTextColor(0xFFFFB000);
            tvResultTitle.setText(getString(R.string.flasher_timeout_title));
            tvResultTitle.setVisibility(View.VISIBLE);
            tvPayout.setText(getString(R.string.flasher_timeout_message));
            tvPayout.setVisibility(View.VISIBLE);
            tvCongratulations.setVisibility(View.GONE);
            llContinueBet.setVisibility(View.GONE);
            btnBackToGame.setVisibility(View.VISIBLE);
            tvCountdown.setVisibility(View.VISIBLE);
            startCountdown();
        });
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
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        noisyReceiver = AudioHelper.registerNoisyReceiver(this, () -> {
            if (spinPlayer != null) spinPlayer.pause();
        });
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);

        // Результат пришёл пока экран был свёрнут: цепочка остановки была прервана.
        // Восстанавливаем реелы с правильными top/mid/bot и сразу показываем итог.
        if (resultShown && !resultUiShown && pendingMatrix != null) {
            uiHandler.removeCallbacksAndMessages(null);
            reelLeft  .setReelOrder(buildVisualOrder(pendingMatrix[0][0], pendingMatrix[1][0], pendingMatrix[2][0]));
            reelCenter.setReelOrder(buildVisualOrder(pendingMatrix[0][1], pendingMatrix[1][1], pendingMatrix[2][1]));
            reelRight .setReelOrder(buildVisualOrder(pendingMatrix[0][2], pendingMatrix[1][2], pendingMatrix[2][2]));
            reelLeft  .snapToPosition(1);
            reelCenter.snapToPosition(1);
            reelRight .snapToPosition(1);
            showResult(pendingResultText, pendingWin);
            return;
        }

        if (waitingForResult) {
            FlasherRun.FLAG = true;
            AppExecutors.io().execute(new FlasherRun());
            startSpinAll();
            if (spinPlayer == null) spinPlayer = new SlotSpinSoundPlayer();
            if (AudioHelper.isSoundEnabled(this)) spinPlayer.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        FlasherRun.FLAG = false;
        cancelCountdown();
        cancelBetTimeout();
        uiHandler.removeCallbacksAndMessages(null);
        reelLeft.cancelAnim();
        reelCenter.cancelAnim();
        reelRight.cancelAnim();
        paylineView.reset();
        stopSound();
        if (resultMediaPlayer != null && resultMediaPlayer.isPlaying()) resultMediaPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
    }

    @Override
    public void onBackPressed() {
        cancelCountdown();
        cancelBetTimeout();
        FlasherRun.FLAG = false;
        reelLeft.cancelAnim();
        reelCenter.cancelAnim();
        reelRight.cancelAnim();
        stopSound();
        if (resultMediaPlayer != null) { resultMediaPlayer.stop(); }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        if (spinPlayer != null) { spinPlayer.release(); spinPlayer = null; }
        if (resultMediaPlayer != null) { resultMediaPlayer.release(); resultMediaPlayer = null; }
        SLOT_FLASHER    = null;
        STOP_POSITIONS  = null;
        RESULT_MATRIX   = null;
    }
}
