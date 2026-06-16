package com.samuilolegovich.wallet.enums;

/**
 * Перечисление изменяемых булевых флагов-настроек кошелька (наличие реального
 * и тестового кошелька, признак реального режима игры). Хранит значение прямо
 * в константе перечисления, что позволяет менять его во время работы приложения.
 */
public enum BooleanEnum {
    IS_WALLET(true),
    IS_WALLET_TEST(true),

    IS_REAL(false),
    ;

    private boolean b;

    /** Создаёт константу перечисления с начальным значением флага. */
    BooleanEnum(boolean b) {
        this.b = b;
    }

    /** Возвращает текущее значение флага. */
    public boolean isB() { return b; }
    /** Устанавливает новое значение флага для данной константы. */
    private void setValue(boolean b) { this.b = b; }

    /** Находит указанную константу перечисления и обновляет её значение флага. */
    public static void setValue(BooleanEnum enums, boolean b) {
        for (BooleanEnum e : BooleanEnum.values()) {
            if (e.equals(enums)) {
                e.setValue(b);
                break;
            }
        }
    }
}
