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
            
            log.error("Field: {}\nRejected Value: {}\nMessage: {}", fieldName, rejectedValue, errorMessage);
            
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation Failed");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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
