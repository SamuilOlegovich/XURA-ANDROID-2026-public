package com.samuilolegovich.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Тесты для класса Lotto — генерация случайных чисел и цветов в игровой логике.
 * Ошибка здесь нарушает честность игры и механику ставок.
 */
public class LottoTest {

    /** Список чёрных чисел рулетки (должен совпадать с логикой в Lotto) */
    private static final List<Integer> BLACK_NUMBERS =
            Arrays.asList(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 33, 31, 35);

    // -------------------------------------------------------------------------
    // genLotto
    // -------------------------------------------------------------------------

    /** Результат genLotto() кратен 3753 и находится в диапазоне 0–75060 */
    @Test
    public void genLotto_returnsMultipleOf3753InRange() {
        for (int i = 0; i < 1000; i++) {
            int result = Lotto.genLotto();
            assertTrue("Значение должно быть >= 0", result >= 0);
            assertTrue("Значение должно быть <= 75060 (20 * 3753)", result <= 20 * 3753);
            assertEquals("Значение должно делиться на 3753 без остатка", 0, result % 3753);
        }
    }

    // -------------------------------------------------------------------------
    // genNumberAndColor
    // -------------------------------------------------------------------------

    /** genNumberAndColor() возвращает карту ровно с одной записью */
    @Test
    public void genNumberAndColor_returnsMapWithOneEntry() {
        for (int i = 0; i < 100; i++) {
            Map<Boolean, String> result = Lotto.genNumberAndColor();
            assertEquals("Карта должна содержать ровно один элемент", 1, result.size());
        }
    }

    /** Число в genNumberAndColor() находится в диапазоне 00–36 */
    @Test
    public void genNumberAndColor_numberIsInRange0to36() {
        for (int i = 0; i < 500; i++) {
            Map<Boolean, String> result = Lotto.genNumberAndColor();
            String numStr = result.values().iterator().next();
            int num = Integer.parseInt(numStr);
            assertTrue("Число >= 0", num >= 0);
            assertTrue("Число <= 36", num <= 36);
        }
    }

    /** true означает чёрное число — должно совпадать со списком BLACK_NUMBERS */
    @Test
    public void genNumberAndColor_colorMatchesBlackList() {
        for (int i = 0; i < 500; i++) {
            Map<Boolean, String> result = Lotto.genNumberAndColor();
            boolean isBlack = result.keySet().iterator().next();
            int num = Integer.parseInt(result.values().iterator().next());

            if (isBlack) {
                assertTrue("true = чёрное: " + num + " должно быть в списке BLACK", BLACK_NUMBERS.contains(num));
            } else {
                assertFalse("false = красное: " + num + " не должно быть в списке BLACK", BLACK_NUMBERS.contains(num));
            }
        }
    }

    // -------------------------------------------------------------------------
    // learnTheColorOfNumber
    // -------------------------------------------------------------------------

    /** Известные чёрные числа возвращают true */
    @Test
    public void learnTheColorOfNumber_knownBlackNumbers_returnTrue() {
        assertTrue(Lotto.learnTheColorOfNumber("2"));
        assertTrue(Lotto.learnTheColorOfNumber("11"));
        assertTrue(Lotto.learnTheColorOfNumber("35"));
    }

    /** Красные и зелёные числа возвращают false */
    @Test
    public void learnTheColorOfNumber_redAndGreenNumbers_returnFalse() {
        assertFalse(Lotto.learnTheColorOfNumber("1"));   // красное
        assertFalse(Lotto.learnTheColorOfNumber("3"));   // красное
        assertFalse(Lotto.learnTheColorOfNumber("0"));   // зелёное (0)
        assertFalse(Lotto.learnTheColorOfNumber("36"));  // красное
    }

    // -------------------------------------------------------------------------
    // getRandomNumberForColor
    // -------------------------------------------------------------------------

    /** getRandomNumberForColor(true) всегда возвращает чёрное число */
    @Test
    public void getRandomNumberForColor_trueAlwaysReturnsBlackNumber() {
        for (int i = 0; i < 100; i++) {
            int num = Lotto.getRandomNumberForColor(true);
            assertTrue("Должно быть чёрным числом: " + num, BLACK_NUMBERS.contains(num));
        }
    }

    /** getRandomNumberForColor(false) всегда возвращает не-чёрное число в диапазоне 0–36 */
    @Test
    public void getRandomNumberForColor_falseAlwaysReturnsNonBlackNumber() {
        for (int i = 0; i < 100; i++) {
            int num = Lotto.getRandomNumberForColor(false);
            assertFalse("Не должно быть чёрным числом: " + num, BLACK_NUMBERS.contains(num));
            assertTrue("Число должно быть в диапазоне 0–36: " + num, num >= 0 && num <= 36);
        }
    }

    // -------------------------------------------------------------------------
    // getRandomColorForNumber
    // -------------------------------------------------------------------------

    /** getRandomColorForNumber совпадает с learnTheColorOfNumber для всех чисел 0–36 */
    @Test
    public void getRandomColorForNumber_matchesBlackListForAll0to36() {
        for (int i = 0; i <= 36; i++) {
            boolean expected = BLACK_NUMBERS.contains(i);
            boolean actual   = Lotto.getRandomColorForNumber(String.valueOf(i));
            assertEquals("Неверный цвет для числа " + i, expected, actual);
        }
    }
}