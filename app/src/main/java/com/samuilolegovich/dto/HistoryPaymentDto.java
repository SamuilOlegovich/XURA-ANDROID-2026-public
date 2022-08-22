package com.samuilolegovich.dto;



public class HistoryPaymentDto {
    private String address;
    private String amount;
    private String tag;

    public HistoryPaymentDto(String address, String amount, String tag) {
        this.address = address;
        this.amount = amount;
        this.tag = tag;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
