package com.samuilolegovich.viewmodel;

/** Перечисление возможных ошибок валидации при отправке платежа со страницы SendPayment. */
public enum SendError {
    WRONG_ADDRESS,
    INVALID_AMOUNT,
    AMOUNT_IS_ZERO,
    INSUFFICIENT_BALANCE,
    TAG_TOO_LONG,
    TAG_TOO_LARGE,
    PAYMENT_FAILED
}