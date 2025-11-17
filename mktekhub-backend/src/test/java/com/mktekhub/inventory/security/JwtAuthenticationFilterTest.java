package com.mktekhub.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JwtAuthenticationFilter.
 * Tests JWT token extraction, validation, and authentication setup.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();

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

    // ==================== VALID TOKEN TESTS ====================

    @Test
    @DisplayName("Filter with valid JWT token - Should set authentication in context")
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).getUsernameFromToken(token);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(filterChain).doFilter(request, response);

        // Verify authentication was set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("testuser", authentication.getName());
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    @DisplayName("Filter with valid token - Should include user authorities")
    void doFilterInternal_ValidToken_IncludesAuthorities() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertNotNull(authentication.getAuthorities());
        assertEquals(1, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_VIEWER")));
    }

    @Test
    @DisplayName("Filter with valid token - Should set authentication details")
    void doFilterInternal_ValidToken_SetsAuthenticationDetails() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertNotNull(authentication.getDetails());
    }

    // ==================== NO TOKEN TESTS ====================

    @Test
    @DisplayName("Filter without Authorization header - Should not set authentication")
    void doFilterInternal_NoAuthorizationHeader_NoAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);

        // Verify no authentication was set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Filter with empty Authorization header - Should not set authentication")
    void doFilterInternal_EmptyAuthorizationHeader_NoAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    // ==================== INVALID TOKEN FORMAT TESTS ====================

    @Test
    @DisplayName("Filter with token without 'Bearer ' prefix - Should not set authentication")
    void doFilterInternal_TokenWithoutBearerPrefix_NoAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("invalid-token-format");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Filter with malformed Bearer token - Should not set authentication")
    void doFilterInternal_MalformedBearerToken_NoAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Filter with Bearer token with only space - Should not set authentication")
    void doFilterInternal_BearerTokenOnlySpace_NoAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    // ==================== INVALID TOKEN TESTS ====================

    @Test
    @DisplayName("Filter with invalid JWT token - Should not set authentication")
    void doFilterInternal_InvalidToken_NoAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "invalid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Filter with expired JWT token - Should not set authentication")
    void doFilterInternal_ExpiredToken_NoAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "expired-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil).validateToken(token);
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    // ==================== EXCEPTION HANDLING TESTS ====================

    @Test
    @DisplayName("Filter when JWT validation throws exception - Should continue filter chain")
    void doFilterInternal_JwtUtilThrowsException_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        String token = "problematic-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("JWT processing error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Filter when UserDetailsService throws exception - Should continue filter chain")
    void doFilterInternal_UserDetailsServiceThrowsException_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("nonexistent");
        when(userDetailsService.loadUserByUsername("nonexistent"))
            .thenThrow(new RuntimeException("User not found"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Filter when getting username throws exception - Should continue filter chain")
    void doFilterInternal_GetUsernameThrowsException_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenThrow(new RuntimeException("Cannot extract username"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Filter with case-sensitive Bearer prefix - Should handle correctly")
    void doFilterInternal_CaseSensitiveBearerPrefix_HandlesCorrectly() throws ServletException, IOException {
        // Arrange - "bearer" in lowercase should NOT work (case-sensitive)
        when(request.getHeader("Authorization")).thenReturn("bearer valid-token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("Filter with extra spaces in Bearer token - Should extract token correctly")
    void doFilterInternal_ExtraSpacesInBearer_ExtractsTokenCorrectly() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer  " + token); // Extra space
        when(jwtUtil.validateToken(" " + token)).thenReturn(false); // Should fail with extra space

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter always calls filterChain.doFilter - Ensures request continues")
    void doFilterInternal_AlwaysCallsDoFilter() throws ServletException, IOException {
        // Test with no token
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);

        // Reset and test with valid token
        reset(filterChain, request);
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Integration - Complete authentication flow with valid token")
    void integration_CompleteAuthenticationFlow() throws ServletException, IOException {
        // Arrange
        String token = "complete-valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Verify complete flow
        verify(request).getHeader("Authorization");
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).getUsernameFromToken(token);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(filterChain).doFilter(request, response);

        // Verify security context
        SecurityContext context = SecurityContextHolder.getContext();
        assertNotNull(context);

        Authentication authentication = context.getAuthentication();
        assertNotNull(authentication);
        assertEquals("testuser", authentication.getName());
        assertEquals(userDetails, authentication.getPrincipal());
        assertNotNull(authentication.getAuthorities());
    }

    @Test
    @DisplayName("Integration - Multiple filter calls should be independent")
    void integration_MultipleFilterCallsIndependent() throws ServletException, IOException {
        // First request with valid token
        String token1 = "token1";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token1);
        when(jwtUtil.validateToken(token1)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token1)).thenReturn("user1");

        UserDetails user1Details = new UserDetailsImpl(1L, "user1", "user1@test.com",
            "User", "One", "pass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER")));
        when(userDetailsService.loadUserByUsername("user1")).thenReturn(user1Details);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth1 = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("user1", auth1.getName());

        // Clear context
        SecurityContextHolder.clearContext();

        // Second request with different token
        reset(request);
        String token2 = "token2";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token2);
        when(jwtUtil.validateToken(token2)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token2)).thenReturn("user2");

        UserDetails user2Details = new UserDetailsImpl(2L, "user2", "user2@test.com",
            "User", "Two", "pass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userDetailsService.loadUserByUsername("user2")).thenReturn(user2Details);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth2 = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("user2", auth2.getName());
    }
}
