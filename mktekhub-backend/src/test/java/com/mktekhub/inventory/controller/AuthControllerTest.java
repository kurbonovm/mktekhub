/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.dto.AuthResponse;
import com.mktekhub.inventory.dto.LoginRequest;
import com.mktekhub.inventory.dto.SignupRequest;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.service.AuthService;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

/** Unit tests for AuthController. */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;

  private SignupRequest signupRequest;
  private LoginRequest loginRequest;
  private AuthResponse authResponse;

  @BeforeEach
  void setUp() {
    signupRequest = new SignupRequest();
    signupRequest.setUsername("testuser");
    signupRequest.setEmail("test@example.com");
    signupRequest.setPassword("Password123!");
    signupRequest.setFirstName("Test");
    signupRequest.setLastName("User");

    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("Password123!");

    Set<String> roles = new HashSet<>();
    roles.add("ROLE_VIEWER");
    authResponse =
        new AuthResponse("jwt-token", 1L, "testuser", "test@example.com", "Test", "User", roles);
  }

  @Test
  @DisplayName("POST /api/auth/signup - Success")
  void signup_Success() throws Exception {
    when(authService.signup(any(SignupRequest.class)))
        .thenReturn("User registered successfully! Please login to continue.");

    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.message").value("User registered successfully! Please login to continue."));

    verify(authService).signup(any(SignupRequest.class));
  }

  @Test
  @DisplayName("POST /api/auth/signup - Duplicate username")
  void signup_DuplicateUsername() throws Exception {
    when(authService.signup(any(SignupRequest.class)))
        .thenThrow(new DuplicateResourceException("User", "username", "testuser"));

    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());

    verify(authService).signup(any(SignupRequest.class));
  }

  @Test
  @DisplayName("POST /api/auth/login - Success")
  void login_Success() throws Exception {
    when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.username").value("testuser"));

    verify(authService).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("POST /api/auth/login - Invalid credentials")
  void login_InvalidCredentials() throws Exception {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new BadCredentialsException("Invalid"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid username or password"));

    verify(authService).login(any(LoginRequest.class));
  }
}
