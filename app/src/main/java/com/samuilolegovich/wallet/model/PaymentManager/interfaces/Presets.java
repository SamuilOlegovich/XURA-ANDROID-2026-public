package com.samuilolegovich.wallet.model.PaymentManager.interfaces;


import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.enums.BooleanEnum;

public interface Presets {
    void setPresets(BooleanEnum enums, boolean b);
    void setPresets(StringEnum enums, String s);
}
