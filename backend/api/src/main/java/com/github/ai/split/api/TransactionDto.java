package com.github.ai.split.api;

public class TransactionDto {

    public String creditorUid = "";
    public String debtorUid = "";
    public double amount = 0.0;

    public TransactionDto() {}

    public TransactionDto(String creditorUid, String debtorUid, double amount) {
        this.creditorUid = creditorUid;
        this.debtorUid = debtorUid;
        this.amount = amount;
    }
}