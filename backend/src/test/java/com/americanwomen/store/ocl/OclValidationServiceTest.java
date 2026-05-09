package com.americanwomen.store.ocl;

import com.americanwomen.store.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for OCL Runtime Validation Service
 * 
 * Tests verify that all invariants defined in store.ocl are properly enforced.
 * Each test corresponds to a specific invariant and verifies both positive and negative cases.
 */
@DisplayName("OCL Validation Service Tests")
class OclValidationServiceTest {

    private OclValidationService oclValidationService;

    @BeforeEach
    void setUp() {
        oclValidationService = new OclValidationService();
        // Manually initialize (normally done by @PostConstruct)
        try {
            oclValidationService.init();
        } catch (Exception e) {
            // If OCL files don't exist in test, skip initialization
            // In real scenario, this would fail fast
        }
    }

    // ==================== User Invariants ====================

    @Test
    @DisplayName("User: usernameNotEmpty - should pass when username is not empty")
    void testUsernameNotEmpty_Pass() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.Role.ROLE_USER);

        assertDoesNotThrow(() -> oclValidationService.validateUser(user));
    }

    @Test
    @DisplayName("User: usernameNotEmpty - should fail when username is null")
    void testUsernameNotEmpty_Fail_Null() {
        User user = new User();
        user.setUsername(null);
        user.setEmail("test@example.com");
        user.setRole(User.Role.ROLE_USER);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateUser(user));
        
        assertHasInvariant(ex, "usernameNotEmpty");
    }

    @Test
    @DisplayName("User: usernameNotEmpty - should fail when username is empty")
    void testUsernameNotEmpty_Fail_Empty() {
        User user = new User();
        user.setUsername("");
        user.setEmail("test@example.com");
        user.setRole(User.Role.ROLE_USER);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateUser(user));
        
        assertHasInvariant(ex, "usernameNotEmpty");
    }

    @Test
    @DisplayName("User: emailContainsAt - should pass when email contains @")
    void testEmailContainsAt_Pass() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.Role.ROLE_USER);

        assertDoesNotThrow(() -> oclValidationService.validateUser(user));
    }

    @Test
    @DisplayName("User: emailContainsAt - should fail when email does not contain @")
    void testEmailContainsAt_Fail() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("invalidemail");
        user.setRole(User.Role.ROLE_USER);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateUser(user));
        
        assertHasInvariant(ex, "emailContainsAt");
    }

    @Test
    @DisplayName("User: roleValid - should pass when role is ROLE_USER")
    void testRoleValid_Pass_User() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.Role.ROLE_USER);

        assertDoesNotThrow(() -> oclValidationService.validateUser(user));
    }

    @Test
    @DisplayName("User: roleValid - should pass when role is ROLE_ADMIN")
    void testRoleValid_Pass_Admin() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.Role.ROLE_ADMIN);

        assertDoesNotThrow(() -> oclValidationService.validateUser(user));
    }

    @Test
    @DisplayName("User: roleValid - should fail when role is null")
    void testRoleValid_Fail_Null() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(null);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateUser(user));
        
        assertHasInvariant(ex, "roleValid");
    }

    // ==================== Product Invariants ====================

    @Test
    @DisplayName("Product: sellingPricePositive - should pass when price > 0")
    void testSellingPricePositive_Pass() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setSalePercentage(0);
        product.setOnSale(false);

        assertDoesNotThrow(() -> oclValidationService.validateProduct(product));
    }

    @Test
    @DisplayName("Product: sellingPricePositive - should fail when price <= 0")
    void testSellingPricePositive_Fail() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("0"));
        product.setStockQuantity(10);
        product.setSalePercentage(0);
        product.setOnSale(false);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateProduct(product));
        
        assertHasInvariant(ex, "sellingPricePositive");
    }

    @Test
    @DisplayName("Product: stockQuantityNonNegative - should pass when quantity >= 0")
    void testStockQuantityNonNegative_Pass() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(0);
        product.setSalePercentage(0);
        product.setOnSale(false);

        assertDoesNotThrow(() -> oclValidationService.validateProduct(product));
    }

    @Test
    @DisplayName("Product: stockQuantityNonNegative - should fail when quantity < 0")
    void testStockQuantityNonNegative_Fail() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(-1);
        product.setSalePercentage(0);
        product.setOnSale(false);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateProduct(product));
        
        assertHasInvariant(ex, "stockQuantityNonNegative");
    }

    @Test
    @DisplayName("Product: salePercentageRange - should pass when percentage is 0-100")
    void testSalePercentageRange_Pass() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setSalePercentage(50);
        product.setOnSale(true);

        assertDoesNotThrow(() -> oclValidationService.validateProduct(product));
    }

    @Test
    @DisplayName("Product: salePercentageRange - should fail when percentage > 100")
    void testSalePercentageRange_Fail_Over100() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setSalePercentage(101);
        product.setOnSale(true);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateProduct(product));
        
        assertHasInvariant(ex, "salePercentageRange");
    }

    @Test
    @DisplayName("Product: salePercentageRange - should fail when percentage < 0")
    void testSalePercentageRange_Fail_Negative() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setSalePercentage(-1);
        product.setOnSale(true);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateProduct(product));
        
        assertHasInvariant(ex, "salePercentageRange");
    }

    @Test
    @DisplayName("Product: onSaleImpliesPercentage - should pass when onSale=true and percentage > 0")
    void testOnSaleImpliesPercentage_Pass() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setSalePercentage(20);
        product.setOnSale(true);

        assertDoesNotThrow(() -> oclValidationService.validateProduct(product));
    }

    @Test
    @DisplayName("Product: onSaleImpliesPercentage - should fail when onSale=true but percentage <= 0")
    void testOnSaleImpliesPercentage_Fail() {
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setSalePercentage(0);
        product.setOnSale(true);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateProduct(product));
        
        assertHasInvariant(ex, "onSaleImpliesPercentage");
    }

    // ==================== Order Invariants ====================

    @Test
    @DisplayName("Order: totalEqualsSubtotalPlusShipping - should pass when total = subtotal + shipping")
    void testTotalEqualsSubtotalPlusShipping_Pass() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("110.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setPriceSnapshot(new BigDecimal("100.00"));

        assertDoesNotThrow(() -> oclValidationService.validateOrder(order, List.of(item)));
    }

    @Test
    @DisplayName("Order: totalEqualsSubtotalPlusShipping - should fail when total != subtotal + shipping")
    void testTotalEqualsSubtotalPlusShipping_Fail() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("120.00")); // Wrong total
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setPriceSnapshot(new BigDecimal("100.00"));

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateOrder(order, List.of(item)));
        
        assertHasInvariant(ex, "totalEqualsSubtotalPlusShipping");
    }

    @Test
    @DisplayName("Order: hasAtLeastOneItem - should pass when items list has at least one item")
    void testHasAtLeastOneItem_Pass() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("110.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setPriceSnapshot(new BigDecimal("100.00"));

        assertDoesNotThrow(() -> oclValidationService.validateOrder(order, List.of(item)));
    }

    @Test
    @DisplayName("Order: hasAtLeastOneItem - should fail when items list is empty")
    void testHasAtLeastOneItem_Fail() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("110.00"));
        order.setStatus(OrderStatus.PENDING);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateOrder(order, List.of()));
        
        assertHasInvariant(ex, "hasAtLeastOneItem");
    }

    // ==================== OrderItem Invariants ====================

    @Test
    @DisplayName("OrderItem: quantityPositive - should pass when quantity > 0")
    void testQuantityPositive_Pass() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("110.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setPriceSnapshot(new BigDecimal("100.00"));

        assertDoesNotThrow(() -> oclValidationService.validateOrder(order, List.of(item)));
    }

    @Test
    @DisplayName("OrderItem: quantityPositive - should fail when quantity <= 0")
    void testQuantityPositive_Fail() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("110.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setQuantity(0);
        item.setPriceSnapshot(new BigDecimal("100.00"));

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateOrder(order, List.of(item)));
        
        assertHasInvariant(ex, "quantityPositive");
    }

    @Test
    @DisplayName("OrderItem: priceSnapshotPositive - should pass when price > 0")
    void testPriceSnapshotPositive_Pass() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("110.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setPriceSnapshot(new BigDecimal("100.00"));

        assertDoesNotThrow(() -> oclValidationService.validateOrder(order, List.of(item)));
    }

    @Test
    @DisplayName("OrderItem: priceSnapshotPositive - should fail when price <= 0")
    void testPriceSnapshotPositive_Fail() {
        Order order = new Order();
        order.setSubtotal(new BigDecimal("100.00"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("110.00"));
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setPriceSnapshot(new BigDecimal("0"));

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateOrder(order, List.of(item)));
        
        assertHasInvariant(ex, "priceSnapshotPositive");
    }

    // ==================== Return Rule ====================

    @Test
    @DisplayName("Order: returnOnlyWhenDelivered - should pass when status is DELIVERED")
    void testReturnOnlyWhenDelivered_Pass() {
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);
        order.setReturnStatus(ReturnStatus.REQUESTED);

        assertDoesNotThrow(() -> oclValidationService.validateReturnAllowed(order));
    }

    @Test
    @DisplayName("Order: returnOnlyWhenDelivered - should fail when status is not DELIVERED")
    void testReturnOnlyWhenDelivered_Fail() {
        Order order = new Order();
        order.setStatus(OrderStatus.SHIPPED);
        order.setReturnStatus(ReturnStatus.REQUESTED);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateReturnAllowed(order));
        
        assertHasInvariant(ex, "returnOnlyWhenDelivered");
    }

    // ==================== Confirm Delivery Rule ====================

    @Test
    @DisplayName("Order: confirmDeliveryOnlyWhenShipped - should pass when status is SHIPPED")
    void testConfirmDeliveryOnlyWhenShipped_Pass() {
        Order order = new Order();
        order.setStatus(OrderStatus.SHIPPED);

        assertDoesNotThrow(() -> oclValidationService.validateConfirmDeliveryAllowed(order));
    }

    @Test
    @DisplayName("Order: confirmDeliveryOnlyWhenShipped - should fail when status is not SHIPPED")
    void testConfirmDeliveryOnlyWhenShipped_Fail() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        OclValidationException ex = assertThrows(OclValidationException.class, 
            () -> oclValidationService.validateConfirmDeliveryAllowed(order));
        
        assertHasInvariant(ex, "confirmDeliveryOnlyWhenShipped");
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to assert that an exception contains a specific invariant violation
     */
    private void assertHasInvariant(OclValidationException ex, String invariantName) {
        assertNotNull(ex.getViolations(), "Violations list should not be null");
        assertFalse(ex.getViolations().isEmpty(), "Violations list should not be empty");
        
        boolean found = ex.getViolations().stream()
            .anyMatch(v -> invariantName.equals(v.getInvariantName()));
        
        assertTrue(found, 
            String.format("Expected invariant '%s' not found in violations. Found: %s", 
                invariantName, 
                ex.getViolations().stream()
                    .map(OclViolationDto::getInvariantName)
                    .toList()));
    }
}

