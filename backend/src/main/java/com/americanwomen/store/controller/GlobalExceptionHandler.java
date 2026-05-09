package com.americanwomen.store.controller;

import com.americanwomen.store.dto.ErrorResponse;
import com.americanwomen.store.ocl.OclValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OclValidationException.class)
    public ResponseEntity<com.americanwomen.store.dto.OclErrorResponse> handleOclValidationException(OclValidationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new com.americanwomen.store.dto.OclErrorResponse("OCL validation failed", e.getViolations()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        // Log for debugging (but don't expose details)
        System.err.println("[GlobalExceptionHandler] Bad credentials - returning 401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Invalid username or password"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        // Log validation errors for debugging
        System.err.println("[GlobalExceptionHandler] Validation failed: " + errors.toString());
        
        // Create a user-friendly message
        StringBuilder message = new StringBuilder("Invalid input: ");
        boolean first = true;
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            if (!first) message.append(", ");
            message.append(entry.getKey()).append(" - ").append(entry.getValue());
            first = false;
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(message.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An error occurred: " + e.getMessage()));
    }
}



