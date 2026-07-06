package com.samuilolegovich.view;

/**
 * Стрипы барабанов слот-машины.
 *
 * Длина: 84 позиции (сумма весов символов).
 * Распределение: XRP×30, Rocket×20, Moon×15, Diamond×10, Whale×5, Jackpot×1, Wild×3.
 *
 * Три барабана используют одну базовую ленту, сдвинутую на 28 и 56 позиций —
 * одинаковая вероятность символов, разный визуальный порядок.
 *
 * Сервер обязан использовать идентичный стрип: бросает random(0,83) для каждого
 * барабана и присылает три стоп-позиции в Memo транзакции.
 */
public final class SlotReelStrip {

    public static final int LENGTH = 84;

    // XRP(0)×30, Rocket(1)×20, Moon(2)×15, Diamond(3)×10, Whale(4)×5, Jackpot(5)×1, Wild(6)×3
    private static final int[] BASE = {
        0, 1, 0, 2, 0, 1, 0, 3, 0, 1,
        2, 0, 1, 0, 4, 2, 0, 1, 3, 0,
        1, 2, 0, 6, 1, 0, 3, 2, 0, 1,
        0, 2, 3, 0, 1, 4, 2, 0, 1, 3,
        0, 2, 1, 0, 5, 2, 0, 3, 1, 0,
        6, 2, 0, 1, 3, 0, 2, 4, 1, 0,
        3, 2, 0, 1, 0, 2, 6, 3, 0, 1,
        2, 0, 4, 1, 0, 3, 2, 1, 0, 4,
        1, 0, 1, 0
    };

    public static final int[] LEFT   = BASE;
    public static final int[] CENTER = rotate(BASE, 28);
    public static final int[] RIGHT  = rotate(BASE, 56);

    private static int[] rotate(int[] src, int by) {
        int[] r = new int[src.length];
        for (int i = 0; i < src.length; i++) r[i] = src[(i + by) % src.length];
        return r;
    }

    /**
     * Строит матрицу 3×3 из трёх стоп-позиций барабанов.
     * Row 0 = верхняя строка, Row 1 = средняя (главная линия), Row 2 = нижняя.
     * Col 0 = левый барабан, Col 1 = центральный, Col 2 = правый.
     */
    public static int[][] buildMatrix(int stopLeft, int stopCenter, int stopRight) {
        int[][] m = new int[3][3];
        for (int row = 0; row < 3; row++) {
            m[row][0] = LEFT  [(stopLeft   + row - 1 + LENGTH) % LENGTH];
            m[row][1] = CENTER[(stopCenter + row - 1 + LENGTH) % LENGTH];
            m[row][2] = RIGHT [(stopRight  + row - 1 + LENGTH) % LENGTH];
        }
        return m;
    }

    private SlotReelStrip() {}
}
