package com.github.ai.split.api.response;

import com.github.ai.split.api.GroupDto;
public class PostGroupResponse {

    public GroupDto group = new GroupDto();

    public PostGroupResponse() {}

    public PostGroupResponse(GroupDto group) {
        this.group = group != null ? group : new GroupDto();
    }
}