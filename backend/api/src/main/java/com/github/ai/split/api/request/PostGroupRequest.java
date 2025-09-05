package com.github.ai.split.api.request;

import com.github.ai.split.api.NewExpenseDto;
import com.github.ai.split.api.UserNameDto;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class PostGroupRequest {

    public String password = "";
    public String title = "";
    public String description = "";
    public String currencyIsoCode = "";
    public List<UserNameDto> members = Collections.emptyList();
    public List<NewExpenseDto> expenses = Collections.emptyList();

    public PostGroupRequest() {
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    public PostGroupRequest(String password, String title, String description, String currencyIsoCode,
                            List<UserNameDto> members, List<NewExpenseDto> expenses) {
        this.password = password;
        this.title = title;
        this.description = description;
        this.currencyIsoCode = currencyIsoCode;
        this.members = members != null ? members : new ArrayList<>();
        this.expenses = expenses != null ? expenses : new ArrayList<>();
    }
}

