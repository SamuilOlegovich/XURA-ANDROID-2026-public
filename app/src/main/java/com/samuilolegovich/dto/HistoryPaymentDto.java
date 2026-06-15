package com.samuilolegovich.dto;

import java.io.Serializable;

public class HistoryPaymentDto implements Serializable {
    private String address;
    private String amount;
    private String tag;
    private String time;

    public HistoryPaymentDto(String address, String amount, String tag) {
        this(address, amount, tag, "");
    }

    public HistoryPaymentDto(String address, String amount, String tag, String time) {
        this.address = address;
        this.amount  = amount;
        this.tag     = tag;
        this.time    = time;
    }

    public String getAddress() { return address; }
    public void   setAddress(String address) { this.address = address; }

    public String getAmount() { return amount; }
    public void   setAmount(String amount) { this.amount = amount; }

    public String getTag() { return tag; }
    public void   setTag(String tag) { this.tag = tag; }

    public String getTime() { return time; }
    public void   setTime(String time) { this.time = time; }
}
