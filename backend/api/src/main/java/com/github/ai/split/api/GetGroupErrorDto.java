package com.github.ai.split.api;

public class GetGroupErrorDto {

    public String uid = "";
    public String message = "";

    public GetGroupErrorDto() {}

    public GetGroupErrorDto(String uid, String message) {
        this.uid = uid;
        this.message = message;
    }
}