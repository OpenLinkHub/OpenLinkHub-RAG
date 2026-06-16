package com.openlinkhub.rag.admin.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    public ApiResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("data") T data,
            @JsonProperty("message") String message
    ) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>(true, data, "");
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<T>(false, null, message);
    }

    @JsonProperty("success")
    public boolean isSuccess() {
        return success;
    }

    @JsonProperty("data")
    public T getData() {
        return data;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }
}
