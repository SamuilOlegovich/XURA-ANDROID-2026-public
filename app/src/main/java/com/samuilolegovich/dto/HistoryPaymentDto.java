package com.samuilolegovich.dto;

import java.io.Serializable;

/**
 * DTO одной строки истории платежей: адрес контрагента, сумма, служебный тег операции и время.
 * Реализует Serializable, чтобы список таких объектов можно было передавать между Activity через Intent/Bundle.
 */
public class HistoryPaymentDto implements Serializable {
    private String address;
    private String amount;
    private String tag;
    private String time;

    /** Создаёт запись истории без указания времени (используется, когда время транзакции неизвестно). */
    public HistoryPaymentDto(String address, String amount, String tag) {
        this(address, amount, tag, "");
    }

    /** Создаёт полную запись истории платежа со всеми полями. */
    public HistoryPaymentDto(String address, String amount, String tag, String time) {
        this.address = address;
        this.amount  = amount;
        this.tag     = tag;
        this.time    = time;
    }

    /** Возвращает XRPL-адрес контрагента транзакции. */
    public String getAddress() { return address; }
    /** Задаёт XRPL-адрес контрагента транзакции. */
    public void   setAddress(String address) { this.address = address; }

    /** Возвращает сумму транзакции с знаком и единицей измерения (например "+1.5 XRP"). */
    public String getAmount() { return amount; }
    /** Задаёт сумму транзакции. */
    public void   setAmount(String amount) { this.amount = amount; }

    /** Возвращает служебный тег операции (тип ставки, выигрыша, реферала и т.д.), определяющий иконку и подпись. */
    public String getTag() { return tag; }
    /** Задаёт служебный тег операции. */
    public void   setTag(String tag) { this.tag = tag; }

    /** Возвращает отформатированное время транзакции (или пустую строку, если оно неизвестно). */
    public String getTime() { return time; }
    /** Задаёт отформатированное время транзакции. */
    public void   setTime(String time) { this.time = time; }
}
