package com.samuilolegovich.enums;

/**
 * Таблица компактных кодов для всех типов ставок рулетки, используемых в XRP memo.
 * Memo ограничено по длине, поэтому полные теги ставок ("RED", "D1" и т.д.) сворачиваются
 * в однобуквенные/двубуквенные коды при отправке транзакции на сервер.
 *
 * Пример формата memo (несколько ставок сразу):
 *   RLT:n5@1.5,r@2.0,d1@0.5:referralCode
 *
 * Ставки на конкретное число N:0–N:36 преобразуются в n0–n36 (обрабатывается программно ниже).
 *
 * Полная таблица соответствий:
 * ┌────────────┬──────┬──────────────────────┬──────────┐
 * │ Тег (полный)│ Код  │ Описание             │ Выплата  │
 * ├────────────┼──────┼──────────────────────┼──────────┤
 * │ N:0        │ n0   │ Ставка на ноль       │ x35      │
 * │ N:1–N:36   │ n1–n36 │ Ставка на число    │ x35      │
 * │ RED        │ r    │ Красный цвет         │ x2       │
 * │ BLACK      │ b    │ Чёрный цвет          │ x2       │
 * │ ODD        │ o    │ Нечётные числа       │ x2       │
 * │ EVEN       │ e    │ Чётные числа         │ x2       │
 * │ LOW        │ l    │ 1–18                 │ x2       │
 * │ HIGH       │ h    │ 19–36                │ x2       │
 * │ D1         │ d1   │ 1-я дюжина (1–12)    │ x3       │
 * │ D2         │ d2   │ 2-я дюжина (13–24)   │ x3       │
 * │ D3         │ d3   │ 3-я дюжина (25–36)   │ x3       │
 * │ C1         │ c1   │ 1-я колонка          │ x3       │
 * │ C2         │ c2   │ 2-я колонка          │ x3       │
 * │ C3         │ c3   │ 3-я колонка          │ x3       │
 * └────────────┴──────┴──────────────────────┴──────────┘
 */
public enum RouletteBetCode {

    RED  ("RED",   "r",  2),
    BLACK("BLACK", "b",  2),
    ODD  ("ODD",   "o",  2),
    EVEN ("EVEN",  "e",  2),
    LOW  ("LOW",   "l",  2),
    HIGH ("HIGH",  "h",  2),
    D1   ("D1",    "d1", 3),
    D2   ("D2",    "d2", 3),
    D3   ("D3",    "d3", 3),
    C1   ("C1",    "c1", 3),
    C2   ("C2",    "c2", 3),
    C3   ("C3",    "c3", 3);

    /** Полный внутренний тег, используемый по всему приложению (например "RED", "D1", "N:5"). */
    public final String fullTag;
    /** Компактный код, отправляемый в memo XRP-транзакции (например "r", "d1", "n5"). */
    public final String code;
    /** Множитель выигрыша, применяемый к сумме ставки. */
    public final int multiplier;

    /** Связывает полный тег ставки с её компактным кодом для memo и множителем выплаты. */
    RouletteBetCode(String fullTag, String code, int multiplier) {
        this.fullTag = fullTag;
        this.code = code;
        this.multiplier = multiplier;
    }

    // ── Преобразования ────────────────────────────────────────────────

    /** Преобразует полный внутренний тег ставки в его компактный код для memo. */
    public static String tagToCode(String tag) {
        if (tag != null && tag.startsWith("N:")) {
            return "n" + tag.substring(2);
        }
        for (RouletteBetCode b : values()) {
            if (b.fullTag.equals(tag)) return b.code;
        }
        return tag != null ? tag.toLowerCase() : "";
    }

    /** Преобразует компактный код из memo обратно в полный внутренний тег ставки. */
    public static String codeToTag(String code) {
        if (code != null && code.length() >= 2 && code.charAt(0) == 'n') {
            String numPart = code.substring(1);
            try {
                int n = Integer.parseInt(numPart);
                if (n >= 0 && n <= 36) return "N:" + n;
            } catch (NumberFormatException ignored) {}
        }
        for (RouletteBetCode b : values()) {
            if (b.code.equalsIgnoreCase(code)) return b.fullTag;
        }
        return code != null ? code.toUpperCase() : "";
    }

    /** Возвращает множитель выигрыша для заданного полного внутреннего тега ставки. */
    public static int multiplierForTag(String tag) {
        if (tag != null && tag.startsWith("N:")) return 35;
        for (RouletteBetCode b : values()) {
            if (b.fullTag.equals(tag)) return b.multiplier;
        }
        return 2;
    }
}
