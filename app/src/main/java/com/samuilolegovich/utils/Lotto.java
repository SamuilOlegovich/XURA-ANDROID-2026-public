package com.samuilolegovich.utils;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;



/**
 * Криптографически случайная генерация чисел/цветов для игр рулетки и "Угадай число/цвет",
 * а также определение цвета (чёрный/красный) числа европейской рулетки по списку чёрных номеров.
 */
public class Lotto {
    private static SecureRandom secureRandom = new SecureRandom();
    private static List<Integer> black = List.of(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 33, 31, 35);

    /** Генерирует случайное "лотерейное" число для розыгрыша джекпота. */
    public static synchronized int genLotto() {
        return secureRandom.nextInt(21) * 3753;
    }



    /** Генерирует случайное число рулетки (0–36) и возвращает его вместе с признаком цвета (true — чёрный, false — не чёрный). */
    public static synchronized Map<Boolean, String> genNumberAndColor() {
        int i = secureRandom.nextInt(37);
        String s =  i < 10 ? "0" + i : i + "";
        if (black.contains(i)) {
            return Map.of(true, s);
        }
        return Map.of(false, s);
    }


    /** Определяет, является ли переданное число чёрным (true) согласно раскладке рулетки. */
    public static synchronized boolean learnTheColorOfNumber(String color) {
        int i = Integer.parseInt(color);
        if (black.contains(i)) {
            return true;
        }
        return false;
    }


    /** Возвращает случайное число рулетки заданного цвета: из списка чёрных чисел либо любое не из этого списка. */
    public static synchronized int getRandomNumberForColor(boolean color) {
        if (color) {
            return black.get(secureRandom.nextInt(18));
        } else {
            while (true) {
                int i = secureRandom.nextInt(37);
                if (!black.contains(i)) {
                    return i;
                }
            }
        }
    }


    /** Определяет, является ли переданное (в виде строки) число чёрным. */
    public static synchronized boolean getRandomColorForNumber(String number) {
        int i = Integer.parseInt(number);
        if (black.contains(i)) {
            return true;
        }
        return false;
    }

}
