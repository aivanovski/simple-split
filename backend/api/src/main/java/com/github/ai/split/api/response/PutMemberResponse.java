package com.github.ai.split.api.response;

import com.github.ai.split.api.GroupDto;
public class PutMemberResponse {

    public GroupDto group = new GroupDto();

    public PutMemberResponse() {}

    public PutMemberResponse(GroupDto group) {
        this.group = group != null ? group : new GroupDto();
    }
}