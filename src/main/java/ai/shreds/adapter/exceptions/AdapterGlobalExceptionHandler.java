package ai.shreds.adapter.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import ai.shreds.application.exceptions.ApplicationServiceException;
import ai.shreds.domain.exceptions.DomainInsufficientFundsException;
import ai.shreds.domain.exceptions.DomainAccountFrozenException;
import ai.shreds.domain.exceptions.DomainInvalidCurrencyException;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;

import java.util.stream.Collectors;

/**
 * Global exception handler for the adapter layer.
 * Catches exceptions from controllers and converts them to standardized error responses.
 */
@ControllerAdvice
public class AdapterGlobalExceptionHandler {

    /**
     * Handles domain insufficient funds exceptions.
     */
    @ExceptionHandler(DomainInsufficientFundsException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleInsufficientFundsException(
            DomainInsufficientFundsException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "INSUFFICIENT_FUNDS",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles domain account frozen exceptions.
     */
    @ExceptionHandler(DomainAccountFrozenException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleAccountFrozenException(
            DomainAccountFrozenException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "ACCOUNT_FROZEN",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }

    /**
     * Handles domain invalid currency exceptions.
     */
    @ExceptionHandler(DomainInvalidCurrencyException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleInvalidCurrencyException(
            DomainInvalidCurrencyException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "INVALID_CURRENCY",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles adapter-specific service exceptions.
     */
    @ExceptionHandler(AdapterServiceException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleAdapterServiceException(
            AdapterServiceException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            ex.getErrorCode() != null ? ex.getErrorCode() : "ADAPTER_ERROR",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles application layer service exceptions.
     */
    @ExceptionHandler(ApplicationServiceException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleApplicationServiceException(
            ApplicationServiceException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            ex.getCode() != null ? ex.getCode() : "APPLICATION_ERROR",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "VALIDATION_ERROR",
            "Validation failed: " + errorMessage,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles bind exceptions for form data validation.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleBindException(
            BindException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "BIND_ERROR",
            "Data binding failed: " + errorMessage,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        String errorMessage = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "CONSTRAINT_VIOLATION",
            "Constraint violation: " + errorMessage,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles HTTP message not readable exceptions (malformed JSON).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "MALFORMED_REQUEST",
            "Malformed JSON request",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles method argument type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "INVALID_PARAMETER",
            String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName()),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles HTTP method not supported exceptions.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "METHOD_NOT_ALLOWED",
            String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handles no handler found exceptions (404 errors).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "NOT_FOUND",
            String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SharedErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.create(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}