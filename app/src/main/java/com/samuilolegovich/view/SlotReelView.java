package com.samuilolegovich.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Кастомный View — один барабан слот-машины.
 * Показывает 3 ячейки (символа) из 7, непрерывно прокручивает их при вращении,
 * при команде stopAt() замедляется и останавливается нужным символом в средней ячейке.
 */
public class SlotReelView extends View {

    public static final int SYM_XRP     = 0;
    public static final int SYM_ROCKET  = 1;
    public static final int SYM_MOON    = 2;
    public static final int SYM_DIAMOND = 3;
    public static final int SYM_WHALE   = 4;
    public static final int SYM_JACKPOT = 5;
    public static final int SYM_WILD    = 6;

    // Text glyphs drawn in the center of each cell
    static final String[] SYM_GLYPH = { "X", "▲", "◑", "◆", "W", "★", "≋" };
    // Short label below glyph
    static final String[] SYM_LABEL = { "XRP", "RKT", "MOON", "DIA", "WHAL", "JKPT", "WILD" };
    // Accent color per symbol
    static final int[] SYM_COLORS = {
        0xFF00D4FF, // XRP     – cyan
        0xFF4080FF, // Rocket  – blue
        0xFF9090FF, // Moon    – light purple
        0xFF40D0FF, // Diamond – light cyan
        0xFF9020D0, // Whale   – purple
        0xFFFFB000, // Jackpot – gold
        0xFFFF2080, // Wild    – pink
    };

    // Symbol order on this reel (7 unique symbols, can be shuffled per reel)
    private int[] reelOrder = { 0, 1, 2, 3, 4, 5, 6 };

    private float cellPx          = 0f;
    private float scrollPx        = 0f;   // ever-increasing during spin
    private float pendingSpinMult = -1f;  // >=0 means startSpin was called before layout
    private float currentSpeedMult = 1.0f; // last speed used in doStartSpin

    private ValueAnimator activeAnimator;
    private boolean highlightMiddle = false;

    private final Paint bgPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glyphPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hlPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect    = new RectF();

    public SlotReelView(Context ctx) {
        super(ctx);
        init();
    }

    public SlotReelView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init() {
        glyphPaint.setTextAlign(Paint.Align.CENTER);
        glyphPaint.setTypeface(Typeface.DEFAULT_BOLD);
        glyphPaint.setFakeBoldText(true);

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(0x80FFFFFF);
        labelPaint.setTypeface(Typeface.DEFAULT);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(0x28FFFFFF);
        borderPaint.setStrokeWidth(1.5f);

        hlPaint.setStyle(Paint.Style.STROKE);
        hlPaint.setColor(0xFFFFB000);
        hlPaint.setStrokeWidth(3f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        cellPx = h / 3f;
        glyphPaint.setTextSize(cellPx * 0.46f);
        labelPaint.setTextSize(cellPx * 0.17f);
        // Запускаем отложенный спин, если startSpin() вызвали до layout
        if (pendingSpinMult >= 0f) {
            doStartSpin(pendingSpinMult);
            pendingSpinMult = -1f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cellPx <= 0) return;
        int n   = reelOrder.length;
        float w = getWidth();
        float h = getHeight();
        float r = 14f;

        // Fractional offset within current cell (0..cellPx)
        float frac = scrollPx % cellPx;
        // Index of the symbol that is at the very top of the view (may be partially off-screen)
        int topIdx = (int) Math.floor(scrollPx / cellPx);

        // Draw 4 cells so the view is always fully covered (partial top + 3 full + partial bottom)
        for (int slot = 0; slot <= 3; slot++) {
            float cellTop    = slot * cellPx - frac;
            float cellBottom = cellTop + cellPx;
            if (cellBottom < 0 || cellTop > h) continue;

            int symIdx = ((topIdx + slot) % n + n) % n;
            int sym    = reelOrder[symIdx];

            // Dark background
            cellRect.set(5f, cellTop + 3f, w - 5f, cellBottom - 3f);
            bgPaint.setColor(0xFF0D0D20);
            canvas.drawRoundRect(cellRect, r, r, bgPaint);

            // Colored tint overlay
            bgPaint.setColor((SYM_COLORS[sym] & 0x00FFFFFF) | 0x1A000000);
            canvas.drawRoundRect(cellRect, r, r, bgPaint);

            // Border
            canvas.drawRoundRect(cellRect, r, r, borderPaint);

            // Glyph (big)
            glyphPaint.setColor(SYM_COLORS[sym]);
            canvas.drawText(SYM_GLYPH[sym], w / 2f, cellTop + cellPx * 0.60f, glyphPaint);

            // Short label below
            canvas.drawText(SYM_LABEL[sym], w / 2f, cellTop + cellPx * 0.85f, labelPaint);
        }

        // Gold highlight around middle row when result is shown
        if (highlightMiddle) {
            float mt = cellPx + 2f;
            float mb = cellPx * 2f - 2f;
            cellRect.set(2f, mt, w - 2f, mb);
            canvas.drawRoundRect(cellRect, r, r, hlPaint);
        }
    }

    // ─── Public API ─────────────────────────────────────────────────────────

    /** Задаёт порядок символов на барабане (для визуального разнообразия между тремя рядами). */
    public void setReelOrder(int[] order) {
        reelOrder = order;
        invalidate();
    }

    /** Включает/выключает золотую подсветку средней ячейки (выигравшая линия). */
    public void setHighlightMiddle(boolean on) {
        highlightMiddle = on;
        invalidate();
    }

    /** Запускает непрерывное вращение с нормальной скоростью. */
    public void startSpin() {
        startSpin(1.0f);
    }

    /**
     * Запускает вращение с множителем скорости (>1 = быстрее).
     * Если view ещё не измерен (cellPx==0), откладывает старт до onSizeChanged.
     */
    public void startSpin(float speedMult) {
        cancelAnim();
        if (cellPx <= 0f) {
            pendingSpinMult = speedMult;
            return;
        }
        pendingSpinMult = -1f;
        doStartSpin(speedMult);
    }

    private void doStartSpin(float speedMult) {
        currentSpeedMult = speedMult;
        float loopPx = reelOrder.length * cellPx;
        ValueAnimator anim = ValueAnimator.ofFloat(scrollPx, scrollPx + loopPx * 1000f);
        anim.setDuration((long)(reelOrder.length * 120L * 1000 / speedMult));
        anim.setInterpolator(new LinearInterpolator());
        anim.addUpdateListener(a -> {
            scrollPx = (float) a.getAnimatedValue();
            invalidate();
        });
        activeAnimator = anim;
        anim.start();
    }

    /**
     * Останавливает барабан так, чтобы символ {@code targetSym} оказался в средней ячейке.
     * Анимация плавно продолжает текущую скорость спина, затем замедляется до нуля.
     * После окончания анимации вызывается {@code onStopped}.
     */
    public void stopAt(int targetSym, Runnable onStopped) {
        cancelAnim();
        if (cellPx <= 0) { if (onStopped != null) onStopped.run(); return; }

        int n       = reelOrder.length;
        float total = n * cellPx;

        // Find the position of targetSym in reelOrder
        int targetIdx = 0;
        for (int i = 0; i < n; i++) {
            if (reelOrder[i] == targetSym) { targetIdx = i; break; }
        }

        // For the middle row (slot=1) to show targetSym:
        //   topIdx + 1 ≡ targetIdx  (mod n)
        //   → topIdx ≡ targetIdx - 1  (mod n)
        float currentMod = scrollPx % total;
        float wantedMod  = ((targetIdx - 1 + n) % n) * cellPx;
        float delta      = wantedMod - currentMod;
        if (delta <= 0f) delta += total;
        delta += total; // 1 дополнительный полный оборот для визуального эффекта

        // Текущая скорость спина: cellPx * speedMult / 120 px/ms
        // DecelerateInterpolator(1f): f'(0) = 2 → начальная скорость = 2*delta/duration
        // Приравниваем к скорости спина → duration = 2*delta / spinVelocity
        float spinVelocity = cellPx * currentSpeedMult / 120f; // px/ms
        long  duration     = (long)(2f * delta / spinVelocity);
        duration = Math.max(900L, Math.min(2500L, duration));

        // Capture для лямбды (effectively final)
        final int   tIdx   = targetIdx;
        final float total_ = total;

        ValueAnimator anim = ValueAnimator.ofFloat(scrollPx, scrollPx + delta);
        anim.setDuration(duration);
        anim.setInterpolator(new DecelerateInterpolator(1f));
        anim.addUpdateListener(a -> {
            scrollPx = (float) a.getAnimatedValue();
            invalidate();
        });
        anim.addListener(new AnimatorListenerAdapter() {
            boolean wasCancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (wasCancelled) return;
                // Снапим scrollPx точно на границу ячейки — убирает погрешность float
                // и исключает мигание на соседний символ при завершении анимации
                int nn    = reelOrder.length;
                float exact = ((tIdx - 1 + nn) % nn) * cellPx;
                scrollPx = (float) Math.floor(scrollPx / total_) * total_ + exact;
                invalidate();
                if (onStopped != null) onStopped.run();
            }
        });
        activeAnimator = anim;
        anim.start();
    }

    /** Возвращает символ, находящийся в средней (видимой, выигрышной) ячейке прямо сейчас. */
    public int getMiddleSymbol() {
        if (cellPx <= 0 || reelOrder.length == 0) return 0;
        int n      = reelOrder.length;
        int topIdx = (int) Math.floor(scrollPx / cellPx);
        return reelOrder[((topIdx + 1) % n + n) % n];
    }

    /** Немедленно останавливает любую текущую анимацию и отменяет отложенный старт. */
    public void cancelAnim() {
        pendingSpinMult = -1f;
        if (activeAnimator != null) {
            activeAnimator.cancel();
            activeAnimator = null;
        }
    }
}
