package com.example.transaction_service.util;

public class ApiResponse<T> {
    private String message;
    private T data;
    private boolean success;
    private Integer statusCode;

    public ApiResponse(String message, T data, Integer statusCode) {
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
        this.success = true; // Default to true, unless specified otherwise
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }
}
