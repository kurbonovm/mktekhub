/* Licensed under the Apache License, Version 2.0 */
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for authentication operations (signup and login). */
@Service
public class AuthService {

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtUtil jwtUtil;

  /** Register a new user. */
  @Transactional
  public String signup(SignupRequest signupRequest) {
    // Check if username exists
    if (userRepository.existsByUsername(signupRequest.getUsername())) {
      throw new DuplicateResourceException("User", "username", signupRequest.getUsername());
    }

    // Check if email exists
    if (userRepository.existsByEmail(signupRequest.getEmail())) {
      throw new DuplicateResourceException("User", "email", signupRequest.getEmail());
    }

    // Create new user
    User user = new User();
    user.setUsername(signupRequest.getUsername());
    user.setEmail(signupRequest.getEmail());
    user.setFirstName(signupRequest.getFirstName());
    user.setLastName(signupRequest.getLastName());
    user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
    user.setIsActive(true);

    // Assign default role (VIEWER)
    Set<Role> roles = new HashSet<>();
    Role viewerRole =
        roleRepository
            .findByName("VIEWER")
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "VIEWER"));
    roles.add(viewerRole);
    user.setRoles(roles);

    // Save user
    userRepository.save(user);

    return "User registered successfully! Please login to continue.";
  }

  /** Authenticate user and generate JWT token. */
  public AuthResponse login(LoginRequest loginRequest) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtil.generateToken(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    Set<String> roles =
        userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority().replace("ROLE_", ""))
            .collect(Collectors.toSet());

    return new AuthResponse(
        jwt,
        userDetails.getId(),
        userDetails.getUsername(),
        userDetails.getEmail(),
        userDetails.getFirstName(),
        userDetails.getLastName(),
        roles);
  }
}
