package com.samuilolegovich.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Компактный статический canvas-вид: сетка 3×3 символов слот-машины
 * с наложением выигравших paylines. Используется в TxDetailSheet.
 */
public class SlotMiniView extends View {

    // Точная копия PAYLINES из SlotPaylineView
    static final int[][][] PAYLINES = {
        { {1,0}, {1,1}, {1,2} }, // средняя горизонталь
        { {0,0}, {0,1}, {0,2} }, // верхняя горизонталь
        { {2,0}, {2,1}, {2,2} }, // нижняя горизонталь
        { {0,0}, {1,1}, {2,2} }, // диагональ ↘
        { {2,0}, {1,1}, {0,2} }, // диагональ ↗
    };

    static final int[] LINE_COLORS = {
        0xFFFFB000, // средняя     — золото
        0xFF00D4FF, // верхняя     — голубой
        0xFF40FF90, // нижняя      — салатовый
        0xFFFF6A00, // диагональ ↘ — оранжевый
        0xFFFF40C0, // диагональ ↗ — малиновый
    };

    static final String[] LINE_NAMES = {
        "Middle line", "Top line", "Bottom line", "Diagonal ↘", "Diagonal ↗"
    };

    // Множители — совпадают с SlotPaylineView
    static final int[] MULTIPLIERS = { 2, 5, 10, 20, 50, 100, 0 };

    // ── Данные ──────────────────────────────────────────────────────────────

    private int[][] matrix  = null;
    private boolean isWin   = false;

    private final boolean[] winActive = new boolean[5];
    private final int[]     winSym    = new int[5];

    // ── Paints ──────────────────────────────────────────────────────────────

    private final Paint bgPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hlBgPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glyphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF cellRect = new RectF();

    public SlotMiniView(Context ctx)                     { super(ctx); init(); }
    public SlotMiniView(Context ctx, AttributeSet attrs) { super(ctx, attrs); init(); }

    private void init() {
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(0xFF13131F);

        hlBgPaint.setStyle(Paint.Style.FILL);
        hlBgPaint.setColor(0xFF1E1E3A);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);

        glyphPaint.setTextAlign(Paint.Align.CENTER);
        glyphPaint.setTypeface(Typeface.DEFAULT_BOLD);
        glyphPaint.setFakeBoldText(true);

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTypeface(Typeface.DEFAULT);
        labelPaint.setColor(0x80FFFFFF);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setStrokeJoin(Paint.Join.ROUND);

        dotPaint.setStyle(Paint.Style.FILL);
    }

    // ── API ─────────────────────────────────────────────────────────────────

    public void setData(int[][] matrix, boolean isWin) {
        this.matrix = matrix;
        this.isWin  = isWin;
        if (matrix != null) computeWinLines();
        invalidate();
    }

    /** Индексы активных выигрышных линий — для построения легенды в TxDetailSheet. */
    public int[] getWinLineIndices() {
        int count = 0;
        for (boolean b : winActive) if (b) count++;
        int[] arr = new int[count];
        int i = 0;
        for (int li = 0; li < 5; li++) if (winActive[li]) arr[i++] = li;
        return arr;
    }

    public int getWinSymbol(int lineIdx) { return winSym[lineIdx]; }

    // ── Measure ─────────────────────────────────────────────────────────────

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        setMeasuredDimension(w, w); // всегда квадрат
    }

    // ── Win logic ───────────────────────────────────────────────────────────

    private void computeWinLines() {
        for (int li = 0; li < PAYLINES.length; li++) {
            int[][] pl = PAYLINES[li];
            int[] syms = {
                matrix[pl[0][0]][pl[0][1]],
                matrix[pl[1][0]][pl[1][1]],
                matrix[pl[2][0]][pl[2][1]]
            };
            int sym = resolveWin(syms);
            winActive[li] = sym >= 0;
            winSym[li]    = Math.max(sym, 0);
        }
    }

    // Wild(6) замещает любой символ; три Wild — не победа
    private static int resolveWin(int[] syms) {
        int found = -1;
        for (int s : syms) {
            if (s == 6) continue;
            if (found < 0) found = s;
            else if (found != s) return -1;
        }
        return found;
    }

    // ── Draw ────────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        if (matrix == null) return;

        float gap      = dp(3);
        float cellSize = (getWidth() - gap * 4) / 3f;

        // Какие ячейки на выигрышной линии
        boolean[][] onWinLine = new boolean[3][3];
        if (isWin) {
            for (int li = 0; li < 5; li++) {
                if (!winActive[li]) continue;
                for (int[] pt : PAYLINES[li]) onWinLine[pt[0]][pt[1]] = true;
            }
        }

        // 1. Ячейки
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawCell(canvas, row, col, cellSize, gap, onWinLine[row][col]);
            }
        }

        // 2. Paylines поверх
        if (isWin) {
            for (int li = 0; li < 5; li++) {
                if (winActive[li]) drawPayline(canvas, li, cellSize, gap);
            }
        }
    }

    private void drawCell(Canvas canvas, int row, int col,
                          float cellSize, float gap, boolean highlighted) {
        float l = gap + col * (cellSize + gap);
        float t = gap + row * (cellSize + gap);
        cellRect.set(l, t, l + cellSize, t + cellSize);
        float corner = dp(6);

        int sym = matrix[row][col];
        int symColor = SlotReelView.SYM_COLORS[sym];

        // Фон
        canvas.drawRoundRect(cellRect, corner, corner,
                highlighted ? hlBgPaint : bgPaint);

        // Рамка
        borderPaint.setColor(highlighted ? (symColor & 0x00FFFFFF | 0x66000000) : 0x22FFFFFF);
        canvas.drawRoundRect(cellRect, corner, corner, borderPaint);

        float cx = l + cellSize / 2f;
        float cy = t + cellSize / 2f;

        // Глиф
        glyphPaint.setTextSize(cellSize * 0.38f);
        glyphPaint.setColor(symColor);
        if (isWin && !highlighted) glyphPaint.setAlpha(90);
        canvas.drawText(SlotReelView.SYM_GLYPH[sym], cx, cy + cellSize * 0.06f, glyphPaint);
        glyphPaint.setAlpha(255);

        // Метка
        labelPaint.setTextSize(cellSize * 0.18f);
        labelPaint.setAlpha(isWin && !highlighted ? 60 : 140);
        canvas.drawText(SlotReelView.SYM_LABEL[sym], cx, cy + cellSize * 0.40f, labelPaint);
        labelPaint.setAlpha(255);
    }

    private void drawPayline(Canvas canvas, int li, float cellSize, float gap) {
        int[][] pl    = PAYLINES[li];
        int     color = LINE_COLORS[li];
        float   lw    = cellSize * 0.055f;

        Path path = new Path();
        float[] cx = new float[3];
        float[] cy = new float[3];
        for (int i = 0; i < 3; i++) {
            cx[i] = gap + pl[i][1] * (cellSize + gap) + cellSize / 2f;
            cy[i] = gap + pl[i][0] * (cellSize + gap) + cellSize / 2f;
            if (i == 0) path.moveTo(cx[i], cy[i]);
            else        path.lineTo(cx[i], cy[i]);
        }

        // Свечение
        glowPaint.setColor((color & 0x00FFFFFF) | 0x28000000);
        glowPaint.setStrokeWidth(lw * 5f);
        canvas.drawPath(path, glowPaint);

        // Линия
        linePaint.setColor(color);
        linePaint.setStrokeWidth(lw);
        canvas.drawPath(path, linePaint);

        // Точки на вершинах
        dotPaint.setColor(color);
        for (int i = 0; i < 3; i++) {
            canvas.drawCircle(cx[i], cy[i], lw * 2f, dotPaint);
        }
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
