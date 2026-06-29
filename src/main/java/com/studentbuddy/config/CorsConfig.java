package com.studentbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS for local development. The frontend is now a standalone set of static
 * files (served from a different origin than the API — e.g. VS Code Live Server
 * on :5500, or `python -m http.server` on :8000), so the browser needs the API
 * to allow cross-origin requests.
 *
 * Because authentication uses a Bearer token in the Authorization header (not
 * cookies), credentials are not required, and allowing any localhost origin is
 * safe and convenient for development. Tighten allowedOrigins for production.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Any origin is acceptable in dev since we don't use cookies.
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
