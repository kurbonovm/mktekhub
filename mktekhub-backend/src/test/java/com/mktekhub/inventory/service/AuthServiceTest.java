package com.mktekhub.inventory.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role viewerRole;

    @BeforeEach
    void setUp() {
        // Setup signup request
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Setup role
        viewerRole = new Role();
        viewerRole.setId(1L);
        viewerRole.setName("VIEWER");

        // Setup user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("encodedPassword");
        user.setIsActive(true);
        user.setRoles(Set.of(viewerRole));
    }

    @Test
    void signup_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        String result = authService.signup(signupRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("registered successfully"));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("VIEWER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_ThrowsException_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> authService.signup(signupRequest));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_ThrowsException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> authService.signup(signupRequest));
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_ThrowsException_WhenViewerRoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.signup(signupRequest));
        verify(roleRepository).findByName("VIEWER");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "test@example.com",
                "Test",
                "User",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(authentication)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertTrue(response.getRoles().contains("VIEWER"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(authentication);
    }

    @Test
    void signup_SetsUserFieldsCorrectly() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Capture the saved user
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);

            // Verify all fields are set correctly
            assertEquals("testuser", savedUser.getUsername());
            assertEquals("test@example.com", savedUser.getEmail());
            assertEquals("Test", savedUser.getFirstName());
            assertEquals("User", savedUser.getLastName());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertTrue(savedUser.getIsActive());
            assertTrue(savedUser.getRoles().contains(viewerRole));

            return savedUser;
        });

        // Act
        authService.signup(signupRequest);

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ExtractsRolesWithoutRolePrefix() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "test@example.com",
                "Test",
                "User",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(authentication)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        // Verify that "ROLE_" prefix is removed from roles
        assertTrue(response.getRoles().contains("VIEWER"));
        assertFalse(response.getRoles().contains("ROLE_VIEWER"));
    }
}
