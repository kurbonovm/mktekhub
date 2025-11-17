package com.mktekhub.inventory.security;

import com.mktekhub.inventory.model.Role;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserDetailsServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;
    private Role viewerRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        viewerRole = new Role();
        viewerRole.setId(1L);
        viewerRole.setName("VIEWER");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setIsActive(true);

        Set<Role> roles = new HashSet<>();
        roles.add(viewerRole);
        user.setRoles(roles);
    }

    @Test
    @DisplayName("LoadUserByUsername - Should load user successfully")
    void loadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_VIEWER")));

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("LoadUserByUsername - Should throw UsernameNotFoundException when user not found")
    void loadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("User not found with username: nonexistent"));

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("LoadUserByUsername - Should handle multiple roles")
    void loadUserByUsername_MultipleRoles() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(viewerRole);
        roles.add(adminRole);
        user.setRoles(roles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_VIEWER")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("LoadUserByUsername - Should handle user with no roles")
    void loadUserByUsername_NoRoles() {
        // Arrange
        user.setRoles(new HashSet<>());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("LoadUserByUsername - Should preserve user details correctly")
    void loadUserByUsername_PreservesUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertTrue(userDetails instanceof UserDetailsImpl);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;

        assertEquals(1L, userDetailsImpl.getId());
        assertEquals("testuser", userDetailsImpl.getUsername());
        assertEquals("test@example.com", userDetailsImpl.getEmail());
        assertEquals("Test", userDetailsImpl.getFirstName());
        assertEquals("User", userDetailsImpl.getLastName());
    }
}
