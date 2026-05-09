package com.americanwomen.store.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Clear any existing authentication first
        SecurityContextHolder.clearContext();
        
        String token = getTokenFromRequest(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            try {
                String username = tokenProvider.getUsernameFromToken(token);
                String role = tokenProvider.getRoleFromToken(token);
                
                // Ensure role has ROLE_ prefix for Spring Security (but don't double-prefix)
                String authority;
                if (role == null || role.isEmpty()) {
                    authority = "ROLE_USER"; // Default fallback
                } else if (role.startsWith("ROLE_")) {
                    authority = role; // Already has prefix
                } else {
                    authority = "ROLE_" + role; // Add prefix
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(new SimpleGrantedAuthority(authority)));

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Enhanced debug logging for ALL requests matching our endpoints
                String uri = request.getRequestURI();
                String method = request.getMethod();
                if (uri.contains("confirm-delivery") || uri.contains("return-request") || uri.contains("/admin/orders") || uri.contains("/admin/")) {
                    var authContext = SecurityContextHolder.getContext().getAuthentication();
                    System.out.println("[AUTH DEBUG] uri=" + uri + " method=" + method + " principal=" + (authContext != null ? authContext.getName() : "null"));
                    System.out.println("  Role from token (raw): " + role);
                    System.out.println("  Authority after prefix handling: " + authority);
                    System.out.println("  Authorities in SecurityContext: " + (authContext != null ? authContext.getAuthorities() : "null"));
                }
            } catch (Exception e) {
                System.err.println("[JWT Filter] Error processing token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Debug logging for failed authentication
            String uri = request.getRequestURI();
            if (uri.contains("confirm-delivery") || uri.contains("/admin/orders") || uri.contains("/admin/")) {
                System.out.println("[JWT Filter] ❌ Request without valid auth: " + request.getMethod() + " " + uri);
                System.out.println("  Token present: " + (token != null));
                if (token != null) {
                    try {
                        boolean valid = tokenProvider.validateToken(token);
                        System.out.println("  Token valid: " + valid);
                        if (!valid) {
                            System.out.println("  ❌ Token validation failed - token may be expired or invalid");
                        }
                    } catch (Exception e) {
                        System.out.println("  ❌ Token validation error: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("  ❌ No token in Authorization header");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}



