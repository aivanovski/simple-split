package com.github.ai.split.api.response;

import com.github.ai.split.api.GroupDto;
public class DeleteExpenseResponse {

    public GroupDto group = new GroupDto();

    public DeleteExpenseResponse() {}

    public DeleteExpenseResponse(GroupDto group) {
        this.group = group != null ? group : new GroupDto();
    }
}