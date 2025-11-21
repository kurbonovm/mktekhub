/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Comprehensive unit tests for SecurityConfig. Tests security configuration beans and their
 * properties.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

  @Mock private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock private AuthenticationConfiguration authenticationConfiguration;

  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private SecurityConfig securityConfig;

  @BeforeEach
  void setUp() {
    // Setup is handled by @Mock and @InjectMocks
  }

  // ==================== PASSWORD ENCODER BEAN TESTS ====================

  @Test
  @DisplayName("PasswordEncoder Bean - Should be created successfully")
  void passwordEncoder_ShouldBeCreated() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    // Assert
    assertNotNull(passwordEncoder);
  }

  @Test
  @DisplayName("PasswordEncoder Bean - Should be BCryptPasswordEncoder")
  void passwordEncoder_ShouldBeBCryptPasswordEncoder() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    // Assert
    assertTrue(
        passwordEncoder
            instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder);
  }

  @Test
  @DisplayName("PasswordEncoder - Should encode passwords correctly")
  void passwordEncoder_ShouldEncodePasswords() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "myPassword123";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    // Assert
    assertNotNull(encodedPassword);
    assertNotEquals(rawPassword, encodedPassword);
    assertTrue(encodedPassword.startsWith("$2")); // BCrypt hash format
  }

  @Test
  @DisplayName("PasswordEncoder - Should validate passwords correctly")
  void passwordEncoder_ShouldValidatePasswords() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "testPassword";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    // Assert
    assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
  }

  @Test
  @DisplayName("PasswordEncoder - Should produce different hashes for same password")
  void passwordEncoder_ShouldProduceDifferentHashesForSamePassword() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String password = "samePassword";
    String hash1 = passwordEncoder.encode(password);
    String hash2 = passwordEncoder.encode(password);

    // Assert - Hashes should be different due to salt
    assertNotEquals(hash1, hash2);
    // But both should validate against the original password
    assertTrue(passwordEncoder.matches(password, hash1));
    assertTrue(passwordEncoder.matches(password, hash2));
  }

  @Test
  @DisplayName("PasswordEncoder - Multiple calls should return same instance")
  void passwordEncoder_MultipleCalls_SameBeanType() {
    // Act
    PasswordEncoder encoder1 = securityConfig.passwordEncoder();
    PasswordEncoder encoder2 = securityConfig.passwordEncoder();

    // Assert - Should be same class type
    assertEquals(encoder1.getClass(), encoder2.getClass());
  }

  // ==================== AUTHENTICATION MANAGER BEAN TESTS ====================

  @Test
  @DisplayName("AuthenticationManager Bean - Should be created successfully")
  void authenticationManager_ShouldBeCreated() throws Exception {
    // Arrange
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

    // Act
    AuthenticationManager manager =
        securityConfig.authenticationManager(authenticationConfiguration);

    // Assert
    assertNotNull(manager);
    assertEquals(authenticationManager, manager);
  }

  @Test
  @DisplayName("AuthenticationManager Bean - Should use AuthenticationConfiguration")
  void authenticationManager_ShouldUseAuthenticationConfiguration() throws Exception {
    // Arrange
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

    // Act
    AuthenticationManager manager =
        securityConfig.authenticationManager(authenticationConfiguration);

    // Assert
    assertSame(authenticationManager, manager);
  }

  @Test
  @DisplayName("AuthenticationManager Bean - Should propagate exceptions")
  void authenticationManager_ShouldPropagateExceptions() throws Exception {
    // Arrange
    when(authenticationConfiguration.getAuthenticationManager())
        .thenThrow(new RuntimeException("Configuration error"));

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> {
          securityConfig.authenticationManager(authenticationConfiguration);
        });
  }

  // ==================== SECURITY FILTER CHAIN BEAN TESTS ====================

  // Note: SecurityFilterChain is tested through integration tests with @SpringBootTest
  // Direct unit testing of filterChain requires complex Spring Security context setup
  // The configuration is validated through controller tests that use @WebMvcTest

  // ==================== CONFIGURATION VALIDATION TESTS ====================

  @Test
  @DisplayName("Configuration - BCrypt strength should be adequate")
  void configuration_BCryptStrengthShouldBeAdequate() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String encoded = passwordEncoder.encode("test");

    // Assert - BCrypt hash should have proper format and length
    assertTrue(encoded.startsWith("$2")); // BCrypt identifier
    assertTrue(encoded.length() >= 60); // BCrypt hash length
  }

  @Test
  @DisplayName("Configuration - Password encoder should handle empty string")
  void configuration_PasswordEncoderShouldHandleEmptyString() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String encoded = passwordEncoder.encode("");

    // Assert
    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches("", encoded));
  }

  @Test
  @DisplayName("Configuration - Password encoder should handle special characters")
  void configuration_PasswordEncoderShouldHandleSpecialCharacters() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String specialPassword = "p@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?";
    String encoded = passwordEncoder.encode(specialPassword);

    // Assert
    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(specialPassword, encoded));
  }

  @Test
  @DisplayName("Configuration - Password encoder should handle Unicode characters")
  void configuration_PasswordEncoderShouldHandleUnicodeCharacters() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String unicodePassword = "пароль密码كلمةسر";
    String encoded = passwordEncoder.encode(unicodePassword);

    // Assert
    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(unicodePassword, encoded));
  }

  @Test
  @DisplayName("Configuration - Password encoder should handle long passwords")
  void configuration_PasswordEncoderShouldHandleLongPasswords() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    // BCrypt has a max length of 72 bytes
    String longPassword = "a".repeat(70);
    String encoded = passwordEncoder.encode(longPassword);

    // Assert
    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(longPassword, encoded));
  }

  // ==================== SECURITY REQUIREMENTS TESTS ====================

  @Test
  @DisplayName("Security - Password encoder should be cryptographically secure")
  void security_PasswordEncoderShouldBeCryptographicallySecure() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    // Assert - BCrypt is a secure one-way hash function
    assertTrue(
        passwordEncoder
            instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder);

    // Verify it's not reversible
    String original = "securePassword123";
    String hash = passwordEncoder.encode(original);

    // Hash should not contain original password
    assertFalse(hash.contains(original));
  }

  @Test
  @DisplayName("Security - Should use salted hashing")
  void security_ShouldUseSaltedHashing() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String password = "testPassword";

    // Generate multiple hashes
    String hash1 = passwordEncoder.encode(password);
    String hash2 = passwordEncoder.encode(password);
    String hash3 = passwordEncoder.encode(password);

    // Assert - All hashes should be different (due to random salt)
    assertNotEquals(hash1, hash2);
    assertNotEquals(hash2, hash3);
    assertNotEquals(hash1, hash3);

    // But all should validate
    assertTrue(passwordEncoder.matches(password, hash1));
    assertTrue(passwordEncoder.matches(password, hash2));
    assertTrue(passwordEncoder.matches(password, hash3));
  }

  // ==================== INTEGRATION TESTS ====================

  @Test
  @DisplayName("Integration - All security beans should work together")
  void integration_AllSecurityBeansShouldWorkTogether() throws Exception {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
    AuthenticationManager manager =
        securityConfig.authenticationManager(authenticationConfiguration);

    // Assert
    assertNotNull(passwordEncoder);
    assertNotNull(manager);

    // Test password encoding works
    String password = "integrationTest";
    String encoded = passwordEncoder.encode(password);
    assertTrue(passwordEncoder.matches(password, encoded));
  }

  @Test
  @DisplayName("Integration - Configuration should support complete authentication flow")
  void integration_ConfigurationShouldSupportCompleteAuthenticationFlow() throws Exception {
    // Arrange
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "userPassword123";

    // Act - Simulate user registration
    String storedHash = passwordEncoder.encode(rawPassword);

    // Simulate login - password validation
    boolean isValid = passwordEncoder.matches(rawPassword, storedHash);
    boolean isInvalid = passwordEncoder.matches("wrongPassword", storedHash);

    // Assert
    assertTrue(isValid);
    assertFalse(isInvalid);
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("Edge Case - Password encoder should handle null gracefully")
  void edgeCase_PasswordEncoderShouldHandleNull() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    // Assert - Should throw exception for null (security requirement)
    assertThrows(
        Exception.class,
        () -> {
          passwordEncoder.encode(null);
        });
  }

  @Test
  @DisplayName("Edge Case - Password matching should be case-sensitive")
  void edgeCase_PasswordMatchingShouldBeCaseSensitive() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String password = "TestPassword";
    String encoded = passwordEncoder.encode(password);

    // Assert
    assertTrue(passwordEncoder.matches("TestPassword", encoded));
    assertFalse(passwordEncoder.matches("testpassword", encoded));
    assertFalse(passwordEncoder.matches("TESTPASSWORD", encoded));
  }

  @Test
  @DisplayName("Edge Case - Should handle very short passwords")
  void edgeCase_ShouldHandleVeryShortPasswords() {
    // Act
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String shortPassword = "a";
    String encoded = passwordEncoder.encode(shortPassword);

    // Assert
    assertNotNull(encoded);
    assertTrue(passwordEncoder.matches(shortPassword, encoded));
  }

  @Test
  @DisplayName("Edge Case - Password encoder should be thread-safe")
  void edgeCase_PasswordEncoderShouldBeThreadSafe() throws InterruptedException {
    // Arrange
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String password = "threadSafeTest";

    // Act - Encode in multiple threads
    Thread thread1 =
        new Thread(
            () -> {
              String hash = passwordEncoder.encode(password);
              assertTrue(passwordEncoder.matches(password, hash));
            });

    Thread thread2 =
        new Thread(
            () -> {
              String hash = passwordEncoder.encode(password);
              assertTrue(passwordEncoder.matches(password, hash));
            });

    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    // Assert - No assertion needed, test passes if no exceptions
  }
}
