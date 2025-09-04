package com.github.ai.split.api.request;

import com.github.ai.split.api.UserUidDto;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class PutExpenseRequest {

    public String title = "";
    public String description = "";
    public Double amount = null;
    public List<UserUidDto> paidBy = Collections.emptyList();
    public Boolean isSplitBetweenAll = null;
    public List<UserUidDto> splitBetween = Collections.emptyList();

    public PutExpenseRequest() {
        this.paidBy = new ArrayList<>();
        this.splitBetween = new ArrayList<>();
    }

    public PutExpenseRequest(String title, String description, Double amount,
                             List<UserUidDto> paidBy, Boolean isSplitBetweenAll,
                             List<UserUidDto> splitBetween) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy != null ? paidBy : new ArrayList<>();
        this.isSplitBetweenAll = isSplitBetweenAll;
        this.splitBetween = splitBetween != null ? splitBetween : new ArrayList<>();
    }
}

