package com.americanwomen.store.integration;

import com.americanwomen.store.controller.AuthController;
import com.americanwomen.store.dto.AuthRequest;
import com.americanwomen.store.dto.AuthResponse;
import com.americanwomen.store.dto.JwtResponse;
import com.americanwomen.store.dto.RegisterRequest;
import com.americanwomen.store.entity.User;
import com.americanwomen.store.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Auth module
 * 
 * Tests:
 * - Register new user (valid) → 200
 * - Register invalid user → 400 with OCL violations
 * - Login valid → 200 returns token
 * - Login invalid → 401
 */
@DisplayName("Auth Module Integration Tests")
class AuthModuleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Register new user with valid data → 200")
    void testRegister_ValidData_Returns200() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify user was created in database
        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("newuser@test.com", savedUser.getEmail());
        assertEquals(User.Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    @DisplayName("Register user with empty username → 400 with OCL violations")
    void testRegister_EmptyUsername_Returns400WithOclViolations() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(""); // Invalid: empty
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        try {
            authController.register(request);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Verify it's an OCL validation exception
            assertTrue(e.getMessage().contains("OCL validation failed") ||
                       e.getMessage().contains("usernameNotEmpty") ||
                       e.getCause() != null && e.getCause().getMessage().contains("usernameNotEmpty"));
        }
    }

    @Test
    @DisplayName("Register user with invalid email (no @) → 400 with OCL violations")
    void testRegister_InvalidEmail_Returns400WithOclViolations() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("invalidemail"); // Invalid: no @
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        try {
            authController.register(request);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Verify it's an OCL validation exception
            assertTrue(e.getMessage().contains("OCL validation failed") ||
                       e.getMessage().contains("emailContainsAt") ||
                       e.getCause() != null && e.getCause().getMessage().contains("emailContainsAt"));
        }
    }

    @Test
    @DisplayName("Register user with duplicate username → 400")
    void testRegister_DuplicateUsername_Returns400() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser"); // Already exists from setUp
        request.setEmail("different@test.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        try {
            authController.register(request);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Username already exists") ||
                       e.getMessage().contains("already exists"));
        }
    }

    @Test
    @DisplayName("Login with valid credentials → 200 returns token")
    void testLogin_ValidCredentials_Returns200WithToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        if (response.getBody() instanceof AuthResponse) {
            AuthResponse authResponse = (AuthResponse) response.getBody();
            assertNotNull(authResponse.getToken());
            assertNotNull(authResponse.getUser());
            assertEquals("testuser", authResponse.getUser().getUsername());
        } else if (response.getBody() instanceof JwtResponse) {
            JwtResponse jwtResponse = (JwtResponse) response.getBody();
            assertNotNull(jwtResponse.getToken());
        }
    }

    @Test
    @DisplayName("Login with invalid username → 401")
    void testLogin_InvalidUsername_Returns401() {
        AuthRequest request = new AuthRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        try {
            authController.login(request);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Should return 401 or authentication exception
            assertTrue(e.getMessage().contains("Bad credentials") ||
                       e.getMessage().contains("Invalid") ||
                       e.getMessage().contains("401"));
        }
    }

    @Test
    @DisplayName("Login with invalid password → 401")
    void testLogin_InvalidPassword_Returns401() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        try {
            authController.login(request);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Should return 401 or authentication exception
            assertTrue(e.getMessage().contains("Bad credentials") ||
                       e.getMessage().contains("Invalid") ||
                       e.getMessage().contains("401"));
        }
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200")
    void testLoginEndpoint_Returns200() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

