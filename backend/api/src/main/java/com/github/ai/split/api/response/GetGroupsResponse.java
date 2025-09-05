package com.github.ai.split.api.response;

import com.github.ai.split.api.GroupDto;
import com.github.ai.split.api.GetGroupErrorDto;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class GetGroupsResponse {

    public List<GroupDto> groups = Collections.emptyList();
    public List<GetGroupErrorDto> errors = Collections.emptyList();

    public GetGroupsResponse() {
        this.groups = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public GetGroupsResponse(List<GroupDto> groups, List<GetGroupErrorDto> errors) {
        this.groups = groups != null ? groups : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
    }
}