package com.samuilolegovich.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;


import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.utils.AudioHelper;

import static com.samuilolegovich.view.GuessTheNumberGame.GUESS_THE_NUMBER_GAME_CLASS;
import static com.samuilolegovich.view.GuessTheColorGame.GUESS_THE_COLOR_GAME_CLASS;
import static com.samuilolegovich.view.RouletteGame.ROULETTE_GAME_CLASS;
import static com.samuilolegovich.view.SlotGame.SLOT_GAME_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран выбора игры (рулетка, угадай число, угадай цвет): фоновая музыка, навигация
 * на экран выбранной игры и декоративная волнообразная анимация "подпрыгивания" карточек
 * с толчком логотипа, повторяющаяся по таймеру.
 */
@AndroidEntryPoint
public class SelectGame extends BaseActivity {
    public static final String SELECT_GAME_CLASS = ".SelectGame";

    public static volatile SelectGame SELECT_GAME_ACTIVITY;

    private static final long WAVE_INITIAL_DELAY  = 600L;   // пауза после окончания входной анимации
    private static final long WAVE_REPEAT_DELAY   = 3500L;  // пауза между волнами
    private static final int  WAVE_STAGGER_MS     = 100;    // задержка между карточками в волне
    private static final int  WAVE_BOUNCE_DP      = 20;     // высота подпрыгивания

    private static final long INTRO_STAGGER_MS    = 110L;   // задержка между карточками при входе
    private static final long INTRO_CARD_DURATION = 380L;   // длительность анимации одной карточки
    private static final long INTRO_LOGO_DURATION = 350L;   // длительность появления лого

    private MediaPlayer flourOfChoiceMediaPlayer;
    private AudioFocusRequest audioFocusRequest;
    private BroadcastReceiver noisyReceiver;

    private final AudioManager.OnAudioFocusChangeListener focusListener = focusChange -> {
        if (flourOfChoiceMediaPlayer == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (flourOfChoiceMediaPlayer.isPlaying()) flourOfChoiceMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                flourOfChoiceMediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                flourOfChoiceMediaPlayer.setVolume(0.5f, 0.5f);
                if (!flourOfChoiceMediaPlayer.isPlaying() && AudioHelper.isSoundEnabled(this))
                    flourOfChoiceMediaPlayer.start();
                break;
        }
    };

    private View     logo;
    private TextView selectTextView;
    private TextView tvGameModeBadge;
    private View guessTheNumber;
    private View guessTheColor;
    private View roulette;
    private View slot;

    private Handler waveHandler;
    private Runnable waveRunnable;



    /** Инициализирует экран: сохраняет ссылку на активити, разметку, View, локализацию, фоновую музыку, слушателей и нижнюю навигацию. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_game_page);
        SELECT_GAME_ACTIVITY = this;
        setButtons();
        setLanguage();
        setSound();
        listeners();
        setupBottomNav();
        waveHandler = new Handler(Looper.getMainLooper());
    }



    /** Создаёт плеер фоновой музыки (старт откладывается до onResume). */
    private void setSound() {
        flourOfChoiceMediaPlayer = MediaPlayer.create(this, R.raw.flour_of_choice);
        flourOfChoiceMediaPlayer.setVolume(0.5f, 0.5f);
        flourOfChoiceMediaPlayer.setLooping(true);
    }


    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        logo            = findViewById(R.id.logo_xura);
        guessTheNumber  = findViewById(R.id.double_your_bet_linc);
        guessTheColor   = findViewById(R.id.guess_the_color_linc);
        selectTextView  = (TextView) findViewById(R.id.select_text_view);
        roulette        = findViewById(R.id.roulette_linc);
        slot            = findViewById(R.id.slot_linc);
        tvGameModeBadge = findViewById(R.id.tv_game_mode_badge);
    }

    /** Отключает обрезку на root/scroll/inner — нужно только во время входной анимации. */
    private void disableClipping() {
        ViewGroup root = (ViewGroup) findViewById(R.id.select_game);
        if (root != null) root.setClipChildren(false);
        ViewGroup scroll = (ViewGroup) findViewById(R.id.games_scroll);
        if (scroll != null) {
            scroll.setClipChildren(false);
            scroll.setClipToPadding(false);
            View inner = scroll.getChildAt(0);
            if (inner instanceof ViewGroup) ((ViewGroup) inner).setClipChildren(false);
        }
    }

    /** Восстанавливает обрезку после входной анимации — fadingEdge и bounce работают корректно. */
    private void enableClipping() {
        ViewGroup root = (ViewGroup) findViewById(R.id.select_game);
        if (root != null) root.setClipChildren(true);
        ViewGroup scroll = (ViewGroup) findViewById(R.id.games_scroll);
        if (scroll != null) {
            scroll.setClipChildren(true);
            scroll.setClipToPadding(true);
            View inner = scroll.getChildAt(0);
            if (inner instanceof ViewGroup) ((ViewGroup) inner).setClipChildren(true);
        }
    }


    /** Устанавливает локализованный текст заголовка экрана и обновляет бейдж режима игры. */
    private void setLanguage() {
        selectTextView.setText(R.string.select_game);
        updateGameModeBadge();
    }

    /** Показывает бейдж текущего режима игры: TRIAL (голубой) или LIVE (золотой). */
    private void updateGameModeBadge() {
        if (tvGameModeBadge == null) return;
        boolean isReal = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
        if (isReal) {
            tvGameModeBadge.setText(R.string.badge_live_mode);
            tvGameModeBadge.setBackgroundResource(R.drawable.bg_chip_live);
        } else {
            tvGameModeBadge.setText(R.string.badge_trial_mode);
            tvGameModeBadge.setBackgroundResource(R.drawable.bg_chip_trial);
        }
    }


    /** Назначает обработчики кнопок выбора игры: onPause() сам остановит музыку при переходе. */
    private void listeners() {
        guessTheColor.setOnClickListener(v -> {
            pulse(v); soundNav();
            goToAnotherPage(GUESS_THE_COLOR_GAME_CLASS);
        });

        guessTheNumber.setOnClickListener(v -> {
            pulse(v); soundNav();
            goToAnotherPage(GUESS_THE_NUMBER_GAME_CLASS);
        });

        roulette.setOnClickListener(v -> {
            pulse(v); soundNav();
            goToAnotherPage(ROULETTE_GAME_CLASS);
        });

        if (slot != null) slot.setOnClickListener(v -> {
            pulse(v); soundNav();
            goToAnotherPage(SLOT_GAME_CLASS);
        });
    }


    /** Запускает Activity по имени её класса/действия. */
    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    /** Паузит музыку, освобождает аудиофокус, отписывается от наушников, останавливает анимации и сбрасывает состояние View. */
    @Override
    protected void onPause() {
        super.onPause();
        if (flourOfChoiceMediaPlayer != null && flourOfChoiceMediaPlayer.isPlaying())
            flourOfChoiceMediaPlayer.pause();
        AudioHelper.abandonFocus(this, audioFocusRequest);
        AudioHelper.unregisterNoisyReceiver(this, noisyReceiver);
        audioFocusRequest = null;
        noisyReceiver = null;
        stopWave();
        resetViewsToNormal();
    }


    /** Запрашивает аудиофокус, регистрирует приёмник наушников, запускает музыку (если не замьючено) и входную анимацию. */
    @Override
    protected void onResume() {
        super.onResume();
        updateGameModeBadge();
        noisyReceiver = AudioHelper.registerNoisyReceiver(this,
                () -> { if (flourOfChoiceMediaPlayer != null && flourOfChoiceMediaPlayer.isPlaying())
                            flourOfChoiceMediaPlayer.pause(); });
        audioFocusRequest = AudioHelper.requestFocus(this, focusListener);
        if (flourOfChoiceMediaPlayer != null && AudioHelper.isSoundEnabled(this))
            flourOfChoiceMediaPlayer.start();
        if (Settings.isAnimationsEnabled(this)) {
            playEntranceAndStartWave();
        }
    }


    // ─── Входная анимация ───────────────────────────────────────────────────

    /** Возвращает карточки игр в порядке снизу вверх (slot → roulette → число → цвет). */
    private View[] buildCardList() {
        if (slot != null) return new View[]{ slot, roulette, guessTheNumber, guessTheColor };
        return new View[]{ roulette, guessTheNumber, guessTheColor };
    }

    /**
     * Входная анимация при каждом открытии экрана:
     * карточки влетают снизу одна за другой, затем появляется лого с заголовком,
     * после чего стартует обычная волновая анимация.
     */
    private void playEntranceAndStartWave() {
        stopWave();
        disableClipping(); // карточки летят снизу за пределами ScrollView
        View[] cards = buildCardList();
        float slideFromY = 150 * getResources().getDisplayMetrics().density;

        // Сбрасываем все View в начальное состояние (немедленно, до следующего кадра)
        for (View c : cards) {
            if (c == null) continue;
            c.animate().cancel();
            c.setAlpha(0f);
            c.setTranslationY(slideFromY);
        }
        if (logo           != null) { logo          .animate().cancel(); logo          .setAlpha(0f); }
        if (selectTextView != null) { selectTextView.animate().cancel(); selectTextView.setAlpha(0f); }
        if (tvGameModeBadge!= null) { tvGameModeBadge.animate().cancel(); tvGameModeBadge.setAlpha(0f); }

        // Запускаем анимации в следующем кадре — гарантируем что initial state уже применён
        waveHandler.post(() -> {
            for (int i = 0; i < cards.length; i++) {
                if (cards[i] == null) continue;
                ViewPropertyAnimator anim = cards[i].animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(INTRO_CARD_DURATION)
                        .setStartDelay((long) i * INTRO_STAGGER_MS)
                        .setInterpolator(new DecelerateInterpolator(2f));

                // Цепочка: последняя карточка → текст + плашка → лого (последним)
                if (i == cards.length - 1) {
                    anim.withEndAction(() -> {
                        // Шаг 1: надпись и плашка режима
                        if (tvGameModeBadge != null)
                            tvGameModeBadge.animate().alpha(1f).setDuration(INTRO_LOGO_DURATION).setStartDelay(50L).setInterpolator(null);
                        if (selectTextView != null)
                            selectTextView.animate().alpha(1f).setDuration(INTRO_LOGO_DURATION).setInterpolator(null)
                                .withEndAction(() -> {
                                    // Шаг 2: лого — последним
                                    if (logo != null)
                                        logo.animate().alpha(1f).setDuration(INTRO_LOGO_DURATION).setInterpolator(null)
                                            .withEndAction(this::enableClipping);
                                });
                    });
                }
            }
        });

        // Волна начинается после окончания всей входной анимации + стандартная пауза
        // цепочка: карточки (stagger + duration) → текст (duration) → лого (duration)
        long waveDelay = (long)(cards.length - 1) * INTRO_STAGGER_MS + INTRO_CARD_DURATION + INTRO_LOGO_DURATION * 2 + WAVE_INITIAL_DELAY;
        startWaveDelayed(waveDelay);
    }

    /** Сбрасывает все View в нормальное состояние (alpha=1, translationY=0) и отменяет их анимации. */
    private void resetViewsToNormal() {
        View[] cards = buildCardList();
        for (View c : cards) {
            if (c == null) continue;
            c.animate().cancel();
            c.setAlpha(1f);
            c.setTranslationY(0f);
        }
        if (logo           != null) { logo          .animate().cancel(); logo          .setAlpha(1f); logo.setTranslationY(0f); }
        if (selectTextView != null) { selectTextView.animate().cancel(); selectTextView.setAlpha(1f); }
        if (tvGameModeBadge!= null) { tvGameModeBadge.animate().cancel(); tvGameModeBadge.setAlpha(1f); }
    }


    // ─── Волновая анимация подпрыгивания ────────────────────────────────────

    /** Запускает волновую анимацию с указанной задержкой перед первым запуском. */
    private void startWaveDelayed(long delay) {
        stopWave();
        waveRunnable = new Runnable() {
            @Override public void run() {
                playWave();
                waveHandler.postDelayed(this, WAVE_REPEAT_DELAY);
            }
        };
        waveHandler.postDelayed(waveRunnable, delay);
    }

    /** Отменяет запланированный повтор волновой анимации. */
    private void stopWave() {
        if (waveRunnable != null) {
            waveHandler.removeCallbacks(waveRunnable);
            waveRunnable = null;
        }
    }

    /** Запускает одну волну подпрыгивания карточек игр снизу вверх с нарастающей задержкой, а затем — толчок логотипа. */
    private void playWave() {
        View[] cards = buildCardList();
        float bounceY = WAVE_BOUNCE_DP * getResources().getDisplayMetrics().density;

        for (int i = 0; i < cards.length; i++) {
            animateBounce(cards[i], (long) i * WAVE_STAGGER_MS, bounceY);
        }

        // Когда волна дошла до верхней карточки — логотип получает толчок снизу
        long logoDelay = (long) (cards.length - 1) * WAVE_STAGGER_MS + 170L;
        animateLogoJolt(logoDelay);
    }

    /** Анимирует одну карточку: быстрый подъём с замедлением в пике, затем приземление с эффектом пружины. */
    private void animateBounce(View card, long startDelay, float bounceY) {
        ObjectAnimator up = ObjectAnimator.ofFloat(card, "translationY", 0f, -bounceY);
        up.setDuration(170);
        up.setInterpolator(new DecelerateInterpolator(1.5f));

        ObjectAnimator down = ObjectAnimator.ofFloat(card, "translationY", -bounceY, 0f);
        down.setDuration(230);
        down.setInterpolator(new OvershootInterpolator(1.8f));

        AnimatorSet bounce = new AnimatorSet();
        bounce.playSequentially(up, down);
        bounce.setStartDelay(startDelay);
        bounce.start();
    }

    /** Анимирует лёгкий толчок логотипа вниз-вверх, имитируя удар волны подпрыгивающих карточек снизу. */
    private void animateLogoJolt(long startDelay) {
        if (logo == null) return;
        float joltY = 8 * getResources().getDisplayMetrics().density;

        ObjectAnimator down = ObjectAnimator.ofFloat(logo, "translationY", 0f, joltY);
        down.setDuration(80);
        down.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator up = ObjectAnimator.ofFloat(logo, "translationY", joltY, 0f);
        up.setDuration(200);
        up.setInterpolator(new OvershootInterpolator(2.5f));

        AnimatorSet jolt = new AnimatorSet();
        jolt.playSequentially(down, up);
        jolt.setStartDelay(startDelay);
        jolt.start();
    }


    /** onPause() уже остановил музыку — здесь просто закрываем экран. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /** Освобождает MediaPlayer и сбрасывает статическую ссылку на активити. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flourOfChoiceMediaPlayer != null) {
            flourOfChoiceMediaPlayer.release();
            flourOfChoiceMediaPlayer = null;
        }
        SELECT_GAME_ACTIVITY = null;
    }
}
