package com.github.ai.split.api;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class NewExpenseDto {

    public String title = "";
    public String description = "";
    public double amount = 0.0;
    public List<UserNameDto> paidBy = Collections.emptyList();
    public Boolean isSplitBetweenAll = null;
    public List<UserNameDto> splitBetween = Collections.emptyList();

    public NewExpenseDto() {
        this.paidBy = new ArrayList<>();
        this.splitBetween = new ArrayList<>();
    }

    public NewExpenseDto(String title, String description, double amount,
                         List<UserNameDto> paidBy, Boolean isSplitBetweenAll,
                         List<UserNameDto> splitBetween) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy != null ? paidBy : new ArrayList<>();
        this.isSplitBetweenAll = isSplitBetweenAll;
        this.splitBetween = splitBetween != null ? splitBetween : new ArrayList<>();
    }
}

