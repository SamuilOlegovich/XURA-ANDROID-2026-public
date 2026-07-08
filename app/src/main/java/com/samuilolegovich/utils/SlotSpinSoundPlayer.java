package com.samuilolegovich.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Синтезирует звук вращения барабанов слот-машины через PCM (AudioTrack).
 *
 * Архитектура:
 *  - Непрерывный фоновый гул мотора (~120 Гц полосовой шум).
 *  - Клики не генерируются по таймеру — они инжектируются вызовом playClick()
 *    ровно в тот момент, когда SlotReelView сообщает о смене символа (onTickListener).
 *    Так клики синхронизированы с реальной анимацией барабана.
 */
public class SlotSpinSoundPlayer {

    private static final int SAMPLE_RATE  = 44100;
    private static final int CHUNK        = 512;   // размер буфера за одну запись

    // Гул мотора
    private static final double HUM_FREQ  = 120.0;
    private static final double HUM_AMP   = 0.08;

    // Параметры одного клика (ударный механический импульс)
    private static final double CLICK_BODY_FREQ   = 320.0;
    private static final double CLICK_BODY_FREQ2  = 160.0;
    private static final double CLICK_BODY_AMP    = 0.48;
    private static final double CLICK_NOISE_AMP   = 0.38;
    private static final double CLICK_BODY_DECAY  = 70.0;
    private static final double CLICK_NOISE_DECAY = 250.0;
    private static final int    CLICK_SAMPLES     = (int)(SAMPLE_RATE * 0.045); // 45 мс

    private final short[] clickWave = buildClick();

    // -1 = не играем клик; >=0 = текущая позиция в clickWave
    private final AtomicInteger clickPos = new AtomicInteger(-1);

    private AudioTrack       audioTrack;
    private Thread           thread;
    private volatile boolean playing = false;
    private volatile boolean paused  = false;
    private volatile float   volume  = 1.0f;

    // ─── Public API ─────────────────────────────────────────────────────────

    public void start() {
        if (playing) return;
        playing = true;
        paused  = false;
        thread  = new Thread(this::loop, "slot-spin-sound");
        thread.setDaemon(true);
        thread.start();
    }

    /** Вызывается из SlotReelView.onTickListener при каждой смене символа. */
    public void playClick() {
        clickPos.set(0);
    }

    public void pause() {
        paused = true;
        if (audioTrack != null) try { audioTrack.pause(); } catch (Exception ignored) {}
    }

    public void resume() {
        if (!playing) { start(); return; }
        paused = false;
        if (audioTrack != null) try { audioTrack.play(); } catch (Exception ignored) {}
    }

    public void setVolume(float vol) {
        volume = vol;
        if (audioTrack != null) try { audioTrack.setVolume(vol); } catch (Exception ignored) {}
    }

    public void stop() {
        playing = false;
        paused  = false;
        clickPos.set(-1);
        if (audioTrack != null) try { audioTrack.stop(); } catch (Exception ignored) {}
    }

    public void release() {
        stop();
        if (audioTrack != null) { audioTrack.release(); audioTrack = null; }
    }

    // ─── Audio loop ─────────────────────────────────────────────────────────

    private void loop() {
        int minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                Math.max(minBuf, CHUNK * 4), AudioTrack.MODE_STREAM);
        audioTrack.setVolume(volume);
        audioTrack.play();

        short[] buf = new short[CHUNK];
        long globalPos = 0;

        while (playing) {
            if (paused) {
                try { Thread.sleep(16); } catch (InterruptedException e) { break; }
                continue;
            }

            for (int i = 0; i < CHUNK; i++) {
                double t = (double)(globalPos + i) / SAMPLE_RATE;

                // Фоновый гул мотора
                double hum = Math.sin(2 * Math.PI * HUM_FREQ * t) * HUM_AMP;

                // Инжекция клика
                double click = 0;
                int cp = clickPos.get();
                if (cp >= 0 && cp < CLICK_SAMPLES) {
                    click = clickWave[cp] / 32767.0;
                    clickPos.incrementAndGet();
                } else if (cp >= CLICK_SAMPLES) {
                    clickPos.set(-1);
                }

                double s = hum + click;
                if (s >  0.95) s =  0.95;
                if (s < -0.95) s = -0.95;
                buf[i] = (short)(s * 32767);
            }

            globalPos += CHUNK;
            if (audioTrack.write(buf, 0, CHUNK) < 0) break;
        }
    }

    // ─── Предгенерация формы клика ───────────────────────────────────────────

    private static short[] buildClick() {
        short[] w = new short[CLICK_SAMPLES];
        for (int i = 0; i < CLICK_SAMPLES; i++) {
            double t    = (double) i / SAMPLE_RATE;
            double body = (Math.sin(2 * Math.PI * CLICK_BODY_FREQ  * t) * 0.6
                         + Math.sin(2 * Math.PI * CLICK_BODY_FREQ2 * t) * 0.4)
                         * Math.exp(-t * CLICK_BODY_DECAY) * CLICK_BODY_AMP;
            // Шум атаки — псевдослучайный (детерминированный для buildClick)
            double noise = ((i * 1103515245 + 12345) & 0x7fffffff) / (double)0x7fffffff * 2 - 1;
            noise *= Math.exp(-t * CLICK_NOISE_DECAY) * CLICK_NOISE_AMP;
            double s = body + noise;
            if (s >  0.95) s =  0.95;
            if (s < -0.95) s = -0.95;
            w[i] = (short)(s * 32767);
        }
        return w;
    }
}
