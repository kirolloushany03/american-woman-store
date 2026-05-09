package com.americanwomen.store.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Security
 * 
 * Tests:
 * - Any /api/admin/** without admin token → 403
 * - User token cannot access admin endpoints → 403
 * - Auth endpoints remain public
 */
@DisplayName("Security Integration Tests")
@AutoConfigureMockMvc
class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Admin endpoint without authentication → 403")
    void testAdminEndpoint_NoAuth_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoint with user role → 403")
    @WithMockUser(username = "testuser", roles = "USER")
    void testAdminEndpoint_UserRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoint with admin role → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminEndpoint_AdminRole_Returns200() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User endpoint without authentication → 401")
    void testUserEndpoint_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/orders/my"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("User endpoint with user role → 200")
    @WithMockUser(username = "testuser", roles = "USER")
    void testUserEndpoint_UserRole_Returns200() throws Exception {
        mockMvc.perform(get("/api/orders/my"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Auth register endpoint remains public → 200")
    void testAuthRegister_Public_Returns200() throws Exception {
        String requestBody = """
            {
                "username": "publicuser",
                "email": "public@test.com",
                "password": "password123",
                "firstName": "Public",
                "lastName": "User"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Auth login endpoint remains public → 200")
    void testAuthLogin_Public_Returns200() throws Exception {
        String requestBody = """
            {
                "username": "testuser",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isOk());
    }
}

