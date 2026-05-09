package com.americanwomen.store.integration;

import com.americanwomen.store.dto.AddCartItemRequest;
import com.americanwomen.store.dto.UpdateCartItemRequest;
import com.americanwomen.store.entity.CartItem;
import com.americanwomen.store.repository.CartItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Integration tests for Cart module
 * 
 * Tests:
 * - User adds to cart (auth required) → 200
 * - Update quantity → 200
 * - Remove item → 200
 * - Clear cart → 200
 */
@DisplayName("Cart Module Integration Tests")
@AutoConfigureMockMvc
class CartModuleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("User adds item to cart → 200")
    @WithMockUser(username = "testuser")
    void testAddToCart_ValidItem_Returns200() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(product1.getId());
        request.setQuantity(2);
        request.setSize("M");
        request.setColor("Red");

        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk());

        // Verify cart item was saved
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(
            regularUser.getId(), product1.getId()).orElse(null);
        assertNotNull(cartItem);
        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    @DisplayName("User updates cart item quantity → 200")
    @WithMockUser(username = "testuser")
    void testUpdateCartItem_ValidQuantity_Returns200() throws Exception {
        // First add item
        AddCartItemRequest addRequest = new AddCartItemRequest();
        addRequest.setProductId(product1.getId());
        addRequest.setQuantity(1);
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest))
                .with(csrf()));

        // Update quantity
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest();
        updateRequest.setQuantity(5);

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(
            regularUser.getId(), product1.getId()).orElse(null);
        assertNotNull(cartItem);

        mockMvc.perform(put("/api/cart/" + cartItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
            .andExpect(status().isOk());

        // Verify quantity was updated
        CartItem updated = cartItemRepository.findById(cartItem.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
    }

    @Test
    @DisplayName("User removes item from cart → 200")
    @WithMockUser(username = "testuser")
    void testRemoveCartItem_ValidId_Returns200() throws Exception {
        // First add item
        AddCartItemRequest addRequest = new AddCartItemRequest();
        addRequest.setProductId(product1.getId());
        addRequest.setQuantity(1);
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest))
                .with(csrf()));

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(
            regularUser.getId(), product1.getId()).orElse(null);
        assertNotNull(cartItem);
        Long cartItemId = cartItem.getId();

        mockMvc.perform(delete("/api/cart/" + cartItemId)
                .with(csrf()))
            .andExpect(status().isOk());

        // Verify item was removed
        assertFalse(cartItemRepository.existsById(cartItemId));
    }

    @Test
    @DisplayName("User clears cart → 200")
    @WithMockUser(username = "testuser")
    void testClearCart_ValidUser_Returns200() throws Exception {
        // First add items
        AddCartItemRequest request1 = new AddCartItemRequest();
        request1.setProductId(product1.getId());
        request1.setQuantity(1);
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
                .with(csrf()));

        AddCartItemRequest request2 = new AddCartItemRequest();
        request2.setProductId(product2.getId());
        request2.setQuantity(1);
        mockMvc.perform(post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
                .with(csrf()));

        // Clear cart
        mockMvc.perform(delete("/api/cart")
                .with(csrf()))
            .andExpect(status().isOk());

        // Verify cart is empty
        assertEquals(0, cartItemRepository.findByUserId(regularUser.getId()).size());
    }
}

