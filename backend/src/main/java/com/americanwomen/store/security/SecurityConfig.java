package com.americanwomen.store.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Public endpoints (no auth required)
                .requestMatchers("/api/auth/**", "/api/products/**", "/api/contact", "/api/config", "/api/fix/**").permitAll()
                
                // 2. User-accessible order endpoints (MUST come BEFORE admin rules to avoid conflicts)
                // These are POST endpoints for authenticated users (ownership validated in service layer)
                // Using simple path pattern that matches any order ID
                .requestMatchers("/api/orders/*/confirm-delivery").authenticated()
                .requestMatchers("/api/orders/*/return-request").authenticated()
                
                // 3. Admin-only endpoints (explicit method matching for Spring Security 6)
                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                // Admin-only order management endpoints (PUT methods)
                .requestMatchers(HttpMethod.PUT, "/api/orders/*/confirm").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/orders/*/ship").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/orders/*/cancel").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/orders/*/complete").hasRole("ADMIN")
                
                // 4. Other authenticated endpoints (work for both ROLE_USER and ROLE_ADMIN)
                .requestMatchers("/api/cart/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/orders/checkout").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/orders/my").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                
                // 5. Default: permit all other requests
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                // Handle 401 (not authenticated)
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Unauthorized - please login\"}");
                })
                // Handle 403 (authenticated but not authorized)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String uri = request.getRequestURI();
                    String method = request.getMethod();
                    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    
                    log.warn("[DENIED] {} {} principal={} authorities={}", method, uri, 
                        (auth != null ? auth.getName() : "null"), 
                        (auth != null ? auth.getAuthorities() : "null"));
                    log.warn("  Exception class: {}", accessDeniedException.getClass().getName());
                    log.warn("  Exception message: {}", accessDeniedException.getMessage());
                    if (accessDeniedException.getCause() != null) {
                        log.warn("  Cause: {}", accessDeniedException.getCause().getMessage());
                    }
                    
                    // Determine which rule likely caused the denial
                    String deniedReason = "Access denied - insufficient privileges";
                    if (uri.contains("/admin/")) {
                        deniedReason = "Admin access required (hasRole('ADMIN') not satisfied)";
                    } else if (uri.contains("confirm-delivery") || uri.contains("return-request")) {
                        deniedReason = "User endpoint denied - check SecurityConfig matcher order and method matching";
                    }
                    
                    String message = "Access denied: " + deniedReason;
                    if (uri.contains("/admin/")) {
                        message = "Admin access required";
                    } else if (uri.contains("confirm-delivery") || uri.contains("return-request")) {
                        message = "Access denied - check authorization configuration";
                    }
                    
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"" + message + "\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

