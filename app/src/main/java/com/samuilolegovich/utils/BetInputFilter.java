package com.samuilolegovich.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * InputFilter для полей ввода ставки: блокирует символы, если результирующая строка
 * превысит maxValue или будет содержать более одного знака после запятой.
 *
 * Применение: editText.setFilters(new InputFilter[]{ new BetInputFilter(100.0) });
 */
public class BetInputFilter implements InputFilter {

    private final double maxValue;

    public BetInputFilter(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        // Собираем строку, которая получится после вставки
        String result = dest.subSequence(0, dstart).toString()
                + source.subSequence(start, end).toString()
                + dest.subSequence(dend, dest.length()).toString();

        // Пустая строка — разрешаем (поле очищается)
        if (result.isEmpty()) return null;

        // Только одна точка
        int dotCount = 0;
        for (char c : result.toCharArray()) if (c == '.') dotCount++;
        if (dotCount > 1) return "";

        // Не более одного знака после точки
        int dotIdx = result.indexOf('.');
        if (dotIdx >= 0 && result.length() - dotIdx > 2) return "";

        // Нельзя начинать с нуля перед цифрой (01, 07...) — только 0. допустим
        if (result.length() >= 2 && result.charAt(0) == '0' && result.charAt(1) != '.') return "";

        // Незавершённый ввод вроде "." или "10." — пропускаем без проверки диапазона
        if (result.equals(".") || result.endsWith(".")) return null;

        // Проверяем диапазон
        try {
            double val = Double.parseDouble(result);
            if (val > maxValue) return "";
        } catch (NumberFormatException e) {
            return "";
        }

        return null; // принять ввод
    }
}
