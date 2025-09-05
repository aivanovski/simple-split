package com.github.ai.split.api.request;

import com.github.ai.split.api.UserUidDto;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class PutGroupRequest {

    public String title = null;
    public String password = null;
    public String description = null;
    public String currencyIsoCode = null;
    public List<UserUidDto> members = Collections.emptyList();

    public PutGroupRequest() {
        this.members = new ArrayList<>();
    }

    public PutGroupRequest(String title, String password, String description, String currencyIsoCode,
                           List<UserUidDto> members) {
        this.title = title;
        this.password = password;
        this.description = description;
        this.currencyIsoCode = currencyIsoCode;
        this.members = members != null ? members : new ArrayList<>();
    }
}

