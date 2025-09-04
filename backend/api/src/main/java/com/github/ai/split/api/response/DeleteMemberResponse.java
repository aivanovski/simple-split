package com.github.ai.split.api.response;

import com.github.ai.split.api.GroupDto;
public class DeleteMemberResponse {

    public GroupDto group = new GroupDto();

    public DeleteMemberResponse() {}

    public DeleteMemberResponse(GroupDto group) {
        this.group = group != null ? group : new GroupDto();
    }
}