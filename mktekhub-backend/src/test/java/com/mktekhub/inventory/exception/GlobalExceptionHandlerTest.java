package com.mktekhub.inventory.exception;

import com.mktekhub.inventory.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for GlobalExceptionHandler.
 * Tests all exception handlers and response formatting.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    // ==================== RESOURCE NOT FOUND EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle ResourceNotFoundException - Should return 404 with correct error response")
    void handleResourceNotFoundException_Returns404() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Item", "id", "123");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleResourceNotFoundException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.getStatus());
        assertEquals("Resource Not Found", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("Item"));
        assertTrue(errorResponse.getMessage().contains("id"));
        assertTrue(errorResponse.getMessage().contains("123"));
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Handle ResourceNotFoundException - Should include custom message")
    void handleResourceNotFoundException_IncludesCustomMessage() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Warehouse not found with id: 999");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleResourceNotFoundException(exception, request);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Warehouse not found with id: 999", errorResponse.getMessage());
    }

    // ==================== DUPLICATE RESOURCE EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle DuplicateResourceException - Should return 409 with correct error response")
    void handleDuplicateResourceException_Returns409() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
            "User with username 'testuser' already exists"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleDuplicateResourceException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(409, errorResponse.getStatus());
        assertEquals("Duplicate Resource", errorResponse.getError());
        assertEquals("User with username 'testuser' already exists", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Handle DuplicateResourceException - Should handle duplicate SKU message")
    void handleDuplicateResourceException_DuplicateSku() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
            "Item with SKU 'ELEC-001' already exists"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleDuplicateResourceException(exception, request);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getMessage().contains("SKU"));
        assertTrue(errorResponse.getMessage().contains("ELEC-001"));
    }

    // ==================== INSUFFICIENT STOCK EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle InsufficientStockException - Should return 400 with correct error response")
    void handleInsufficientStockException_Returns400() {
        // Arrange
        InsufficientStockException exception = new InsufficientStockException(
            "Insufficient stock. Required: 10, Available: 5"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleInsufficientStockException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Insufficient Stock", errorResponse.getError());
        assertEquals("Insufficient stock. Required: 10, Available: 5", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    // ==================== WAREHOUSE CAPACITY EXCEEDED EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle WarehouseCapacityExceededException - Should return 400 with correct error response")
    void handleWarehouseCapacityExceededException_Returns400() {
        // Arrange
        WarehouseCapacityExceededException exception = new WarehouseCapacityExceededException(
            "Warehouse capacity exceeded. Max: 1000, Current: 950, Adding: 100"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleWarehouseCapacityExceededException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Warehouse Capacity Exceeded", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("capacity exceeded"));
        assertEquals("/api/test", errorResponse.getPath());
    }

    // ==================== INVALID OPERATION EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle InvalidOperationException - Should return 400 with correct error response")
    void handleInvalidOperationException_Returns400() {
        // Arrange
        InvalidOperationException exception = new InvalidOperationException(
            "Cannot delete warehouse with existing inventory items"
        );

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleInvalidOperationException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Invalid Operation", errorResponse.getError());
        assertEquals("Cannot delete warehouse with existing inventory items", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
    }

    // ==================== VALIDATION EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle MethodArgumentNotValidException - Should return 400 with validation errors")
    void handleValidationException_Returns400WithValidationErrors() {
        // Arrange
        FieldError fieldError1 = new FieldError("inventoryItem", "name", "Name is required");
        FieldError fieldError2 = new FieldError("inventoryItem", "quantity", "Quantity must be positive");

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleValidationException(methodArgumentNotValidException, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Validation Failed", errorResponse.getError());
        assertEquals("Input validation failed. Please check the errors.", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());

        // Verify validation errors
        Map<String, String> validationErrors = errorResponse.getValidationErrors();
        assertNotNull(validationErrors);
        assertEquals(2, validationErrors.size());
        assertEquals("Name is required", validationErrors.get("name"));
        assertEquals("Quantity must be positive", validationErrors.get("quantity"));
    }

    @Test
    @DisplayName("Handle MethodArgumentNotValidException - Should handle single field error")
    void handleValidationException_SingleFieldError() {
        // Arrange
        FieldError fieldError = new FieldError("user", "email", "Email must be valid");

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleValidationException(methodArgumentNotValidException, request);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);

        Map<String, String> validationErrors = errorResponse.getValidationErrors();
        assertNotNull(validationErrors);
        assertEquals(1, validationErrors.size());
        assertEquals("Email must be valid", validationErrors.get("email"));
    }

    @Test
    @DisplayName("Handle MethodArgumentNotValidException - Should handle multiple errors on same field")
    void handleValidationException_MultipleErrorsOnSameField() {
        // Arrange
        FieldError fieldError1 = new FieldError("user", "password", "Password is required");
        FieldError fieldError2 = new FieldError("user", "password", "Password must be at least 8 characters");

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleValidationException(methodArgumentNotValidException, request);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        Map<String, String> validationErrors = errorResponse.getValidationErrors();

        // Last error for the field should be kept
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.containsKey("password"));
    }

    // ==================== AUTHENTICATION EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle AuthenticationException - Should return 401 with correct error response")
    void handleAuthenticationException_Returns401() {
        // Arrange
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAuthenticationException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(401, errorResponse.getStatus());
        assertEquals("Authentication Failed", errorResponse.getError());
        assertEquals("Invalid username or password", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
    }

    @Test
    @DisplayName("Handle BadCredentialsException - Should return 401 with generic message")
    void handleBadCredentialsException_Returns401WithGenericMessage() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Wrong password");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAuthenticationException(exception, request);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        // Should return generic message for security (don't reveal whether username or password is wrong)
        assertEquals("Invalid username or password", errorResponse.getMessage());
    }

    // ==================== ACCESS DENIED EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle AccessDeniedException - Should return 403 with correct error response")
    void handleAccessDeniedException_Returns403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAccessDeniedException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(403, errorResponse.getStatus());
        assertEquals("Access Denied", errorResponse.getError());
        assertEquals("You do not have permission to access this resource", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
    }

    // ==================== GLOBAL EXCEPTION TESTS ====================

    @Test
    @DisplayName("Handle Generic Exception - Should return 500 with correct error response")
    void handleGlobalException_Returns500() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGlobalException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("An unexpected error occurred. Please try again later.", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
    }

    @Test
    @DisplayName("Handle Generic Exception - Should handle NullPointerException")
    void handleGlobalException_NullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null value encountered");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGlobalException(exception, request);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
        // Should return generic message, not the actual exception message
        assertEquals("An unexpected error occurred. Please try again later.", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Handle Generic Exception - Should handle IllegalArgumentException")
    void handleGlobalException_IllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGlobalException(exception, request);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("All handlers - Should include request URI in error response")
    void allHandlers_IncludeRequestUri() {
        // Test with different URIs
        when(request.getRequestURI()).thenReturn("/api/inventory/123");

        ResourceNotFoundException exception = new ResourceNotFoundException("Item", "id", "123");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleResourceNotFoundException(exception, request);

        assertEquals("/api/inventory/123", response.getBody().getPath());
    }

    @Test
    @DisplayName("All handlers - Should include timestamp")
    void allHandlers_IncludeTimestamp() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Item", "id", "1");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleResourceNotFoundException(exception, request);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Error responses - Should have consistent structure")
    void errorResponses_ConsistentStructure() {
        // Test multiple exception types for consistency

        // 1. ResourceNotFoundException
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Item", "id", "1");
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler
            .handleResourceNotFoundException(notFoundEx, request);
        ErrorResponse error1 = response1.getBody();

        // 2. DuplicateResourceException
        DuplicateResourceException duplicateEx = new DuplicateResourceException("Duplicate");
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler
            .handleDuplicateResourceException(duplicateEx, request);
        ErrorResponse error2 = response2.getBody();

        // 3. InvalidOperationException
        InvalidOperationException invalidEx = new InvalidOperationException("Invalid");
        ResponseEntity<ErrorResponse> response3 = globalExceptionHandler
            .handleInvalidOperationException(invalidEx, request);
        ErrorResponse error3 = response3.getBody();

        // Assert all have required fields
        assertNotNull(error1.getTimestamp());
        assertNotNull(error1.getStatus());
        assertNotNull(error1.getError());
        assertNotNull(error1.getMessage());
        assertNotNull(error1.getPath());

        assertNotNull(error2.getTimestamp());
        assertNotNull(error2.getStatus());
        assertNotNull(error2.getError());
        assertNotNull(error2.getMessage());
        assertNotNull(error2.getPath());

        assertNotNull(error3.getTimestamp());
        assertNotNull(error3.getStatus());
        assertNotNull(error3.getError());
        assertNotNull(error3.getMessage());
        assertNotNull(error3.getPath());
    }
}
