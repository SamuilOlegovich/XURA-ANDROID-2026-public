package com.samuilolegovich.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;

/**
 * Копирует чувствительный текст (seed-фраза, приватный ключ) в буфер обмена
 * и автоматически очищает его через таймаут, если пользователь не успел
 * перезаписать буфер чем-то другим. Сужает окно, в течение которого секрет
 * доступен другим приложениям через буфер обмена или его историю.
 */
public final class ClipboardUtil {
    /** Приватный конструктор запрещает создание экземпляров — класс статический. */
    private ClipboardUtil() {}

    private static final long AUTO_CLEAR_DELAY_MS = 45_000L;

    /**
     * Кладёт текст в буфер обмена (помечая его как чувствительный на Android 13+, чтобы
     * система скрыла превью), а затем планирует его очистку через AUTO_CLEAR_DELAY_MS —
     * но только если к тому моменту буфер всё ещё содержит именно этот текст.
     */
    public static void copyWithAutoClear(Context context, String label, String text) {
        if (text == null || text.isEmpty()) return;

        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);

        // Просим систему скрыть превью содержимого в clipboard UI (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PersistableBundle extras = new PersistableBundle();
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true);
            clip.getDescription().setExtras(extras);
        }

        cm.setPrimaryClip(clip);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ClipData current = cm.getPrimaryClip();
            if (current != null && current.getItemCount() > 0
                    && text.equals(String.valueOf(current.getItemAt(0).getText()))) {
                cm.setPrimaryClip(ClipData.newPlainText("", ""));
            }
        }, AUTO_CLEAR_DELAY_MS);
    }
}
