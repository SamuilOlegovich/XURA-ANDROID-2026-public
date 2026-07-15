package com.samuilolegovich.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Компактный мини-вид стола европейской рулетки для TxDetailSheet.
 *
 * Два режима:
 *  - setWinningNumber() — подсвечивает выигравшее число (для RLT:{num}:WIN|LOSE)
 *  - setBets()          — подсвечивает позиции ставок на столе (для RLT:{bets}:{ref})
 *
 * Раскладка: нулевой столбец (0, зелёный) + 12 колонок по 3 числа = 37 ячеек.
 * Ниже: строка дюжин (D1/D2/D3) + строка внешних ставок (1-18/ODD/RED/BLK/EVEN/19-36).
 */
public class RouletteMiniView extends View {

    // Чёрные числа европейской рулетки
    private static final Set<Integer> BLACK = new HashSet<>(Arrays.asList(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35));

    // ── Публичный DTO ────────────────────────────────────────────────────
    public static class BetEntry {
        public final String     code;
        public final BigDecimal amount;
        public BetEntry(String code, BigDecimal amount) { this.code = code; this.amount = amount; }
    }

    // ── Состояние ───────────────────────────────────────────────────────
    private int          winningNumber = -1;
    private boolean      isWin         = false;
    private final boolean[]  cellHL    = new boolean[37]; // подсвеченные ячейки
    private final Set<String> betCodes = new HashSet<>(); // активные коды внешних ставок

    // ── Paints ──────────────────────────────────────────────────────────
    private final Paint pRed    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBlack  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGreen  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pHl     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGlow   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pDim    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pOut    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pOutHl  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pOutTxt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect    = new RectF();
    private final RectF glowRect= new RectF();

    private static final int COLS = 13; // col 0 = zero, col 1-12 = numbers

    public RouletteMiniView(Context ctx)                     { super(ctx); init(); }
    public RouletteMiniView(Context ctx, AttributeSet attrs) { super(ctx, attrs); init(); }

    private void init() {
        pRed.setStyle(Paint.Style.FILL);
        pBlack.setStyle(Paint.Style.FILL);
        pGreen.setStyle(Paint.Style.FILL);
        pHl.setStyle(Paint.Style.FILL);
        pHl.setColor(0x55FFB000);
        pBorder.setStyle(Paint.Style.STROKE);
        pBorder.setColor(0x28FFFFFF);
        pGlow.setStyle(Paint.Style.STROKE);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTypeface(Typeface.DEFAULT_BOLD);
        pText.setFakeBoldText(true);
        pText.setColor(0xFFFFFFFF);
        pDim.setTextAlign(Paint.Align.CENTER);
        pDim.setColor(0x88FFFFFF);
        pOut.setStyle(Paint.Style.FILL);
        pOutHl.setStyle(Paint.Style.FILL);
        pOutHl.setColor(0x44FFB000);
        pOutTxt.setTextAlign(Paint.Align.CENTER);
        pOutTxt.setTypeface(Typeface.DEFAULT_BOLD);
    }

    // ── API ─────────────────────────────────────────────────────────────

    /** Режим результата: подсвечивает выигравшее число. */
    public void setWinningNumber(int num, boolean win) {
        winningNumber = num;
        isWin = win;
        betCodes.clear();
        for (int i = 0; i < cellHL.length; i++) cellHL[i] = false;
        invalidate();
    }

    /** Режим ставки: подсвечивает ячейки согласно кодам ставок из мемо. */
    public void setBets(List<BetEntry> bets) {
        winningNumber = -1;
        betCodes.clear();
        for (int i = 0; i < cellHL.length; i++) cellHL[i] = false;
        if (bets == null) return;
        for (BetEntry be : bets) {
            betCodes.add(be.code);
            applyHighlight(be.code);
        }
        invalidate();
    }

    private void applyHighlight(String code) {
        if (code == null) return;
        if (code.startsWith("n")) {
            try {
                int n = Integer.parseInt(code.substring(1));
                if (n >= 0 && n <= 36) cellHL[n] = true;
            } catch (NumberFormatException ignored) {}
            return;
        }
        switch (code) {
            case "r":  for (int n = 1; n <= 36; n++) { if (!BLACK.contains(n)) cellHL[n] = true; } break;
            case "b":  for (int n : BLACK) cellHL[n] = true; break;
            case "o":  for (int n = 1; n <= 35; n += 2) cellHL[n] = true; break;
            case "e":  for (int n = 2; n <= 36; n += 2) cellHL[n] = true; break;
            case "l":  for (int n = 1; n <= 18; n++) cellHL[n] = true; break;
            case "h":  for (int n = 19; n <= 36; n++) cellHL[n] = true; break;
            case "d1": for (int n = 1;  n <= 12; n++) cellHL[n] = true; break;
            case "d2": for (int n = 13; n <= 24; n++) cellHL[n] = true; break;
            case "d3": for (int n = 25; n <= 36; n++) cellHL[n] = true; break;
            case "c1": for (int n = 1; n <= 36; n++) { if (n % 3 == 1) cellHL[n] = true; } break;
            case "c2": for (int n = 1; n <= 36; n++) { if (n % 3 == 2) cellHL[n] = true; } break;
            case "c3": for (int n = 1; n <= 36; n++) { if (n % 3 == 0) cellHL[n] = true; } break;
        }
    }

    // ── Measure ─────────────────────────────────────────────────────────

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        // 3 строки чисел + 1 строка дюжин + 1 строка внешних ставок = 5 строк
        float cw = (float) w / COLS;
        float ch = cw * 0.82f;
        setMeasuredDimension(w, (int) (ch * 5 + dp(2)));
    }

    // ── Draw ────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        float gap  = 1f;
        float cw   = (w - gap * (COLS + 1)) / (float) COLS;
        float ch   = cw * 0.82f;
        float cr   = dp(2); // corner radius

        pText.setTextSize(ch * 0.44f);
        pDim.setTextSize(ch * 0.44f);
        pBorder.setStrokeWidth(gap);
        pGlow.setStrokeWidth(ch * 0.10f);
        pOutTxt.setTextSize(ch * 0.34f);

        // ── 0: первый столбец, занимает все 3 строки ───────────────────
        float zL = gap, zT = gap, zR = zL + cw, zB = gap + 3 * ch + 2 * gap;
        rect.set(zL, zT, zR, zB);
        boolean zWin = winningNumber == 0;
        pGreen.setColor(zWin ? 0xFF2E7D32 : cellHL[0] ? 0xFF1B5E24 : 0xFF0A2E0F);
        canvas.drawRoundRect(rect, cr, cr, pGreen);
        if (zWin) drawGlow(canvas, rect, 0xCC00FF44, cr);
        canvas.drawRoundRect(rect, cr, cr, pBorder);
        float zCy = (zT + zB) / 2f + ch * 0.18f;
        (zWin ? pText : pDim).setTextSize(ch * 0.54f);
        canvas.drawText("0", (zL + zR) / 2f, zCy, zWin ? pText : pDim);
        pText.setTextSize(ch * 0.44f);
        pDim.setTextSize(ch * 0.44f);

        // ── числа 1–36 ─────────────────────────────────────────────────
        for (int n = 1; n <= 36; n++) {
            int col = (n - 1) / 3 + 1;
            int row = 2 - (n - 1) % 3;
            float l = gap + col * (cw + gap);
            float t = gap + row * (ch + gap);
            rect.set(l, t, l + cw, t + ch);

            boolean isBlack = BLACK.contains(n);
            boolean winning = n == winningNumber;
            boolean hl      = cellHL[n];

            int bgColor = winning
                    ? (isBlack ? 0xFF424242 : 0xFFB71C1C)
                    : hl
                        ? (isBlack ? 0xFF2A2A4A : 0xFF6B1010)
                        : (isBlack ? 0xFF141420 : 0xFF3A0808);
            pBlack.setColor(bgColor);
            canvas.drawRoundRect(rect, cr, cr, pBlack);
            if (hl && !winning) canvas.drawRoundRect(rect, cr, cr, pHl);
            if (winning) drawGlow(canvas, rect, isBlack ? 0xAAEEEEEE : 0xAAFF5555, cr);
            canvas.drawRoundRect(rect, cr, cr, pBorder);

            float cx = rect.centerX(), cy = rect.centerY() + ch * 0.18f;
            Paint tp = (winning || hl) ? pText : pDim;
            canvas.drawText(String.valueOf(n), cx, cy, tp);
        }

        // ── строка дюжин ───────────────────────────────────────────────
        float dozY  = gap + 3 * (ch + gap);
        float dozH  = ch * 0.80f;
        float dozX0 = gap + cw + gap; // start after zero column
        float dozW  = (12 * (cw + gap) - gap) / 3f; // each dozen spans 4 columns
        String[] dLabels = {"1-12", "13-24", "25-36"};
        String[] dCodes  = {"d1", "d2", "d3"};
        for (int i = 0; i < 3; i++) {
            float dx = dozX0 + i * (dozW + gap);
            boolean dHl = betCodes.contains(dCodes[i]);
            drawOutside(canvas, dx, dozY, dozW, dozH, dLabels[i], dHl, cr);
        }

        // ── строка внешних ставок ──────────────────────────────────────
        float extY  = dozY + dozH + gap;
        float extH  = ch * 0.80f;
        float extW  = (12 * (cw + gap) - gap) / 6f;
        String[] eLabels = {"1-18", "ODD", "RED", "BLK", "EVEN", "19-36"};
        String[] eCodes  = {"l", "o", "r", "b", "e", "h"};
        for (int i = 0; i < 6; i++) {
            float ex = dozX0 + i * (extW + gap);
            boolean eHl = betCodes.contains(eCodes[i]);
            drawOutside(canvas, ex, extY, extW, extH, eLabels[i], eHl, cr);
        }
    }

    private void drawOutside(Canvas canvas, float l, float t, float w, float h,
                              String label, boolean hl, float cr) {
        rect.set(l, t, l + w, t + h);
        pOut.setColor(hl ? 0xFF151D35 : 0xFF0D0D20);
        canvas.drawRoundRect(rect, cr, cr, pOut);
        if (hl) canvas.drawRoundRect(rect, cr, cr, pOutHl);
        canvas.drawRoundRect(rect, cr, cr, pBorder);
        pOutTxt.setColor(hl ? 0xFFFFFFFF : 0x66FFFFFF);
        canvas.drawText(label, rect.centerX(), rect.centerY() + h * 0.22f, pOutTxt);
    }

    private void drawGlow(Canvas canvas, RectF r, int color, float cr) {
        float d = dp(2);
        glowRect.set(r.left - d, r.top - d, r.right + d, r.bottom + d);
        pGlow.setColor(color);
        canvas.drawRoundRect(glowRect, cr + d, cr + d, pGlow);
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
