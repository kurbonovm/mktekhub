package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.AuthResponse;
import com.mktekhub.inventory.dto.LoginRequest;
import com.mktekhub.inventory.dto.SignupRequest;
import com.mktekhub.inventory.model.Role;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.repository.RoleRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.security.JwtUtil;
import com.mktekhub.inventory.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for authentication operations (signup and login).
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setIsActive(true);

        // Assign default role (VIEWER)
        Set<Role> roles = new HashSet<>();
        Role viewerRole = roleRepository.findByName("VIEWER")
                .orElseThrow(() -> new RuntimeException("Error: Role VIEWER not found."));
        roles.add(viewerRole);
        user.setRoles(roles);

        // Save user
        userRepository.save(user);

        // Authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signupRequest.getUsername(), signupRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roleNames = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), roleNames);
    }

    /**
     * Authenticate user and generate JWT token.
     */
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), roles);
    }
}
