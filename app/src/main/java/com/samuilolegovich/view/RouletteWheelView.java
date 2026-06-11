package com.samuilolegovich.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RouletteWheelView extends View {

    // Standard European roulette wheel order (clockwise)
    private static final int[] WHEEL_NUMBERS = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    private static final Set<Integer> BLACK_NUMBERS = new HashSet<>(Arrays.asList(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    ));

    private static final float SECTOR_ANGLE = 360f / WHEEL_NUMBERS.length;

    private static final int C_RED     = Color.parseColor("#C81030");
    private static final int C_BLACK   = Color.parseColor("#111111");
    private static final int C_GREEN   = Color.parseColor("#007040");
    private static final int C_GOLD    = Color.parseColor("#FFB000");
    private static final int C_DIVIDER = Color.parseColor("#2E2E2E");
    private static final int C_NUMBER  = Color.parseColor("#FFB000"); // gold numbers

    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval              = new RectF();
    private final Path  markerPath        = new Path();

    private float          mRotation     = 0f;
    private ValueAnimator  activeAnimator;

    public RouletteWheelView(Context c)                              { super(c);       init(); }
    public RouletteWheelView(Context c, AttributeSet a)              { super(c, a);    init(); }
    public RouletteWheelView(Context c, AttributeSet a, int defStyle){ super(c, a, defStyle); init(); }

    private void init() {
        dividePaint.setStyle(Paint.Style.STROKE);
        dividePaint.setColor(C_DIVIDER);

        textPaint.setColor(C_NUMBER);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setColor(C_GOLD);

        centerPaint.setColor(Color.BLACK); // updated via setCenterColor()

        markerPaint.setColor(C_GOLD);

        markerStrokePaint.setStyle(Paint.Style.STROKE);
        markerStrokePaint.setColor(C_GREEN);
        markerStrokePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cx   = getWidth()  / 2f;
        float cy   = getHeight() / 2f;
        float maxR = Math.min(cx, cy) - 2f;
        if (maxR <= 0) return;

        float outerR        = maxR * 0.93f;
        float numBandInnerR = maxR * 0.70f; // inner boundary of the number band
        float innerR        = maxR * 0.47f; // 0.93−0.70=0.23 → 0.70−0.23=0.47 (equal gaps)
        float textR        = maxR * 0.82f;  // centred in the number band (70%–93%)

        textPaint.setTextSize(maxR * 0.070f);
        rimPaint.setStrokeWidth(maxR * 0.014f);
        dividePaint.setStrokeWidth(maxR * 0.004f);

        // ── Rotating part ─────────────────────────────────────────────
        canvas.save();
        canvas.rotate(mRotation, cx, cy);

        oval.set(cx - outerR, cy - outerR, cx + outerR, cy + outerR);
        for (int i = 0; i < WHEEL_NUMBERS.length; i++) {
            float startAngle = -90f + i * SECTOR_ANGLE;
            int   n          = WHEEL_NUMBERS[i];

            fillPaint.setColor(n == 0 ? C_GREEN : BLACK_NUMBERS.contains(n) ? C_BLACK : C_RED);
            canvas.drawArc(oval, startAngle, SECTOR_ANGLE, true, fillPaint);
            canvas.drawArc(oval, startAngle, SECTOR_ANGLE, true, dividePaint);
        }

        // Inner number-band ring — separates numbers from the centre disc
        rimPaint.setStrokeWidth(maxR * 0.020f);
        canvas.drawCircle(cx, cy, numBandInnerR, rimPaint);

        // Numbers drawn on top of sectors and band ring
        for (int i = 0; i < WHEEL_NUMBERS.length; i++) {
            float startAngle = -90f + i * SECTOR_ANGLE;
            int   n          = WHEEL_NUMBERS[i];
            // +90° corrects canvas-coordinate offset so text lands on its own sector
            float midAngle = startAngle + SECTOR_ANGLE / 2f;
            canvas.save();
            canvas.rotate(midAngle + 90f, cx, cy);
            float textY = cy - textR - (textPaint.ascent() + textPaint.descent()) / 2f;
            canvas.drawText(String.valueOf(n), cx, textY, textPaint);
            canvas.restore();
        }

        // Inner dark circle (covers wedge points)
        canvas.drawCircle(cx, cy, innerR, centerPaint);

        // Inner gold ring (border of centre disc)
        rimPaint.setStrokeWidth(maxR * 0.018f);
        canvas.drawCircle(cx, cy, innerR, rimPaint);

        canvas.restore();

        // ── Fixed part (not rotated) ───────────────────────────────────
        // Outer gold rim
        rimPaint.setStrokeWidth(maxR * 0.028f);
        canvas.drawCircle(cx, cy, outerR + maxR * 0.006f, rimPaint);

        // Ball marker: gold arrow at 12 o'clock, base outside rim, tip in number band
        float mBase = cy - outerR - maxR * 0.010f; // flat base sits just outside outer rim
        float mHW   = maxR * 0.050f;               // half-width of base
        float mH    = maxR * 0.100f;               // length (tip reaches into number band)
        markerPath.reset();
        markerPath.moveTo(cx,        mBase + mH);  // sharp tip (inward)
        markerPath.lineTo(cx - mHW,  mBase);       // base left
        markerPath.lineTo(cx + mHW,  mBase);       // base right
        markerPath.close();
        canvas.drawPath(markerPath, markerPaint);  // gold fill
        markerStrokePaint.setStrokeWidth(maxR * 0.018f);
        canvas.drawPath(markerPath, markerStrokePaint); // white outline
    }

    // ── Public API ────────────────────────────────────────────────────

    public void startSpinning() {
        cancelActive();
        // 2000 rotations ≈ 20 minutes — cancelled long before end
        activeAnimator = ValueAnimator.ofFloat(mRotation, mRotation + 720_000f);
        activeAnimator.setDuration(1_200_000L);
        activeAnimator.setInterpolator(new LinearInterpolator());
        activeAnimator.addUpdateListener(a -> {
            mRotation = (float) a.getAnimatedValue();
            invalidate();
        });
        activeAnimator.start();
    }

    public void stopAtNumber(int number, Runnable onStopped) {
        cancelActive();

        float curAngle   = mRotation % 360f;
        float sectorMid  = -90f + findIndex(number) * SECTOR_ANGLE + SECTOR_ANGLE / 2f;
        // marker is at -90° → target mRotation = -90° - sectorMid
        float targetNorm = ((-sectorMid - 90f) % 360f + 360f) % 360f;
        float delta = (targetNorm - curAngle + 360f) % 360f;
        if (delta < 90f) delta += 360f;
        delta += 720f; // two extra full rotations for drama

        float target = mRotation + delta;
        activeAnimator = ValueAnimator.ofFloat(mRotation, target);
        activeAnimator.setDuration(4000);
        activeAnimator.setInterpolator(new DecelerateInterpolator(3f));
        activeAnimator.addUpdateListener(a -> {
            mRotation = (float) a.getAnimatedValue();
            invalidate();
        });
        activeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (onStopped != null) post(onStopped);
            }
        });
        activeAnimator.start();
    }

    public void stopSpinning() {
        cancelActive();
    }

    public void setCenterColor(int color) {
        centerPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelActive();
    }

    private void cancelActive() {
        if (activeAnimator != null && activeAnimator.isRunning()) {
            activeAnimator.cancel();
        }
    }

    private int findIndex(int number) {
        for (int i = 0; i < WHEEL_NUMBERS.length; i++) {
            if (WHEEL_NUMBERS[i] == number) return i;
        }
        return 0;
    }
}