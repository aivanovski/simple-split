package com.github.ai.split.api.response;

import com.github.ai.split.api.GroupDto;
public class PutGroupResponse {

    public GroupDto group = new GroupDto();

    public PutGroupResponse() {}

    public PutGroupResponse(GroupDto group) {
        this.group = group != null ? group : new GroupDto();
    }
}