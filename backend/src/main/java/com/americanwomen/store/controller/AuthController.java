package com.americanwomen.store.controller;

import com.americanwomen.store.dto.*;
import com.americanwomen.store.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // This will be handled by GlobalExceptionHandler and return 401
            throw e;
        } catch (Exception e) {
            // Log unexpected errors
            System.err.println("[AuthController] Unexpected error in login: " + e.getMessage());
            e.printStackTrace();
            // Re-throw as BadCredentialsException to return 401 instead of 500
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid username or password");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<com.americanwomen.store.dto.ProfileResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}

