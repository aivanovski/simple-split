package com.github.ai.split.api.response;

import com.github.ai.split.api.GroupDto;
public class PostMemberResponse {

    public GroupDto group = new GroupDto();

    public PostMemberResponse() {}

    public PostMemberResponse(GroupDto group) {
        this.group = group != null ? group : new GroupDto();
    }
}

