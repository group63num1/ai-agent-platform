package com.example.demo.common;

import java.time.Instant;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public ApiResponse() {}

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(0, "OK", data); }
    public static <T> ApiResponse<T> ok()       { return new ApiResponse<>(0, "OK", null); }
    public static <T> ApiResponse<T> fail(int code, String message) { return new ApiResponse<>(code, message, null); }

    public int getCode() { return code; }  public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }  public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }  public void setData(T data) { this.data = data; }
    public long getTimestamp() { return timestamp; }  public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
