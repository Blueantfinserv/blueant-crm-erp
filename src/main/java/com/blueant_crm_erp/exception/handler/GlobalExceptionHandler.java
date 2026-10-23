package com.blueant_crm_erp.exception.handler;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("Business error [{}]: {}", ex.getErrorCode(), ex.getMessage(), ex);
        ApiError apiError = errorResponseBuilder.build(ex, request);
        return new ResponseEntity<>(apiError, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            
            Object loggedValue = isSensitiveField(fieldName) ? "[PROTECTED]" : rejectedValue;
            log.error("Field: {}\nRejected Value: {}\nMessage: {}", fieldName, loggedValue, errorMessage);
            
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation Failed");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lower = fieldName.toLowerCase();
        return lower.contains("password")
                || lower.contains("token")
                || lower.contains("secret")
                || lower.contains("otp")
                || lower.contains("credential")
                || lower.contains("auth");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied error: {}", ex.getMessage(), ex);
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                "Access Denied",
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("JSON parse / message not readable error: {}", ex.getMessage(), ex);
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                "Invalid input format or invalid enum value.",
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.error("Method argument type mismatch error: {}", ex.getMessage(), ex);
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                "Invalid parameter value: " + ex.getValue(),
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation error: {}", ex.getMessage(), ex);
        String causeMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        boolean isDuplicate = causeMsg != null && (
                causeMsg.toLowerCase().contains("duplicate") ||
                causeMsg.toLowerCase().contains("uk_") ||
                causeMsg.toLowerCase().contains("unique")
        );

        if (isDuplicate) {
            ApiError apiError = errorResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.DUPLICATE_RESOURCE,
                    ErrorType.BUSINESS,
                    "Duplicate entry or unique constraint violation: " + causeMsg,
                    request
            );
            return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        } else {
            ApiError apiError = errorResponseBuilder.build(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    ErrorType.SYSTEM,
                    "Database integrity failure: " + causeMsg,
                    request
            );
            return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Resource or endpoint not found: {} {}", request.getMethod(), request.getRequestURI());
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                "Resource not found: " + request.getRequestURI(),
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandlerFound(org.springframework.web.servlet.NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found for: {} {}", request.getMethod(), request.getRequestURI());
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                "Resource not found: " + request.getRequestURI(),
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported: {} {}", request.getMethod(), request.getRequestURI());
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.BAD_REQUEST,
                ErrorType.VALIDATION,
                "Request method '" + request.getMethod() + "' is not supported for this endpoint.",
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiError apiError = errorResponseBuilder.build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorType.SYSTEM,
                "An unexpected error occurred",
                request
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
