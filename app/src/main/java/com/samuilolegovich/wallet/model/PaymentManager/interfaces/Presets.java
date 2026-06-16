package com.samuilolegovich.wallet.model.PaymentManager.interfaces;


import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.enums.BooleanEnum;

/** Контракт для сохранения предустановленных значений (флаги и строки) во внутреннем хранилище настроек кошелька. */
public interface Presets {
    /** Сохраняет значение булевого флага-настройки. */
    void setPresets(BooleanEnum enums, boolean b);
    /** Сохраняет значение строковой настройки. */
    void setPresets(StringEnum enums, String s);
}
