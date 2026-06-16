package com.samuilolegovich.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView, который всегда считает себя в фокусе ({@link #isFocused()} возвращает true).
 * Android прокручивает (marquee) однострочный текст с ellipsize="marquee" только у TextView,
 * находящегося в фокусе, поэтому этот трюк нужен, чтобы бегущая строка работала
 * даже на элементах, которые реально фокус не получают (например, в списках).
 */
public class ScrollingTextView extends TextView {
    /** Конструктор для инфлейта View из XML с явным указанием стиля. */
    public ScrollingTextView(Context context, AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
    }

    /** Конструктор для инфлейта View из XML-разметки. */
    public ScrollingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Конструктор для создания View из кода. */
    public ScrollingTextView(Context context) {
        super(context);
    }

    /** Передаёт реальной системе фокуса только событие получения фокуса, игнорируя его потерю, чтобы прокрутка не останавливалась. */
    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    /** Аналогично onFocusChanged — реагирует только на получение фокуса окном, не на его потерю. */
    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) {
            super.onWindowFocusChanged(focused);
        }
    }

    /** Всегда возвращает true, чтобы Android считал View находящимся в фокусе и продолжал анимацию marquee. */
    @Override
    public boolean isFocused() {
        return true;
    }
}
