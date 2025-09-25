package com.modernreservation.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper for all microservices
 * Provides consistent response structure across the Modern Reservation System
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API Response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Response status", example = "success", allowableValues = {"success", "error"})
    private String status;

    @Schema(description = "HTTP status code", example = "200")
    private Integer code;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error details (only present on error responses)")
    private ErrorDetails error;

    @Schema(description = "Response timestamp", example = "2025-09-25T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Request tracking ID", example = "req-123e4567-e89b-12d3-a456-426614174000")
    private String requestId;

    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(String status, Integer code, String message) {
        this();
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public ApiResponse(String status, Integer code, String message, T data) {
        this(status, code, message);
        this.data = data;
    }

    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", 200, "Operation completed successfully", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", 200, message, data);
    }

    public static <T> ApiResponse<T> success(Integer code, String message, T data) {
        return new ApiResponse<>("success", code, message, data);
    }

    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", 500, message);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>("error", code, message);
    }

    public static <T> ApiResponse<T> error(Integer code, String message, ErrorDetails error) {
        ApiResponse<T> response = new ApiResponse<>("error", code, message);
        response.setError(error);
        return response;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

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

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
