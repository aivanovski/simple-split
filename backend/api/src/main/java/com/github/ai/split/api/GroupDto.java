package com.github.ai.split.api;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class GroupDto {

    public String uid = "";
    public String title = "";
    public String description = "";
    public CurrencyDto currency = new CurrencyDto();
    public List<MemberDto> members = Collections.emptyList();
    public List<ExpenseDto> expenses = Collections.emptyList();
    public List<TransactionDto> paybackTransactions = Collections.emptyList();
    public TimestampDto created = new TimestampDto();
    public TimestampDto modified = new TimestampDto();

    public GroupDto() {
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.paybackTransactions = new ArrayList<>();
    }

    public GroupDto(String uid, String title, String description, CurrencyDto currency,
                    List<MemberDto> members, List<ExpenseDto> expenses, List<TransactionDto> paybackTransactions,
                    TimestampDto created, TimestampDto modified) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.currency = currency != null ? currency : new CurrencyDto();
        this.members = members != null ? members : new ArrayList<>();
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        this.paybackTransactions = paybackTransactions != null ? paybackTransactions : new ArrayList<>();
        this.created = created != null ? created : new TimestampDto();
        this.modified = modified != null ? modified : new TimestampDto();
    }
}