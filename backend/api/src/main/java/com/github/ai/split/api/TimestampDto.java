package com.github.ai.split.api;

public class TimestampDto {

    public long timestampSeconds = 0L;
    public String formatted = "";

    public TimestampDto() {}

    public TimestampDto(long timestampSeconds, String formatted) {
        this.timestampSeconds = timestampSeconds;
        this.formatted = formatted;
    }
}