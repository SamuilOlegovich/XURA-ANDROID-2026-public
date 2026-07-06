package com.samuilolegovich.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Прозрачный overlay поверх сетки 3×3 барабанов.
 * При победе анимирует выигравшие paylines: трассирует цветную линию через ячейки
 * и показывает подпись с символом и множителем.
 */
public class SlotPaylineView extends View {

    // 5 линий выплат: [точка 0..2][row, col]
    private static final int[][][] PAYLINES = {
        { {1,0}, {1,1}, {1,2} }, // средняя горизонталь
        { {0,0}, {0,1}, {0,2} }, // верхняя горизонталь
        { {2,0}, {2,1}, {2,2} }, // нижняя горизонталь
        { {0,0}, {1,1}, {2,2} }, // диагональ ↘
        { {2,0}, {1,1}, {0,2} }, // диагональ ↗
    };

    // Смещение подписи от середины линии: доли ширины/высоты ячейки
    private static final float[] LABEL_OX = {  0f,     0f,     0f,    -0.40f,  0.40f };
    private static final float[] LABEL_OY = { -0.38f,  0.32f, -0.32f, -0.32f, -0.32f };

    private static final int[] LINE_COLORS = {
        0xFFFFB000, // средняя     — золото
        0xFF00D4FF, // верхняя     — голубой
        0xFF40FF90, // нижняя      — салатовый
        0xFFFF6A00, // диаг ↘      — оранжевый
        0xFFFF40C0, // диаг ↗      — малиновый
    };

    private static final int[]    MULTIPLIERS = { 2, 5, 10, 20, 50, 250, 0 };
    private static final String[] SYM_LABEL   = { "XRP","RKT","MOON","DIA","WHAL","JKPT","WILD" };

    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bgRect    = new RectF();

    private final List<ActiveLine>    active = new ArrayList<>();
    private final List<ValueAnimator> anims  = new ArrayList<>();

    public SlotPaylineView(Context ctx)                     { super(ctx); init(); }
    public SlotPaylineView(Context ctx, AttributeSet attrs) { super(ctx, attrs); init(); }

    private void init() {
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        dotPaint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setFakeBoldText(true);

        bgPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        float ch = h / 3f;
        textPaint.setTextSize(ch * 0.22f);
        linePaint.setStrokeWidth(ch * 0.055f);
        glowPaint.setStrokeWidth(ch * 0.24f);
    }

    /** Запускает анимацию выигравших линий. Вызывается из SlotFlasher.showResult() при победе. */
    public void showWinLines(int[][] matrix) {
        reset();

        for (int li = 0; li < PAYLINES.length; li++) {
            int[][] pl = PAYLINES[li];
            int[] syms = {
                matrix[pl[0][0]][pl[0][1]],
                matrix[pl[1][0]][pl[1][1]],
                matrix[pl[2][0]][pl[2][1]]
            };
            int sym = resolveWin(syms);
            if (sym < 0) continue;

            ActiveLine al = new ActiveLine();
            al.lineIdx = li;
            al.symbol  = sym;
            al.color   = LINE_COLORS[li];
            active.add(al);
        }

        for (int i = 0; i < active.size(); i++) {
            final ActiveLine al = active.get(i);
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(580);
            anim.setStartDelay((long) i * 640);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addUpdateListener(a -> {
                al.progress  = (float) a.getAnimatedValue();
                al.textAlpha = Math.max(0f, (al.progress - 0.72f) / 0.28f);
                invalidate();
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    al.progress = 1f; al.textAlpha = 1f; invalidate();
                }
            });
            anims.add(anim);
            anim.start();
        }
    }

    // Wild (6) замещает любой символ; все Wild — не победа
    private static int resolveWin(int[] syms) {
        int found = -1;
        for (int s : syms) {
            if (s == 6) continue;
            if (found < 0) found = s;
            else if (found != s) return -1;
        }
        return found;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;
        float w  = getWidth();
        float h  = getHeight();
        float cw = w / 3f;
        float ch = h / 3f;

        for (ActiveLine al : active) {
            if (al.progress <= 0f) continue;
            int[][] pl = PAYLINES[al.lineIdx];

            float[] xs = new float[3];
            float[] ys = new float[3];
            for (int p = 0; p < 3; p++) {
                xs[p] = (pl[p][1] + 0.5f) * cw;
                ys[p] = (pl[p][0] + 0.5f) * ch;
            }

            // Полный путь линии (два отрезка через три точки)
            Path full = new Path();
            full.moveTo(xs[0], ys[0]);
            full.lineTo(xs[1], ys[1]);
            full.lineTo(xs[2], ys[2]);

            PathMeasure pm       = new PathMeasure(full, false);
            float       totalLen = pm.getLength();
            float       d1       = (float) Math.hypot(xs[1] - xs[0], ys[1] - ys[0]);

            // Обрезаем путь по текущему прогрессу
            Path drawn = new Path();
            pm.getSegment(0, totalLen * al.progress, drawn, true);

            // Свечение — широкое, полупрозрачное
            glowPaint.setColor((al.color & 0x00FFFFFF) | 0x30000000);
            canvas.drawPath(drawn, glowPaint);

            // Линия — тонкая, яркая
            linePaint.setColor(al.color);
            canvas.drawPath(drawn, linePaint);

            // Кружки-маркеры на каждой из трёх точек (появляются по мере продвижения линии)
            float dotR = ch * 0.068f;
            dotPaint.setColor(al.color);
            canvas.drawCircle(xs[0], ys[0], dotR, dotPaint);
            if (totalLen * al.progress >= d1)
                canvas.drawCircle(xs[1], ys[1], dotR, dotPaint);
            if (al.progress >= 0.97f)
                canvas.drawCircle(xs[2], ys[2], dotR, dotPaint);

            // Подпись — появляется в конце трассировки
            if (al.textAlpha > 0f) {
                int    alpha = (int)(al.textAlpha * 255);
                String label = SYM_LABEL[al.symbol] + " ×" + MULTIPLIERS[al.symbol];

                float lx = (xs[0] + xs[2]) / 2f + LABEL_OX[al.lineIdx] * cw;
                float ly = (ys[0] + ys[2]) / 2f + LABEL_OY[al.lineIdx] * ch;

                float tw  = textPaint.measureText(label);
                float ts  = textPaint.getTextSize();
                float pad = ts * 0.32f;
                bgRect.set(lx - tw/2f - pad, ly - ts - pad * 0.5f, lx + tw/2f + pad, ly + pad * 1.2f);

                // Тёмная подложка-пилюля
                bgPaint.setColor(0xFF000000);
                bgPaint.setAlpha(Math.min(alpha, 210));
                canvas.drawRoundRect(bgRect, ts * 0.45f, ts * 0.45f, bgPaint);

                // Текст в цвете линии
                textPaint.setColor((al.color & 0x00FFFFFF) | (alpha << 24));
                canvas.drawText(label, lx, ly, textPaint);
            }
        }
    }

    /** Останавливает все анимации и очищает overlay. */
    public void reset() {
        for (ValueAnimator a : anims) a.cancel();
        anims.clear();
        active.clear();
        invalidate();
    }

    private static final class ActiveLine {
        int   lineIdx;
        int   symbol;
        int   color;
        float progress  = 0f;
        float textAlpha = 0f;
    }
}
