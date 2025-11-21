/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.config;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Comprehensive unit tests for CorsConfig. Tests CORS filter configuration for frontend requests.
 */
@DisplayName("CorsConfig Tests")
class CorsConfigTest {

  private CorsConfig corsConfig;

  @BeforeEach
  void setUp() {
    corsConfig = new CorsConfig();
  }

  // ==================== CORS FILTER BEAN TESTS ====================

  @Test
  @DisplayName("CorsFilter Bean - Should be created successfully")
  void corsFilter_ShouldBeCreated() {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();

    // Assert
    assertNotNull(corsFilter);
  }

  @Test
  @DisplayName("CorsFilter Bean - Should be instance of CorsFilter")
  void corsFilter_ShouldBeInstanceOfCorsFilter() {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();

    // Assert
    assertTrue(corsFilter instanceof CorsFilter);
  }

  // ==================== CORS CONFIGURATION TESTS ====================

  @Test
  @DisplayName("CORS Configuration - Should allow credentials")
  void corsConfiguration_ShouldAllowCredentials() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config);
    assertTrue(config.getAllowCredentials());
  }

  @Test
  @DisplayName("CORS Configuration - Should allow localhost:3000")
  void corsConfiguration_ShouldAllowLocalhost3000() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedOrigins());
    assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
  }

  @Test
  @DisplayName("CORS Configuration - Should allow localhost:5173")
  void corsConfiguration_ShouldAllowLocalhost5173() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedOrigins());
    assertTrue(config.getAllowedOrigins().contains("http://localhost:5173"));
  }

  @Test
  @DisplayName("CORS Configuration - Should allow localhost:5174")
  void corsConfiguration_ShouldAllowLocalhost5174() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedOrigins());
    assertTrue(config.getAllowedOrigins().contains("http://localhost:5174"));
  }

  @Test
  @DisplayName("CORS Configuration - Should have exactly 3 allowed origins")
  void corsConfiguration_ShouldHaveThreeAllowedOrigins() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedOrigins());
    assertEquals(3, config.getAllowedOrigins().size());
  }

  // ==================== ALLOWED METHODS TESTS ====================

  @Test
  @DisplayName("CORS Configuration - Should allow GET method")
  void corsConfiguration_ShouldAllowGetMethod() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedMethods());
    assertTrue(config.getAllowedMethods().contains("GET"));
  }

  @Test
  @DisplayName("CORS Configuration - Should allow POST method")
  void corsConfiguration_ShouldAllowPostMethod() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertTrue(config.getAllowedMethods().contains("POST"));
  }

  @Test
  @DisplayName("CORS Configuration - Should allow PUT method")
  void corsConfiguration_ShouldAllowPutMethod() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertTrue(config.getAllowedMethods().contains("PUT"));
  }

  @Test
  @DisplayName("CORS Configuration - Should allow PATCH method")
  void corsConfiguration_ShouldAllowPatchMethod() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertTrue(config.getAllowedMethods().contains("PATCH"));
  }

  @Test
  @DisplayName("CORS Configuration - Should allow DELETE method")
  void corsConfiguration_ShouldAllowDeleteMethod() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertTrue(config.getAllowedMethods().contains("DELETE"));
  }

  @Test
  @DisplayName("CORS Configuration - Should allow OPTIONS method")
  void corsConfiguration_ShouldAllowOptionsMethod() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertTrue(config.getAllowedMethods().contains("OPTIONS"));
  }

  @Test
  @DisplayName("CORS Configuration - Should have exactly 6 allowed methods")
  void corsConfiguration_ShouldHaveSixAllowedMethods() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedMethods());
    assertEquals(6, config.getAllowedMethods().size());
  }

  @Test
  @DisplayName("CORS Configuration - Should allow all HTTP methods needed for REST API")
  void corsConfiguration_ShouldAllowAllRestfulMethods() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedMethods());
    assertTrue(
        config
            .getAllowedMethods()
            .containsAll(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
  }

  // ==================== ALLOWED HEADERS TESTS ====================

  @Test
  @DisplayName("CORS Configuration - Should allow all headers")
  void corsConfiguration_ShouldAllowAllHeaders() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getAllowedHeaders());
    assertEquals(1, config.getAllowedHeaders().size());
    assertTrue(config.getAllowedHeaders().contains("*"));
  }

  // ==================== EXPOSED HEADERS TESTS ====================

  @Test
  @DisplayName("CORS Configuration - Should expose Authorization header")
  void corsConfiguration_ShouldExposeAuthorizationHeader() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getExposedHeaders());
    assertTrue(config.getExposedHeaders().contains("Authorization"));
  }

  @Test
  @DisplayName("CORS Configuration - Should expose Content-Type header")
  void corsConfiguration_ShouldExposeContentTypeHeader() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getExposedHeaders());
    assertTrue(config.getExposedHeaders().contains("Content-Type"));
  }

  @Test
  @DisplayName("CORS Configuration - Should have exactly 2 exposed headers")
  void corsConfiguration_ShouldHaveTwoExposedHeaders() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertEquals(2, config.getExposedHeaders().size());
  }

  // ==================== MAX AGE TESTS ====================

  @Test
  @DisplayName("CORS Configuration - Should set max age to 1 hour (3600 seconds)")
  void corsConfiguration_ShouldSetMaxAgeTo3600() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert
    assertNotNull(config.getMaxAge());
    assertEquals(3600L, config.getMaxAge());
  }

  // ==================== PATH MAPPING TESTS ====================

  @Test
  @DisplayName("CORS Configuration - Should apply to all paths")
  void corsConfiguration_ShouldApplyToAllPaths() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    UrlBasedCorsConfigurationSource source = extractConfigurationSource(corsFilter);

    // Assert
    assertNotNull(source);

    // Create a mock request to test path mapping
    org.springframework.mock.web.MockHttpServletRequest mockRequest =
        new org.springframework.mock.web.MockHttpServletRequest();
    mockRequest.setRequestURI("/api/test");

    CorsConfiguration config = source.getCorsConfiguration(mockRequest);
    assertNotNull(config);
  }

  // ==================== INTEGRATION TESTS ====================

  @Test
  @DisplayName("Integration - Complete CORS configuration validation")
  void integration_CompleteCorsConfiguration() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert - Verify all configuration aspects
    assertNotNull(config);

    // Credentials
    assertTrue(config.getAllowCredentials());

    // Origins
    assertEquals(3, config.getAllowedOrigins().size());
    assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
    assertTrue(config.getAllowedOrigins().contains("http://localhost:5173"));
    assertTrue(config.getAllowedOrigins().contains("http://localhost:5174"));

    // Methods
    assertEquals(6, config.getAllowedMethods().size());
    assertTrue(
        config
            .getAllowedMethods()
            .containsAll(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));

    // Headers
    assertTrue(config.getAllowedHeaders().contains("*"));

    // Exposed Headers
    assertEquals(2, config.getExposedHeaders().size());
    assertTrue(config.getExposedHeaders().contains("Authorization"));
    assertTrue(config.getExposedHeaders().contains("Content-Type"));

    // Max Age
    assertEquals(3600L, config.getMaxAge());
  }

  @Test
  @DisplayName("Integration - CORS filter should be production-ready")
  void integration_CorsFilterProductionReady() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert - Verify security and functionality
    assertNotNull(corsFilter);
    assertNotNull(config);

    // Security: Credentials enabled for authentication
    assertTrue(config.getAllowCredentials());

    // Security: Specific origins only (not wildcard)
    assertNotNull(config.getAllowedOrigins());
    assertFalse(config.getAllowedOrigins().contains("*"));

    // Functionality: All RESTful methods supported
    assertTrue(
        config.getAllowedMethods().containsAll(Arrays.asList("GET", "POST", "PUT", "DELETE")));

    // Functionality: Authorization header exposed for JWT
    assertTrue(config.getExposedHeaders().contains("Authorization"));

    // Performance: Preflight cache enabled
    assertNotNull(config.getMaxAge());
    assertTrue(config.getMaxAge() > 0);
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("Edge Case - Should not allow wildcard origin when credentials are enabled")
  void edgeCase_NoWildcardOriginWithCredentials() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert - When allowCredentials is true, allowedOrigins cannot be "*"
    assertTrue(config.getAllowCredentials());
    assertFalse(config.getAllowedOrigins().contains("*"));
  }

  @Test
  @DisplayName("Edge Case - Configuration should support both React dev servers")
  void edgeCase_SupportsMultipleReactDevServers() throws Exception {
    // Act
    CorsFilter corsFilter = corsConfig.corsFilter();
    CorsConfiguration config = extractCorsConfiguration(corsFilter);

    // Assert - Should support both Vite (5173/5174) and CRA (3000) default ports
    assertTrue(config.getAllowedOrigins().contains("http://localhost:3000")); // Create React App
    assertTrue(config.getAllowedOrigins().contains("http://localhost:5173")); // Vite default
    assertTrue(config.getAllowedOrigins().contains("http://localhost:5174")); // Vite alternate
  }

  // ==================== HELPER METHODS ====================

  /**
   * Extracts CorsConfiguration from CorsFilter using reflection. This is necessary because Spring's
   * CorsFilter doesn't expose its configuration directly.
   */
  private CorsConfiguration extractCorsConfiguration(CorsFilter corsFilter) throws Exception {
    Field configSourceField = CorsFilter.class.getDeclaredField("configSource");
    configSourceField.setAccessible(true);
    UrlBasedCorsConfigurationSource source =
        (UrlBasedCorsConfigurationSource) configSourceField.get(corsFilter);

    // Get configuration for the root path "/**"
    return source.getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());
  }

  /** Extracts UrlBasedCorsConfigurationSource from CorsFilter using reflection. */
  private UrlBasedCorsConfigurationSource extractConfigurationSource(CorsFilter corsFilter)
      throws Exception {
    Field configSourceField = CorsFilter.class.getDeclaredField("configSource");
    configSourceField.setAccessible(true);
    return (UrlBasedCorsConfigurationSource) configSourceField.get(corsFilter);
  }
}
