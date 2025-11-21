/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.dto.AuthResponse;
import com.mktekhub.inventory.dto.LoginRequest;
import com.mktekhub.inventory.dto.SignupRequest;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.Role;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.repository.RoleRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.security.JwtUtil;
import com.mktekhub.inventory.security.UserDetailsImpl;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Comprehensive unit tests for AuthService. Tests all methods for signup and login functionality
 * with edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private RoleRepository roleRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtUtil jwtUtil;

  @InjectMocks private AuthService authService;

  private SignupRequest signupRequest;
  private LoginRequest loginRequest;
  private Role viewerRole;
  private User user;

  @BeforeEach
  void setUp() {
    // Setup signup request
    signupRequest = new SignupRequest();
    signupRequest.setUsername("testuser");
    signupRequest.setEmail("test@example.com");
    signupRequest.setFirstName("Test");
    signupRequest.setLastName("User");
    signupRequest.setPassword("password123");

    // Setup login request
    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    // Setup role
    viewerRole = new Role();
    viewerRole.setId(1L);
    viewerRole.setName("VIEWER");
    viewerRole.setDescription("Viewer role");

    // Setup user
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setPassword("encodedPassword");
    user.setIsActive(true);
    user.setRoles(new HashSet<>(Collections.singletonList(viewerRole)));
  }

  // ==================== SIGNUP TESTS ====================

  @Test
  @DisplayName("Signup - Should successfully register a new user")
  void signup_Success() {
    // Arrange
    when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
    when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    String result = authService.signup(signupRequest);

    // Assert
    assertNotNull(result);
    assertEquals("User registered successfully! Please login to continue.", result);

    // Verify interactions
    verify(userRepository).existsByUsername(signupRequest.getUsername());
    verify(userRepository).existsByEmail(signupRequest.getEmail());
    verify(passwordEncoder).encode(signupRequest.getPassword());
    verify(roleRepository).findByName("VIEWER");

    // Capture and verify saved user
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertEquals(signupRequest.getUsername(), savedUser.getUsername());
    assertEquals(signupRequest.getEmail(), savedUser.getEmail());
    assertEquals(signupRequest.getFirstName(), savedUser.getFirstName());
    assertEquals(signupRequest.getLastName(), savedUser.getLastName());
    assertEquals("encodedPassword", savedUser.getPassword());
    assertTrue(savedUser.getIsActive());
    assertEquals(1, savedUser.getRoles().size());
    assertTrue(savedUser.getRoles().contains(viewerRole));
  }

  @Test
  @DisplayName("Signup - Should throw DuplicateResourceException when username already exists")
  void signup_DuplicateUsername() {
    // Arrange
    when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

    // Act & Assert
    DuplicateResourceException exception =
        assertThrows(DuplicateResourceException.class, () -> authService.signup(signupRequest));

    assertTrue(exception.getMessage().contains("username"));
    assertTrue(exception.getMessage().contains(signupRequest.getUsername()));

    // Verify interactions
    verify(userRepository).existsByUsername(signupRequest.getUsername());
    verify(userRepository, never()).existsByEmail(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup - Should throw DuplicateResourceException when email already exists")
  void signup_DuplicateEmail() {
    // Arrange
    when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

    // Act & Assert
    DuplicateResourceException exception =
        assertThrows(DuplicateResourceException.class, () -> authService.signup(signupRequest));

    assertTrue(exception.getMessage().contains("email"));
    assertTrue(exception.getMessage().contains(signupRequest.getEmail()));

    // Verify interactions
    verify(userRepository).existsByUsername(signupRequest.getUsername());
    verify(userRepository).existsByEmail(signupRequest.getEmail());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup - Should throw ResourceNotFoundException when VIEWER role not found")
  void signup_ViewerRoleNotFound() {
    // Arrange
    when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
    when(roleRepository.findByName("VIEWER")).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> authService.signup(signupRequest));

    assertTrue(exception.getMessage().contains("Role"));
    assertTrue(exception.getMessage().contains("VIEWER"));

    // Verify interactions
    verify(roleRepository).findByName("VIEWER");
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Signup - Should encode password correctly")
  void signup_PasswordEncoded() {
    // Arrange
    String rawPassword = "mySecurePassword123!";
    signupRequest.setPassword(rawPassword);
    when(userRepository.existsByUsername(any())).thenReturn(false);
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(passwordEncoder.encode(rawPassword)).thenReturn("superEncodedPassword");
    when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    authService.signup(signupRequest);

    // Assert
    verify(passwordEncoder).encode(rawPassword);
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertEquals("superEncodedPassword", userCaptor.getValue().getPassword());
  }

  // ==================== LOGIN TESTS ====================

  @Test
  @DisplayName("Login - Should successfully authenticate and return JWT token")
  void login_Success() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    UserDetailsImpl userDetails =
        new UserDetailsImpl(
            1L,
            "testuser",
            "test@example.com",
            "Test",
            "User",
            "encodedPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER")));

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtUtil.generateToken(authentication)).thenReturn("jwt-token-123");

    // Act
    AuthResponse response = authService.login(loginRequest);

    // Assert
    assertNotNull(response);
    assertEquals("jwt-token-123", response.getToken());
    assertEquals(1L, response.getId());
    assertEquals("testuser", response.getUsername());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("Test", response.getFirstName());
    assertEquals("User", response.getLastName());
    assertEquals(1, response.getRoles().size());
    assertTrue(response.getRoles().contains("VIEWER"));

    // Verify interactions
    ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
        ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
    verify(authenticationManager).authenticate(authCaptor.capture());
    assertEquals(loginRequest.getUsername(), authCaptor.getValue().getPrincipal());
    assertEquals(loginRequest.getPassword(), authCaptor.getValue().getCredentials());
    verify(jwtUtil).generateToken(authentication);
  }

  @Test
  @DisplayName("Login - Should handle multiple roles correctly")
  void login_MultipleRoles() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    UserDetailsImpl userDetails =
        new UserDetailsImpl(
            2L,
            "admin",
            "admin@example.com",
            "Admin",
            "User",
            "encodedPassword",
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_MANAGER"),
                new SimpleGrantedAuthority("ROLE_VIEWER")));

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtUtil.generateToken(authentication)).thenReturn("admin-jwt-token");

    // Act
    AuthResponse response = authService.login(loginRequest);

    // Assert
    assertNotNull(response);
    assertEquals(3, response.getRoles().size());
    assertTrue(response.getRoles().contains("ADMIN"));
    assertTrue(response.getRoles().contains("MANAGER"));
    assertTrue(response.getRoles().contains("VIEWER"));
  }

  @Test
  @DisplayName("Login - Should strip ROLE_ prefix from authorities")
  void login_StripRolePrefix() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    UserDetailsImpl userDetails =
        new UserDetailsImpl(
            1L,
            "testuser",
            "test@example.com",
            "Test",
            "User",
            "encodedPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_MANAGER")));

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtUtil.generateToken(authentication)).thenReturn("jwt-token");

    // Act
    AuthResponse response = authService.login(loginRequest);

    // Assert
    assertTrue(response.getRoles().contains("MANAGER"));
    assertFalse(response.getRoles().contains("ROLE_MANAGER"));
  }

  @Test
  @DisplayName("Login - Should throw exception for invalid credentials")
  void login_InvalidCredentials() {
    // Arrange
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(
            new org.springframework.security.authentication.BadCredentialsException(
                "Bad credentials"));

    // Act & Assert
    assertThrows(
        org.springframework.security.authentication.BadCredentialsException.class,
        () -> authService.login(loginRequest));

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtil, never()).generateToken(any());
  }
}
