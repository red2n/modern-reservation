package com.modernreservation.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Error details for API responses
 * Provides structured error information for debugging and client handling
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed error information")
public class ErrorDetails {

    @Schema(description = "Error code for programmatic handling", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Detailed error description", example = "Invalid request parameters")
    private String details;

    @Schema(description = "Field-specific validation errors")
    private Map<String, String> fieldErrors;

    @Schema(description = "List of validation errors")
    private List<String> errors;

    @Schema(description = "Stack trace (only in development mode)")
    private String stackTrace;

    // Constructors
    public ErrorDetails() {}

    public ErrorDetails(String errorCode, String details) {
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorDetails(String errorCode, String details, Map<String, String> fieldErrors) {
        this(errorCode, details);
        this.fieldErrors = fieldErrors;
    }

    public ErrorDetails(String errorCode, String details, List<String> errors) {
        this(errorCode, details);
        this.errors = errors;
    }

    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}
