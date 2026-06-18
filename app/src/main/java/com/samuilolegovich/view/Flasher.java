package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.samuilolegovich.AppExecutors;
import com.samuilolegovich.BaseActivity;
import androidx.core.content.ContextCompat;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.async.runnable.FlasherRun;
import com.samuilolegovich.async.runnable.NotifierRunForTrialGame;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.Lotto;
import dagger.hilt.android.AndroidEntryPoint;

import java.math.BigDecimal;
import java.util.LinkedHashMap;




/**
 * Экран ожидания результата ставки в рулетке: показывает анимацию вращающегося колеса,
 * слушает ответ сервера (через FlasherRun) либо локальную симуляцию для тестового режима
 * (NotifierRunForTrialGame), затем останавливает колесо на выигравшем числе и отображает
 * итог ставки (выигрыш/проигрыш) со звуком и обратным отсчётом до возврата на экран игры.
 */
@AndroidEntryPoint
public class Flasher extends BaseActivity {
    public static final String FLASHER_CLASS = ".Flasher";

    public static volatile boolean VISIBLE_ON_SCREEN = false;

    public static volatile TestModeEnum TEST_MODE_ENUM;
    @SuppressLint("StaticFieldLeak")
    public static volatile Flasher FLASHER;

    public static String TEST_SAND_AMOUNT;
    public static String NUMBER_BET;
    public static Boolean COLOR_BET;

    // Roulette-specific fields
    public static volatile String ROULETTE_BET_TAG;
    public static volatile int ROULETTE_WIN_MULTIPLIER = 2;
    public static volatile LinkedHashMap<String, BigDecimal> ROULETTE_ALL_BETS;

    private volatile boolean FLAG;

    private MediaPlayer rouletteSpinMediaPlayer;

    private AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver noisyReceiver;
    private boolean gameResultShown = false;
    private boolean lastResultWin   = false;

    private final AudioManager.OnAudioFocusChangeListener focusListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (rouletteSpinMediaPlayer != null && rouletteSpinMediaPlayer.isPlaying())
                    rouletteSpinMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (rouletteSpinMediaPlayer != null) rouletteSpinMediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (rouletteSpinMediaPlayer != null) rouletteSpinMediaPlayer.setVolume(1f, 1f);
                break;
        }
    };

    private RouletteWheelView wheelView;
    private TextView numberInfo;
    private TextView infoThree;
    private TextView infoTwo;
    private TextView winInfo;
    private View btnBackToGame;
    private View llContinueBet;
    private TextView tvCountdown;

    private Handler countdownHandler;
    private Runnable countdownRunnable;

    private String CONGRATULATIONS;
    private String DONT_GIVE_UP;
    private String BET_LOST;
    private String BET_WON;



    /** Инициализирует экран, запускает анимацию вращения и поток ожидания ответа от сервера (или его локальную симуляцию в тестовом режиме). */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flasher);
        FLASHER = this;
        FLAG = true;
        setButtons();
        setLanguage();
        setSound();
        goThread();

        if (!MainActivity.IS_REAL_GAME_MODE) {
            goThreadTest();
        }
    }



    /** Готовит звук вращения рулетки и запускает анимацию колеса. */
    private void setSound() {
        rouletteSpinMediaPlayer = MediaPlayer.create(this, R.raw.roulette_spin);
        rouletteSpinMediaPlayer.setLooping(true);
        wheelView.setCenterColor(0xFF000000);
        wheelView.startSpinning();
    }


    /** Находит View разметки экрана и назначает обработчики кнопок возврата к игре. */
    private void setButtons() {
        wheelView      = (RouletteWheelView) findViewById(R.id.roulette_wheel);
        infoThree      = (TextView) findViewById(R.id.last_text_view_tree);
        numberInfo     = (TextView) findViewById(R.id.number_info_text);
        infoTwo        = (TextView) findViewById(R.id.last_text_view_two);
        winInfo        = (TextView) findViewById(R.id.last_text_view);
        btnBackToGame  = findViewById(R.id.btn_back_to_game);
        llContinueBet  = findViewById(R.id.ll_continue_bet);
        tvCountdown    = findViewById(R.id.tv_countdown);

        btnBackToGame.setOnClickListener(v -> {
            cancelCountdown();
            onBackPressed();
        });

        llContinueBet.setOnClickListener(v -> onBackPressed());
    }


    /** Загружает локализованные строки для итоговых сообщений (поздравление/утешение, выигрыш/проигрыш). */
    private void setLanguage() {
        CONGRATULATIONS = getString(R.string.congratulations);
        DONT_GIVE_UP    = getString(R.string.dont_give_up);
        BET_LOST        = getString(R.string.bet_lost);
        BET_WON         = getString(R.string.bet_won);
    }


    /** Вызывается из FlasherRun каждые 300 мс во время ожидания ответа — визуал отвечает колесо, здесь делать ничего не нужно. */
    public void setColorAndText(String text, boolean b) { }


    /** Завершает ожидание и переводит экран в режим показа итога ставки (вызывается из фонового потока, переключается на UI-поток). */
    public void stopGame(String text, boolean win) {
        runOnUiThread(() -> {
            FLAG = false;
            FlasherRun.FLAG = false;
            gameResultShown = true;
            lastResultWin   = win;
            gameStop(text, win);
        });
    }


    /** Останавливает звук вращения, доводит колесо до выигравшего числа и показывает все элементы итога ставки (число, цвет, текст, звук, отсчёт). */
    @SuppressLint("SetTextI18n")
    private void gameStop(String text, boolean win) {
        rouletteSpinMediaPlayer.stop();

        int displayNumber = resolveDisplayNumber(win);

        wheelView.stopAtNumber(displayNumber, () -> {
            wheelView.setCenterColor(sectorColor(displayNumber));
            numberInfo.setText(win ? NUMBER_BET : String.valueOf(displayNumber));
            numberInfo.setTextColor(0xFFFFB000);
            numberInfo.setVisibility(View.VISIBLE);

            winInfo.setText(win ? BET_WON : BET_LOST);
            winInfo.setVisibility(View.VISIBLE);
            winInfo.post(() -> applyTextGradient(winInfo, win));

            infoThree.setText(win ? CONGRATULATIONS : DONT_GIVE_UP);
            infoThree.setVisibility(View.VISIBLE);

            infoTwo.setText(text);
            infoTwo.setVisibility(View.VISIBLE);

            llContinueBet.setVisibility(View.GONE);
            btnBackToGame.setVisibility(View.VISIBLE);
            tvCountdown.setVisibility(View.VISIBLE);
            startCountdown();
        });
    }


    /** Запускает обратный отсчёт 10 секунд, по истечении которого автоматически выполняется переход назад к экрану игры. */
    private void startCountdown() {
        final int[] seconds = {10};
        tvCountdown.setText(getString(R.string.flasher_return_in, seconds[0]));

        countdownHandler = new Handler(Looper.getMainLooper());
        countdownRunnable = new Runnable() {
            @Override public void run() {
                seconds[0]--;
                if (seconds[0] <= 0) {
                    onBackPressed();
                } else {
                    tvCountdown.setText(getString(R.string.flasher_return_in, seconds[0]));
                    countdownHandler.postDelayed(this, 1000);
                }
            }
        };
        countdownHandler.postDelayed(countdownRunnable, 1000);
    }


    /** Останавливает запущенный обратный отсчёт (например, при ручном выходе с экрана раньше его завершения). */
    private void cancelCountdown() {
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }


    /** Накладывает на текст итога градиент (золотой для выигрыша, красно-серый для проигрыша) для визуального эффекта. */
    private void applyTextGradient(TextView tv, boolean win) {
        float w = tv.getWidth();
        if (w == 0) return;
        int[] colors = win
                ? new int[]{0xFFFFE040, 0xFFFFB000, 0xFFFF6A00, 0xFFFFB000, 0xFFFFE040}
                : new int[]{0xFFFF3060, 0xFF990022, 0xFF666688, 0xFF333355};
        tv.getPaint().setShader(
                new LinearGradient(0, 0, w, 0, colors, null, Shader.TileMode.CLAMP));
        tv.invalidate();
    }


    /** Возвращает цвет сектора рулетки для числа n: зелёный для зеро, чёрный или красный для остальных. */
    private int sectorColor(int n) {
        if (n == 0)                                          return 0xFF007040; // green
        if (Lotto.learnTheColorOfNumber(String.valueOf(n))) return 0xFF111111; // black
        return 0xFFC81030;                                                      // red
    }


    /** Определяет число, на котором нужно остановить колесо — это NUMBER_BET, заданный сервером/симуляцией при ставке. */
    private int resolveDisplayNumber(boolean win) {
        try {
            int n = Integer.parseInt(NUMBER_BET);
            if (n >= 0 && n <= 36) return n;
        } catch (Exception ignored) {}
        return 0;
    }


    /** Окрашивает системные навигационную и статус-бар панели в цвет, соответствующий состоянию экрана. */
    private void setColorNavigation(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            switch (color) {
                case 0:
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.yellow_6));
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.yellow_6));
                    break;
                case 1:
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
                    break;
            }
        }
    }


    /** Запускает на IO-потоке FlasherRun — ожидание реального ответа сервера по ставке. */
    private void goThread() {
        AppExecutors.io().execute(new FlasherRun());
    }


    /** Запускает на IO-потоке локальную симуляцию ответа для тестового (не реального) режима игры. */
    private void goThreadTest() {
        AppExecutors.io().execute(new NotifierRunForTrialGame(TEST_MODE_ENUM));
    }


    /** При уходе с экрана приостанавливает все звуки, освобождает аудиофокус, останавливает поток и анимацию колеса. */
    @Override
    protected void onPause() {
        super.onPause();
        VISIBLE_ON_SCREEN = false;
        FlasherRun.FLAG = false;
        cancelCountdown();
        if (wheelView != null) wheelView.stopSpinning();
        if (rouletteSpinMediaPlayer != null && rouletteSpinMediaPlayer.isPlaying()) rouletteSpinMediaPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        audioFocusRequest = null;
        noisyReceiver = null;
    }


    /** При возвращении запрашивает аудиофокус и: если спин ещё идёт — возобновляет вращение и звук; если результат уже показан — возобновляет win/lost звук. */
    @Override
    protected void onResume() {
        super.onResume();
        VISIBLE_ON_SCREEN = true;
        noisyReceiver = AudioHelper.registerNoisyReceiver(this, () -> {
            if (rouletteSpinMediaPlayer != null && rouletteSpinMediaPlayer.isPlaying())
                rouletteSpinMediaPlayer.pause();
        });
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);
        if (FLAG) {
            FlasherRun.FLAG = true;
            goThread();
            if (wheelView != null) wheelView.startSpinning();
            if (rouletteSpinMediaPlayer != null && AudioHelper.isSoundEnabled(this))
                rouletteSpinMediaPlayer.start();
        }
    }


    /** Останавливает все звуки и анимацию, возвращает цвет навигации к исходному и закрывает экран. */
    @Override
    public void onBackPressed() {
        cancelCountdown();
        if (wheelView != null) wheelView.stopSpinning();
        if (rouletteSpinMediaPlayer != null) rouletteSpinMediaPlayer.stop();
        setColorNavigation(1);
        FlasherRun.FLAG = false;
        super.onBackPressed();
    }

    /** Освобождает ресурсы MediaPlayer, аудиофокус и сбрасывает статическую ссылку на Activity. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        if (rouletteSpinMediaPlayer != null) { rouletteSpinMediaPlayer.release(); rouletteSpinMediaPlayer = null; }
        FLASHER = null;
    }
}
