package com.github.ai.split.api.request;

public class PostMemberRequest {

    public String groupUid = "";
    public String name = "";

    public PostMemberRequest() {
    }

    public PostMemberRequest(String groupUid, String name) {
        this.groupUid = groupUid;
        this.name = name;
    }
}
