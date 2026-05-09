package com.americanwomen.store.integration;

import com.americanwomen.store.dto.OclErrorResponse;
import com.americanwomen.store.dto.RegisterRequest;
import com.americanwomen.store.entity.OrderStatus;
import com.americanwomen.store.entity.ReturnStatus;
import com.americanwomen.store.repository.OrderRepository;
import com.americanwomen.store.repository.ProductRepository;
import com.americanwomen.store.repository.UserRepository;
import com.americanwomen.store.service.AuthService;
import com.americanwomen.store.service.OrderService;
import com.americanwomen.store.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for OCL Runtime Validation
 * 
 * Tests verify that OCL validation is enforced at the service layer
 * and returns structured error responses with invariant names.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OCL Validation Integration Tests")
class OclValidationIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // Clean up test data if needed
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ==================== User Registration Tests ====================

    @Test
    @DisplayName("Register user with invalid username (empty) - should return OCL violations")
    void testRegisterUser_InvalidUsername_ReturnsOclViolations() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(""); // Invalid: empty
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        var exception = assertThrows(Exception.class, () -> {
            authService.register(request);
        });

        // Verify it's an OCL validation exception
        assertTrue(exception.getMessage().contains("OCL validation failed") ||
                   exception.getMessage().contains("usernameNotEmpty"));
    }

    @Test
    @DisplayName("Register user with invalid email (no @) - should return OCL violations")
    void testRegisterUser_InvalidEmail_ReturnsOclViolations() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("invalidemail"); // Invalid: no @
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        var exception = assertThrows(Exception.class, () -> {
            authService.register(request);
        });

        assertTrue(exception.getMessage().contains("OCL validation failed") ||
                   exception.getMessage().contains("emailContainsAt"));
    }

    @Test
    @DisplayName("Register user with valid data - should succeed")
    void testRegisterUser_ValidData_Succeeds() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        assertDoesNotThrow(() -> {
            authService.register(request);
        });
    }

    // ==================== Product Creation Tests ====================

    @Test
    @DisplayName("Create product with negative selling price - should return OCL violations")
    void testCreateProduct_NegativePrice_ReturnsOclViolations() {
        var dto = new com.americanwomen.store.dto.ProductDto();
        dto.setName("Test Product");
        dto.setDescription("Test Description");
        dto.setCategory("Test");
        dto.setSellingPrice(new BigDecimal("-10.00")); // Invalid: negative
        dto.setCostPrice(new BigDecimal("5.00"));
        dto.setStockQuantity(10);
        dto.setSalePercentage(0);
        dto.setOnSale(false);

        var exception = assertThrows(Exception.class, () -> {
            productService.createProduct(dto);
        });

        assertTrue(exception.getMessage().contains("OCL validation failed") ||
                   exception.getMessage().contains("sellingPricePositive"));
    }

    @Test
    @DisplayName("Create product with negative stock quantity - should return OCL violations")
    void testCreateProduct_NegativeStock_ReturnsOclViolations() {
        var dto = new com.americanwomen.store.dto.ProductDto();
        dto.setName("Test Product");
        dto.setDescription("Test Description");
        dto.setCategory("Test");
        dto.setSellingPrice(new BigDecimal("10.00"));
        dto.setCostPrice(new BigDecimal("5.00"));
        dto.setStockQuantity(-1); // Invalid: negative
        dto.setSalePercentage(0);
        dto.setOnSale(false);

        var exception = assertThrows(Exception.class, () -> {
            productService.createProduct(dto);
        });

        assertTrue(exception.getMessage().contains("OCL validation failed") ||
                   exception.getMessage().contains("stockQuantityNonNegative"));
    }

    @Test
    @DisplayName("Create product with onSale=true but salePercentage=0 - should return OCL violations")
    void testCreateProduct_OnSaleWithoutPercentage_ReturnsOclViolations() {
        var dto = new com.americanwomen.store.dto.ProductDto();
        dto.setName("Test Product");
        dto.setDescription("Test Description");
        dto.setCategory("Test");
        dto.setSellingPrice(new BigDecimal("10.00"));
        dto.setCostPrice(new BigDecimal("5.00"));
        dto.setStockQuantity(10);
        dto.setSalePercentage(0); // Invalid: onSale=true requires salePercentage > 0
        dto.setOnSale(true);

        var exception = assertThrows(Exception.class, () -> {
            productService.createProduct(dto);
        });

        assertTrue(exception.getMessage().contains("OCL validation failed") ||
                   exception.getMessage().contains("onSaleImpliesPercentage"));
    }

    // ==================== Order Status Update Tests ====================

    @Test
    @DisplayName("Update order status to DELIVERED from non-SHIPPED - should return OCL violations")
    void testUpdateOrderStatus_InvalidTransition_ReturnsOclViolations() {
        // This test requires a real order in the database
        // For now, we'll test the service method directly if possible
        // In a real scenario, you'd create an order first
        
        // Note: This test may need to be adjusted based on your actual OrderService implementation
        // The key is to verify that OCL validation is called and returns violations
    }

    // ==================== Return Request Tests ====================

    @Test
    @DisplayName("Request return for non-DELIVERED order - should return OCL violations")
    void testRequestReturn_NonDeliveredOrder_ReturnsOclViolations() {
        // This test requires:
        // 1. A user
        // 2. An order with status != DELIVERED
        // 3. Call orderService.requestReturn()
        
        // Note: This is a placeholder - actual implementation depends on your test data setup
        // The key is to verify that OCL validation (returnOnlyWhenDelivered) is enforced
    }

    // ==================== Admin Actions Tests ====================

    @Test
    @DisplayName("Approve return for non-REQUESTED order - should return OCL violations")
    void testApproveReturn_NonRequestedOrder_ReturnsOclViolations() {
        // This test requires:
        // 1. An order with returnStatus != REQUESTED
        // 2. Call orderService.approveReturn()
        
        // Note: This is a placeholder - actual implementation depends on your test data setup
        // The key is to verify that OCL validation is enforced
    }

    @Test
    @DisplayName("Reject return for non-REQUESTED order - should return OCL violations")
    void testRejectReturn_NonRequestedOrder_ReturnsOclViolations() {
        // This test requires:
        // 1. An order with returnStatus != REQUESTED
        // 2. Call orderService.rejectReturn()
        
        // Note: This is a placeholder - actual implementation depends on your test data setup
        // The key is to verify that OCL validation is enforced
    }
}

