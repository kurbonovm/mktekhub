/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.config;

import com.mktekhub.inventory.security.JwtAuthenticationFilter;
import com.mktekhub.inventory.security.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Test configuration to provide mock security beans for controller tests. This prevents Spring from
 * trying to autowire real security components during @WebMvcTest.
 *
 * <p>Note: @MockBean is deprecated in Spring Boot 3.4.0 but remains the standard approach for
 * mocking beans in Spring test contexts. The Spring team recommends continuing to use it until a
 * suitable replacement is provided in a future release.
 */
@TestConfiguration
public class TestSecurityConfig {

  @MockBean private JwtUtil jwtUtil;

  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean private UserDetailsService userDetailsService;
}
