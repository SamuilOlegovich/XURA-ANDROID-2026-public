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

import java.security.SecureRandom;

import dagger.hilt.android.AndroidEntryPoint;



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
    // Результирующая матрица 3×3: [row][col], row 0=top 1=mid 2=bot, col 0=left 1=mid 2=right
    public static volatile int[][] RESULT_MATRIX = null;

    public static volatile boolean VISIBLE_ON_SCREEN = false;
    @SuppressLint("StaticFieldLeak")
    public static volatile SlotFlasher SLOT_FLASHER;

    private volatile boolean waitingForResult = true;
    private boolean resultShown   = false;
    private boolean resultUiShown = false;

    // Сохраняем состояние остановки, чтобы восстановить его после onPause/onResume
    private int[]   pendingMidRow     = null;
    private String  pendingResultText = null;
    private boolean pendingWin        = false;

    private static final int REEL_STOP_DELAY_MS = 400; // задержка между остановкой барабанов

    private SlotReelView reelLeft, reelCenter, reelRight;
    private TextView tvResultTitle;
    private TextView tvPayout;
    private TextView tvCongratulations;
    private TextView tvCountdown;
    private View llContinueBet;
    private View btnBackToGame;

    private MediaPlayer spinMediaPlayer;
    private AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver noisyReceiver;

    private final AudioManager.OnAudioFocusChangeListener focusListener = focusChange -> {
        if (spinMediaPlayer == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (spinMediaPlayer.isPlaying()) spinMediaPlayer.pause(); break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                spinMediaPlayer.setVolume(0.1f, 0.1f); break;
            case AudioManager.AUDIOFOCUS_GAIN:
                spinMediaPlayer.setVolume(1f, 1f); break;
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

    // Каждый барабан получает свой порядок символов для визуального разнообразия
    private void assignReelOrders() {
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
     * Если RESULT_MATRIX задан — используем его символы; иначе генерируем случайные.
     * Состояние сохраняется в полях для восстановления после onPause/onResume.
     */
    private void stopReelsSequentially(String resultText, boolean win) {
        int[][] matrix = RESULT_MATRIX;
        int[] midRow = generateMiddleRow(matrix, win);

        // Сохраняем для восстановления если onPause прервёт цепочку
        pendingMidRow     = midRow;
        pendingResultText = resultText;
        pendingWin        = win;

        reelLeft.stopAt(midRow[0], () -> {
            uiHandler.postDelayed(() -> {
                reelCenter.stopAt(midRow[1], () -> {
                    uiHandler.postDelayed(() -> {
                        reelRight.stopAt(midRow[2], () -> {
                            // Все три барабана остановились — показываем результат
                            uiHandler.postDelayed(() -> showResult(resultText, win, midRow), 200);
                        });
                    }, REEL_STOP_DELAY_MS);
                });
            }, REEL_STOP_DELAY_MS);
        });
    }

    /** Показывает результат: подсветка, WIN/LOSE текст, обратный отсчёт. */
    private void showResult(String text, boolean win, int[] midRow) {
        resultUiShown = true;
        stopSound();

        if (win) {
            reelLeft.setHighlightMiddle(true);
            reelCenter.setHighlightMiddle(true);
            reelRight.setHighlightMiddle(true);
        }

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
        if (spinMediaPlayer != null) {
            try { spinMediaPlayer.stop(); } catch (Exception ignored) {}
        }
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
            if (spinMediaPlayer != null && spinMediaPlayer.isPlaying()) spinMediaPlayer.pause();
        });
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);

        // Результат пришёл пока экран был свёрнут: цепочка остановки была прервана.
        // Снапаем все барабаны на нужные символы и сразу показываем итог.
        if (resultShown && !resultUiShown && pendingMidRow != null) {
            uiHandler.removeCallbacksAndMessages(null); // сбрасываем незавершённые задержки
            reelLeft  .snapTo(pendingMidRow[0]);
            reelCenter.snapTo(pendingMidRow[1]);
            reelRight .snapTo(pendingMidRow[2]);
            showResult(pendingResultText, pendingWin, pendingMidRow);
            return;
        }

        if (waitingForResult) {
            FlasherRun.FLAG = true;
            AppExecutors.io().execute(new FlasherRun());
            startSpinAll();
            if (spinMediaPlayer != null && AudioHelper.isSoundEnabled(this)) spinMediaPlayer.start();
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
        stopSound();
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
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        if (spinMediaPlayer != null) { spinMediaPlayer.release(); spinMediaPlayer = null; }
        SLOT_FLASHER = null;
        RESULT_MATRIX = null;
    }
}
