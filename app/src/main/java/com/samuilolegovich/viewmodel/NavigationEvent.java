package com.samuilolegovich.viewmodel;



/**
 * Событие одноразовой навигации, отправляемое из ViewModel в Activity через LiveData:
 * сообщает, на какой экран перейти после завершения игры (проигрыш, выигрыш,
 * экран реферала), и какой текст/лотерейный номер передать туда.
 */
public class NavigationEvent {
    public static final int LOST = 1;
    public static final int WIN = 2;
    public static final int YOUR_REFERRAL = 3;

    public final int type;
    public final String message;
    public final String lotto;



    /** Создаёт событие навигации заданного типа с сообщением и значением лото для целевого экрана. */
    public NavigationEvent(int type, String message, String lotto) {
        this.type = type;
        this.message = message;
        this.lotto = lotto;
    }
}