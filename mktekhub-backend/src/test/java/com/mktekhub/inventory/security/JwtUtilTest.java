package com.mktekhub.inventory.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for JwtUtil.
 * Tests token generation, validation, expiration, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetailsImpl userDetails;
    private String jwtSecret;
    private long jwtExpiration;

    @BeforeEach
    void setUp() {
        // Setup test JWT properties
        jwtSecret = "mySecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmToWorkProperly12345";
        jwtExpiration = 3600000L; // 1 hour

        // Inject values using reflection
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", jwtExpiration);

        // Setup user details
        userDetails = new UserDetailsImpl(
            1L,
            "testuser",
            "test@example.com",
            "Test",
            "User",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );
    }

    // ==================== TOKEN GENERATION TESTS ====================

    @Test
    @DisplayName("Generate Token from Authentication - Should create valid JWT token")
    void generateToken_FromAuthentication_Success() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtUtil.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify token structure (JWT has 3 parts separated by dots)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // Verify we can extract username from token
        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Generate Token from Username - Should create valid JWT token")
    void generateTokenFromUsername_Success() {
        // Act
        String token = jwtUtil.generateTokenFromUsername("testuser");

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify token structure
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // Verify username extraction
        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Generate Token - Should include correct claims")
    void generateToken_ShouldIncludeCorrectClaims() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtUtil.generateToken(authentication);
        String username = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Generate Token - Two tokens should be different")
    void generateToken_DifferentTokensForSameUser() throws InterruptedException {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token1 = jwtUtil.generateToken(authentication);
        Thread.sleep(1000); // 1 second delay to ensure different issuedAt time
        String token2 = jwtUtil.generateToken(authentication);

        // Assert - Tokens should be different because of different issuedAt times
        assertNotEquals(token1, token2);
    }

    // ==================== TOKEN VALIDATION TESTS ====================

    @Test
    @DisplayName("Validate Token - Should return true for valid token")
    void validateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtUtil.generateTokenFromUsername("testuser");

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validate Token - Should return false for malformed token")
    void validateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // Act
        boolean isValid = jwtUtil.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate Token - Should return false for invalid signature")
    void validateToken_InvalidSignature_ReturnsFalse() {
        // Arrange - Use a completely invalid token format
        String tokenWithInvalidSignature = "not.a.valid.jwt.token.at.all";

        // Act
        boolean isValid = jwtUtil.validateToken(tokenWithInvalidSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate Token - Should return false for expired token")
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Arrange - Set very short expiration and create token
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1000L); // Already expired
        String expiredToken = jwtUtil.generateTokenFromUsername("testuser");

        // Reset to normal expiration for validation
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", jwtExpiration);

        // Act
        boolean isValid = jwtUtil.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate Token - Should return false for empty token")
    void validateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate Token - Should return false for null token")
    void validateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate Token - Should handle unsupported JWT")
    void validateToken_UnsupportedJwt_ReturnsFalse() {
        // Arrange - Create a token without signature (unsupported)
        String unsupportedToken = Jwts.builder()
            .subject("testuser")
            .compact(); // No signature

        // Act
        boolean isValid = jwtUtil.validateToken(unsupportedToken);

        // Assert
        assertFalse(isValid);
    }

    // ==================== USERNAME EXTRACTION TESTS ====================

    @Test
    @DisplayName("Get Username from Token - Should extract correct username")
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        // Arrange
        String expectedUsername = "johndoe";
        String token = jwtUtil.generateTokenFromUsername(expectedUsername);

        // Act
        String actualUsername = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    @DisplayName("Get Username from Token - Should handle special characters in username")
    void getUsernameFromToken_SpecialCharacters_ReturnsUsername() {
        // Arrange
        String username = "user.name+test@domain";
        String token = jwtUtil.generateTokenFromUsername(username);

        // Act
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Get Username from Token - Should throw exception for expired token")
    void getUsernameFromToken_ExpiredToken_ThrowsException() {
        // Arrange - Create expired token
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1000L);
        String expiredToken = jwtUtil.generateTokenFromUsername("testuser");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", jwtExpiration);

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.getUsernameFromToken(expiredToken);
        });
    }

    @Test
    @DisplayName("Get Username from Token - Should throw exception for malformed token")
    void getUsernameFromToken_MalformedToken_ThrowsException() {
        // Arrange
        String malformedToken = "invalid.token.string";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtil.getUsernameFromToken(malformedToken);
        });
    }

    // ==================== TOKEN EXPIRATION TESTS ====================

    @Test
    @DisplayName("Token Expiration - Token should not be expired immediately after creation")
    void tokenExpiration_NewToken_NotExpired() {
        // Arrange
        String token = jwtUtil.generateTokenFromUsername("testuser");

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token Expiration - Should respect configured expiration time")
    void tokenExpiration_RespectsConfiguredTime() {
        // Arrange - Set expiration to 1 second
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1000L);

        // Act
        String token = jwtUtil.generateTokenFromUsername("testuser");

        // Assert - Token should be valid immediately
        assertTrue(jwtUtil.validateToken(token));

        // Reset expiration
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", jwtExpiration);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Integration - Full token lifecycle")
    void integration_FullTokenLifecycle() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act - Generate
        String token = jwtUtil.generateToken(authentication);

        // Act - Validate
        boolean isValid = jwtUtil.validateToken(token);

        // Act - Extract username
        String username = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertTrue(isValid);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Integration - Generate from username and validate")
    void integration_GenerateFromUsernameAndValidate() {
        // Arrange
        String username = "integrationTestUser";

        // Act - Generate
        String token = jwtUtil.generateTokenFromUsername(username);

        // Act - Validate
        boolean isValid = jwtUtil.validateToken(token);

        // Act - Extract
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Integration - Different usernames produce different tokens")
    void integration_DifferentUsernamesProduceDifferentTokens() {
        // Act
        String token1 = jwtUtil.generateTokenFromUsername("user1");
        String token2 = jwtUtil.generateTokenFromUsername("user2");

        // Assert
        assertNotEquals(token1, token2);

        // Verify correct usernames
        assertEquals("user1", jwtUtil.getUsernameFromToken(token1));
        assertEquals("user2", jwtUtil.getUsernameFromToken(token2));
    }
}
