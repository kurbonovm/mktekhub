/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/** CORS configuration to allow frontend requests. */
@Configuration
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();

    // Allow credentials (cookies, authorization headers)
    config.setAllowCredentials(true);

    // Allow requests from these origins
    config.setAllowedOrigins(
        Arrays.asList("http://localhost:3000", "http://localhost:5173", "http://localhost:5174"));

    // Allow all HTTP methods
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // Allow all headers
    config.setAllowedHeaders(List.of("*"));

    // Expose these headers to the client
    config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

    // Cache preflight response for 1 hour
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }
}
