package com.samuilolegovich.viewmodel;

/** Перечисление возможных ошибок валидации ставки в играх (рулетка, угадай число/цвет). */
public enum GameBetError {
    INVALID_AMOUNT,
    AMOUNT_IS_ZERO,
    INSUFFICIENT_BALANCE,
    BET_TOO_HIGH,
    BET_TOO_LOW,
    TAG_TOO_LARGE,
    PAYMENT_FAILED,
    NO_NUMBER_SELECTED
}
