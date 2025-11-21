/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.exception;

import com.mktekhub.inventory.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Handle ResourceNotFoundException Returns 404 NOT FOUND */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage(),
            request.getRequestURI());

    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  /** Handle DuplicateResourceException Returns 409 CONFLICT */
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
      DuplicateResourceException ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Duplicate Resource",
            ex.getMessage(),
            request.getRequestURI());

    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  /** Handle InsufficientStockException Returns 400 BAD REQUEST */
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientStockException(
      InsufficientStockException ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Insufficient Stock",
            ex.getMessage(),
            request.getRequestURI());

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /** Handle WarehouseCapacityExceededException Returns 400 BAD REQUEST */
  @ExceptionHandler(WarehouseCapacityExceededException.class)
  public ResponseEntity<ErrorResponse> handleWarehouseCapacityExceededException(
      WarehouseCapacityExceededException ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Warehouse Capacity Exceeded",
            ex.getMessage(),
            request.getRequestURI());

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /** Handle InvalidOperationException Returns 400 BAD REQUEST */
  @ExceptionHandler(InvalidOperationException.class)
  public ResponseEntity<ErrorResponse> handleInvalidOperationException(
      InvalidOperationException ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Operation",
            ex.getMessage(),
            request.getRequestURI());

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle validation errors from @Valid annotations Returns 400 BAD REQUEST with field-specific
   * errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              validationErrors.put(fieldName, errorMessage);
            });

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Input validation failed. Please check the errors.",
            request.getRequestURI(),
            validationErrors);

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /** Handle authentication exceptions Returns 401 UNAUTHORIZED */
  @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      Exception ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            "Invalid username or password",
            request.getRequestURI());

    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  /** Handle access denied exceptions Returns 403 FORBIDDEN */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            "You do not have permission to access this resource",
            request.getRequestURI());

    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  /** Handle all other exceptions Returns 500 INTERNAL SERVER ERROR */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI());

    // Log the exception for debugging
    ex.printStackTrace();

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
