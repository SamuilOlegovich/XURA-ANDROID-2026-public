package com.samuilolegovich.viewmodel;



public class NavigationEvent {
    public static final int LOST = 1;
    public static final int WIN = 2;
    public static final int YOUR_REFERRAL = 3;

    public final int type;
    public final String message;
    public final String lotto;



    public NavigationEvent(int type, String message, String lotto) {
        this.type = type;
        this.message = message;
        this.lotto = lotto;
    }
}