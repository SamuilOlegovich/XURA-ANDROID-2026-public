package com.samuilolegovich.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.samuilolegovich.R;



/** SoundPool-обёртка для коротких одноразовых звуков (ставка, ошибка). */
public class GameSoundPool {

    private SoundPool soundPool;
    private final int betSoundId;
    private final int errorSoundId;

    public GameSoundPool(Context context) {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();

        betSoundId   = soundPool.load(context, R.raw.bet,   1);
        errorSoundId = soundPool.load(context, R.raw.error, 1);
    }

    /** Воспроизводит звук ставки (если звук включён). */
    public void playBet(Context context) {
        if (soundPool == null || !AudioHelper.isSoundEnabled(context)) return;
        soundPool.play(betSoundId, 1f, 1f, 1, 0, 1f);
    }

    /** Воспроизводит звук ошибки (если звук включён). */
    public void playError(Context context) {
        if (soundPool == null || !AudioHelper.isSoundEnabled(context)) return;
        soundPool.play(errorSoundId, 1f, 1f, 1, 0, 1f);
    }

    /** Освобождает ресурсы SoundPool. */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
