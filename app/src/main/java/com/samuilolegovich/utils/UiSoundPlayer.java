package com.samuilolegovich.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Синглтон для воспроизведения трёх UI-звуков через PCM-синтез (AudioTrack MODE_STATIC).
 * Все звуки генерируются один раз при первом вызове init() и хранятся в памяти.
 *
 * Типы звуков:
 *  NAV    — лёгкий тап: навигация, «назад», выбор игры, правила
 *  SELECT — металлический тинк: чипы ставки, +/−, выбор числа/цвета, ячейки рулетки
 *  ACTION — тяжёлый удар: SPIN, BET, PlaceBet — главная ставка
 */
public class UiSoundPlayer {

    private static final int SAMPLE_RATE = 44100;

    private static volatile UiSoundPlayer instance;

    private final AudioTrack navTrack;
    private final AudioTrack selectTrack;
    private final AudioTrack actionTrack;

    // ─── Параметры NAV: мягкий высокий тап (8 мс) ───────────────────────────
    // Похож на iOS-tap: ненавязчивый, короткий. Используется для любых переходов.
    private static final int    NAV_MS       = 8;
    private static final double NAV_FREQ     = 900.0;
    private static final double NAV_AMP      = 0.38;
    private static final double NAV_DECAY    = 450.0;
    private static final double NAV_NOISE    = 0.12;

    // ─── Параметры SELECT: монета/чип (18 мс) ───────────────────────────────
    // Металлический «тинк» — как укладывание фишки казино на стол.
    private static final int    SEL_MS       = 18;
    private static final double SEL_FREQ     = 650.0;
    private static final double SEL_FREQ2    = 1300.0;  // октавная гармоника
    private static final double SEL_AMP      = 0.50;
    private static final double SEL_DECAY    = 180.0;
    private static final double SEL_NOISE    = 0.14;

    // ─── Параметры ACTION: тяжёлый удар (28 мс) ─────────────────────────────
    // Весомый механический «тхок» — ощущение нажатия физической кнопки/рычага.
    private static final int    ACT_MS       = 28;
    private static final double ACT_FREQ     = 340.0;
    private static final double ACT_FREQ2    = 170.0;   // нижняя субгармоника
    private static final double ACT_AMP      = 0.62;
    private static final double ACT_DECAY    = 85.0;
    private static final double ACT_NOISE    = 0.38;



    private UiSoundPlayer() {
        navTrack    = buildTrack(NAV_MS,  NAV_FREQ,  NAV_FREQ,  NAV_AMP,  NAV_DECAY,  NAV_NOISE,  0.0);
        selectTrack = buildTrack(SEL_MS,  SEL_FREQ,  SEL_FREQ2, SEL_AMP,  SEL_DECAY,  SEL_NOISE,  0.35);
        actionTrack = buildTrack(ACT_MS,  ACT_FREQ,  ACT_FREQ2, ACT_AMP,  ACT_DECAY,  ACT_NOISE,  0.50);
    }

    public static UiSoundPlayer get() {
        if (instance == null) {
            synchronized (UiSoundPlayer.class) {
                if (instance == null) instance = new UiSoundPlayer();
            }
        }
        return instance;
    }

    // ─── Public API ─────────────────────────────────────────────────────────

    /** Лёгкий тап — навигация, «назад», выбор игры, ссылки. */
    public void nav()    { play(navTrack); }

    /** Тинк монеты — чипы ставки, +/−, выбор числа/цвета, ячейка рулетки. */
    public void select() { play(selectTrack); }

    /** Тяжёлый удар — SPIN, BET, PlaceBet. */
    public void action() { play(actionTrack); }

    // ─── Воспроизведение ─────────────────────────────────────────────────────

    private static void play(AudioTrack track) {
        if (track == null) return;
        try {
            synchronized (track) {
                track.stop();
                track.setPlaybackHeadPosition(0);
                track.play();
            }
        } catch (Exception ignored) {}
    }

    // ─── Синтез ──────────────────────────────────────────────────────────────

    /**
     * Строит AudioTrack MODE_STATIC с синтезированным звуком.
     *
     * @param ms       длительность в миллисекундах
     * @param freq1    основная частота (Гц)
     * @param freq2    вторая частота (Гц); если равна freq1 — используется одна
     * @param amp      итоговая амплитуда (0..1)
     * @param decay    скорость экспоненциального затухания
     * @param noiseAmp доля шума в атаке (0 = чистый тон)
     * @param f2ratio  вес второй частоты (0 = только freq1, 0.5 = пополам)
     */
    private static AudioTrack buildTrack(int ms, double freq1, double freq2,
                                         double amp, double decay,
                                         double noiseAmp, double f2ratio) {
        int samples = SAMPLE_RATE * ms / 1000;
        short[] buf = new short[samples];

        // Детерминированный «шум» атаки — не зависит от Random, воспроизводимо
        long seed = 0x12345678L;
        for (int i = 0; i < samples; i++) {
            double t   = (double) i / SAMPLE_RATE;
            double env = Math.exp(-t * decay);

            double tone;
            if (f2ratio > 0) {
                tone = Math.sin(2 * Math.PI * freq1 * t) * (1.0 - f2ratio)
                     + Math.sin(2 * Math.PI * freq2 * t) * f2ratio;
            } else {
                tone = Math.sin(2 * Math.PI * freq1 * t);
            }

            // LCG-шум для атаки (быстро затухает — только первые несколько мс слышны)
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            double noise = ((seed >> 33) & 0x7FFFFFFF) / (double) 0x7FFFFFFF * 2.0 - 1.0;

            double s = (tone * (1.0 - noiseAmp) + noise * noiseAmp) * env * amp;
            if (s >  0.95) s =  0.95;
            if (s < -0.95) s = -0.95;
            buf[i] = (short)(s * 32767);
        }

        int minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufSize = Math.max(minBuf, samples * 2);

        AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufSize, AudioTrack.MODE_STATIC);
        track.write(buf, 0, samples);
        return track;
    }
}
