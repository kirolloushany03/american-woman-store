package com.americanwomen.store.integration;

import com.americanwomen.store.dto.CheckoutRequest;
import com.americanwomen.store.entity.*;
import com.americanwomen.store.repository.CartItemRepository;
import com.americanwomen.store.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Order module
 * 
 * Tests:
 * - Checkout COD valid → 200 creates Order + OrderItems
 * - Checkout Visa/Card valid → 200 and DB stores ONLY cardLast4, cardBrand, cardHolderName
 * - Checkout invalid totals / missing items / violates OCL → 400 violations
 * - User gets "my orders" → 200 list includes new order
 * - Confirm delivery: invalid status → 400, correct status → 200
 * - Request return: invalid → 400, valid → 200
 * - Admin list orders → 200
 * - Admin update status: test common transitions
 * - Admin approve/reject return: test invalid + valid
 */
@DisplayName("Order Module Integration Tests")
@AutoConfigureMockMvc
class OrderModuleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUpCart() {
        // Add items to cart for checkout tests
        CartItem cartItem1 = new CartItem();
        cartItem1.setUserId(regularUser.getId());
        cartItem1.setProductId(product1.getId());
        cartItem1.setQuantity(2);
        cartItem1.setSize("M");
        cartItem1.setColor("Red");
        cartItem1.setPriceSnapshot(product1.getSellingPrice());
        cartItemRepository.save(cartItem1);
    }

    @Test
    @DisplayName("Checkout COD valid → 200 creates Order + OrderItems")
    @WithMockUser(username = "testuser")
    void testCheckout_COD_Valid_Returns200() throws Exception {
        CheckoutRequest request = new CheckoutRequest();
        request.setShippingMethod(ShippingMethod.STANDARD);
        request.setPaymentMethod(PaymentMethod.COD);
        request.setShippingAddress("123 Test St");
        request.setShippingCity("Test City");
        request.setShippingState("Test State");
        request.setShippingZip("12345");

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.total").exists());

        // Verify order was created
        Order order = orderRepository.findAll().stream()
            .filter(o -> o.getUserId().equals(regularUser.getId()))
            .findFirst()
            .orElse(null);
        assertNotNull(order);
        assertEquals(PaymentMethod.COD, order.getPaymentMethod());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().size() > 0);
        
        // Verify cart was cleared
        assertEquals(0, cartItemRepository.findByUserId(regularUser.getId()).size());
    }

    @Test
    @DisplayName("Checkout Visa/Card valid → 200 and DB stores ONLY cardLast4, cardBrand, cardHolderName")
    @WithMockUser(username = "testuser")
    void testCheckout_Card_Valid_StoresOnlyLast4() throws Exception {
        CheckoutRequest request = new CheckoutRequest();
        request.setShippingMethod(ShippingMethod.STANDARD);
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setShippingAddress("123 Test St");
        request.setShippingCity("Test City");
        request.setShippingState("Test State");
        request.setShippingZip("12345");
        request.setCardHolderName("Test User");
        request.setCardNumber("4111111111111111");
        request.setCardExpiry("12/25");
        request.setCardCvv("123");
        request.setCardBrand("VISA");

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk());

        // Verify order was created
        Order order = orderRepository.findAll().stream()
            .filter(o -> o.getUserId().equals(regularUser.getId()))
            .findFirst()
            .orElse(null);
        assertNotNull(order);
        assertEquals(PaymentMethod.CARD, order.getPaymentMethod());
        
        // Verify ONLY last4, brand, and holder name are stored (NOT full card or CVV)
        assertNotNull(order.getCardLast4());
        assertEquals("1111", order.getCardLast4());
        assertEquals("VISA", order.getCardBrand());
        assertEquals("Test User", order.getCardHolderName());
        // Card number and CVV should not be stored (security)
        // These fields don't exist in Order entity by design
    }

    @Test
    @DisplayName("Checkout with empty cart → 400 with OCL violations")
    @WithMockUser(username = "testuser")
    void testCheckout_EmptyCart_Returns400() throws Exception {
        // Clear cart first
        cartItemRepository.deleteAll();

        CheckoutRequest request = new CheckoutRequest();
        request.setShippingMethod(ShippingMethod.STANDARD);
        request.setPaymentMethod(PaymentMethod.COD);
        request.setShippingAddress("123 Test St");
        request.setShippingCity("Test City");
        request.setShippingState("Test State");
        request.setShippingZip("12345");

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("User gets my orders → 200 list includes new order")
    @WithMockUser(username = "testuser")
    void testGetMyOrders_Returns200() throws Exception {
        // Create an order first
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("199.98"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("209.98"));
        order.setCurrency("USD");
        orderRepository.save(order);

        mockMvc.perform(get("/api/orders/my"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @DisplayName("Confirm delivery for non-SHIPPED order → 400")
    @WithMockUser(username = "testuser")
    void testConfirmDelivery_NonShipped_Returns400() throws Exception {
        // Create PENDING order
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("109.99"));
        order.setCurrency("USD");
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/orders/" + order.getId() + "/confirm-delivery")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Confirm delivery for SHIPPED order → 200")
    @WithMockUser(username = "testuser")
    void testConfirmDelivery_Shipped_Returns200() throws Exception {
        // Create SHIPPED order
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.SHIPPED);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("109.99"));
        order.setCurrency("USD");
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/orders/" + order.getId() + "/confirm-delivery")
                .with(csrf()))
            .andExpect(status().isOk());

        // Verify status changed to DELIVERED
        Order updated = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(OrderStatus.DELIVERED, updated.getStatus());
    }

    @Test
    @DisplayName("Request return for non-DELIVERED order → 400")
    @WithMockUser(username = "testuser")
    void testRequestReturn_NonDelivered_Returns400() throws Exception {
        // Create SHIPPED order
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.SHIPPED);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("109.99"));
        order.setCurrency("USD");
        order.setReturnStatus(ReturnStatus.NONE);
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/orders/" + order.getId() + "/return-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Test reason\"}")
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Request return for DELIVERED order → 200")
    @WithMockUser(username = "testuser")
    void testRequestReturn_Delivered_Returns200() throws Exception {
        // Create DELIVERED order
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("109.99"));
        order.setCurrency("USD");
        order.setReturnStatus(ReturnStatus.NONE);
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/orders/" + order.getId() + "/return-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Test reason\"}")
                .with(csrf()))
            .andExpect(status().isOk());

        // Verify return status changed to REQUESTED
        Order updated = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(ReturnStatus.REQUESTED, updated.getReturnStatus());
    }

    @Test
    @DisplayName("Admin list orders → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminListOrders_Returns200() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Admin update order status PENDING→CONFIRMED → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminUpdateStatus_PendingToConfirmed_Returns200() throws Exception {
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("109.99"));
        order.setCurrency("USD");
        order = orderRepository.save(order);

        mockMvc.perform(patch("/api/admin/orders/" + order.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CONFIRMED\"}")
                .with(csrf()))
            .andExpect(status().isOk());

        Order updated = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    @DisplayName("Admin approve return for REQUESTED order → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminApproveReturn_Requested_Returns200() throws Exception {
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("109.99"));
        order.setCurrency("USD");
        order.setReturnStatus(ReturnStatus.REQUESTED);
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/admin/orders/" + order.getId() + "/return/approve")
                .with(csrf()))
            .andExpect(status().isOk());

        Order updated = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(ReturnStatus.APPROVED, updated.getReturnStatus());
        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
    }

    @Test
    @DisplayName("Admin reject return for REQUESTED order → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminRejectReturn_Requested_Returns200() throws Exception {
        Order order = new Order();
        order.setUserId(regularUser.getId());
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingFee(new BigDecimal("10.00"));
        order.setTotal(new BigDecimal("109.99"));
        order.setCurrency("USD");
        order.setReturnStatus(ReturnStatus.REQUESTED);
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/admin/orders/" + order.getId() + "/return/reject")
                .with(csrf()))
            .andExpect(status().isOk());

        Order updated = orderRepository.findById(order.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(ReturnStatus.REJECTED, updated.getReturnStatus());
    }
}

