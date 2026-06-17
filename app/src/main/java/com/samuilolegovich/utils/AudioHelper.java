package com.samuilolegovich.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

import com.samuilolegovich.enums.StringEnum;



/**
 * Утилиты для управления аудио: фокус, наушники, настройка мута.
 * minSdk=28, поэтому используем AudioFocusRequest API 26+ напрямую.
 */
public class AudioHelper {

    /** Запрашивает аудиофокус для игровой музыки. Возвращает объект запроса для последующего освобождения. */
    public static AudioFocusRequest requestFocus(Context context,
                                                  AudioManager.OnAudioFocusChangeListener listener) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return null;

        AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setOnAudioFocusChangeListener(listener)
                .build();

        am.requestAudioFocus(request);
        return request;
    }

    /** Освобождает ранее запрошенный аудиофокус. */
    public static void abandonFocus(Context context, AudioFocusRequest request) {
        if (request == null) return;
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) am.abandonAudioFocusRequest(request);
    }

    /**
     * Регистрирует BroadcastReceiver на ACTION_AUDIO_BECOMING_NOISY (отключение наушников).
     * Возвращает receiver для последующей отмены регистрации.
     */
    public static BroadcastReceiver registerNoisyReceiver(Context context, Runnable onNoisy) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                    onNoisy.run();
                }
            }
        };
        context.registerReceiver(receiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        return receiver;
    }

    /** Отменяет регистрацию BroadcastReceiver для наушников. */
    public static void unregisterNoisyReceiver(Context context, BroadcastReceiver receiver) {
        if (receiver == null) return;
        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {}
    }

    /** Возвращает true, если звук в приложении включён пользователем. */
    public static boolean isSoundEnabled(Context context) {
        return PrefsHelper.get(context).getBoolean(
                StringEnum.APP_PREFERENCES_SOUND_ENABLED.getValue(), true);
    }

    /** Сохраняет настройку звука в preferences. */
    public static void setSoundEnabled(Context context, boolean enabled) {
        PrefsHelper.get(context).edit()
                .putBoolean(StringEnum.APP_PREFERENCES_SOUND_ENABLED.getValue(), enabled)
                .apply();
    }
}
