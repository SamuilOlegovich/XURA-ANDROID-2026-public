package com.samuilolegovich.utils;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;



/**
 * Криптографически случайная генерация чисел/цветов для игр рулетки и "Угадай число/цвет",
 * а также определение цвета (чёрный/красный) числа европейской рулетки по списку чёрных номеров.
 *
 * SecureRandom потокобезопасен сам по себе — synchronized не нужен.
 */
public class Lotto {
    private static final SecureRandom secureRandom = new SecureRandom();

    // 18 чёрных чисел европейской рулетки
    private static final List<Integer> black = List.of(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35);

    // 19 не-чёрных чисел (красные + зелёный 0) — предвычислено, чтобы избежать цикла rejection sampling
    private static final List<Integer> notBlack = List.of(0, 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);

    /** Генерирует случайное "лотерейное" число для розыгрыша джекпота. */
    public static int genLotto() {
        return secureRandom.nextInt(21) * 3753;
    }

    /** Генерирует случайное число рулетки (0–36) и возвращает его вместе с признаком цвета (true — чёрный, false — не чёрный). */
    public static Map<Boolean, String> genNumberAndColor() {
        int i = secureRandom.nextInt(37);
        String s = i < 10 ? "0" + i : i + "";
        return Map.of(black.contains(i), s);
    }

    /** Определяет, является ли переданное число чёрным (true) согласно раскладке рулетки. */
    public static boolean learnTheColorOfNumber(String color) {
        return black.contains(Integer.parseInt(color));
    }

    /** Возвращает случайное число рулетки заданного цвета: из списка чёрных либо из списка не-чёрных. */
    public static int getRandomNumberForColor(boolean color) {
        if (color) {
            return black.get(secureRandom.nextInt(black.size()));
        }
        return notBlack.get(secureRandom.nextInt(notBlack.size()));
    }

    /** Определяет, является ли переданное (в виде строки) число чёрным. */
    public static boolean getRandomColorForNumber(String number) {
        return black.contains(Integer.parseInt(number));
    }
}
