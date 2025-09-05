package com.github.ai.split.api;

import java.util.Collections;
import java.util.List;

public class ErrorMessageDto {

    public String message = null;
    public String exception = "";
    public String stacktraceBase64 = "";
    public List<String> stacktraceLines = Collections.emptyList();

    public ErrorMessageDto() {
    }

    public ErrorMessageDto(String message, String exception, String stacktraceBase64, List<String> stacktraceLines) {
        this.message = message;
        this.exception = exception;
        this.stacktraceBase64 = stacktraceBase64;
        this.stacktraceLines = stacktraceLines;
    }
}
