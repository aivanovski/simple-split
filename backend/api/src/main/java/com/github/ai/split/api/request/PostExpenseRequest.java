package com.github.ai.split.api.request;

import com.github.ai.split.api.UserUidDto;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class PostExpenseRequest {

    public String groupUid = "";
    public String title = "";
    public String description = "";
    public double amount = 0.0;
    public List<UserUidDto> paidBy = Collections.emptyList();
    public Boolean isSplitBetweenAll = null;
    public List<UserUidDto> splitBetween = Collections.emptyList();

    public PostExpenseRequest() {
        this.paidBy = new ArrayList<>();
        this.splitBetween = new ArrayList<>();
    }

    public PostExpenseRequest(String groupUid, String title, String description, double amount,
                              List<UserUidDto> paidBy, Boolean isSplitBetweenAll,
                              List<UserUidDto> splitBetween) {
        this.groupUid = groupUid;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy != null ? paidBy : new ArrayList<>();
        this.isSplitBetweenAll = isSplitBetweenAll;
        this.splitBetween = splitBetween != null ? splitBetween : new ArrayList<>();
    }
}

