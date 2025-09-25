package com.modernreservation.common.exception;

import com.modernreservation.common.dto.ApiResponse;
import com.modernreservation.common.dto.ErrorDetails;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all microservices
 * Provides consistent error responses across the Modern Reservation System
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorDetails errorDetails = new ErrorDetails(
            "VALIDATION_ERROR",
            "Request validation failed",
            fieldErrors
        );

        ApiResponse<Void> response = ApiResponse.error(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errorDetails
        );

        logger.warn("Validation error: {}", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle constraint violation errors from @Validated
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Constraint violation",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorDetails errorDetails = new ErrorDetails(
            "CONSTRAINT_VIOLATION",
            "Constraint validation failed",
            errors
        );

        ApiResponse<Void> response = ApiResponse.error(
            HttpStatus.BAD_REQUEST.value(),
            "Constraint validation failed",
            errorDetails
        );

        logger.warn("Constraint violation: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle argument type mismatch errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid argument type",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
            "TYPE_MISMATCH",
            String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                    ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName())
        );

        ApiResponse<Void> response = ApiResponse.error(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid parameter type",
            errorDetails
        );

        logger.warn("Type mismatch error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Business logic error",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
            ex.getErrorCode(),
            ex.getMessage()
        );

        ApiResponse<Void> response = ApiResponse.error(
            ex.getHttpStatus().value(),
            ex.getMessage(),
            errorDetails
        );

        logger.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
            "RESOURCE_NOT_FOUND",
            ex.getMessage()
        );

        ApiResponse<Void> response = ApiResponse.error(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            errorDetails
        );

        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
            "INTERNAL_ERROR",
            "An unexpected error occurred"
        );

        ApiResponse<Void> response = ApiResponse.error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error",
            errorDetails
        );

        logger.error("Unexpected error", ex);
        return ResponseEntity.internalServerError().body(response);
    }
}
