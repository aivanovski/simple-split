package com.github.ai.split.api;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class ExpenseDto {

    public String uid = "";
    public String title = "";
    public String description = "";
    public double amount = 0.0;
    public CurrencyDto currency = new CurrencyDto();
    public List<MemberDto> paidBy = Collections.emptyList();
    public List<MemberDto> splitBetween = Collections.emptyList();
    public TimestampDto created = new TimestampDto();
    public TimestampDto modified = new TimestampDto();

    public ExpenseDto() {
        this.paidBy = new ArrayList<>();
        this.splitBetween = new ArrayList<>();
    }

    public ExpenseDto(String uid, String title, String description, double amount,
                      CurrencyDto currency, List<MemberDto> paidBy, List<MemberDto> splitBetween,
                      TimestampDto created, TimestampDto modified) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.currency = currency != null ? currency : new CurrencyDto();
        this.paidBy = paidBy != null ? paidBy : new ArrayList<>();
        this.splitBetween = splitBetween != null ? splitBetween : new ArrayList<>();
        this.created = created != null ? created : new TimestampDto();
        this.modified = modified != null ? modified : new TimestampDto();
    }
}