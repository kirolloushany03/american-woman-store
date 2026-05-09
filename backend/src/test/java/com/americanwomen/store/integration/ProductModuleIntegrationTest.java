package com.americanwomen.store.integration;

import com.americanwomen.store.dto.ProductDto;
import com.americanwomen.store.entity.Product;
import com.americanwomen.store.repository.ProductRepository;
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
 * Integration tests for Product module
 * 
 * Tests:
 * - List products public → 200
 * - Admin creates product valid → 200
 * - Admin creates product invalid (OCL violations) → 400
 * - Admin updates product → 200
 * - Admin deletes product → 200
 */
@DisplayName("Product Module Integration Tests")
@AutoConfigureMockMvc
class ProductModuleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("List products public → 200")
    void testListProducts_Public_Returns200() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Admin creates product with valid data → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateProduct_ValidData_Returns200() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("New Product");
        dto.setDescription("New Description");
        dto.setCategory("New Category");
        dto.setSellingPrice(new BigDecimal("199.99"));
        dto.setCostPrice(new BigDecimal("100.00"));
        dto.setSizes("S,M,L");
        dto.setColors("Red,Blue");
        dto.setStockQuantity(15);
        dto.setImageUrl("/assets/images/new.jpg");
        dto.setNewArrival(true);
        dto.setBestSeller(false);
        dto.setOnSale(false);
        dto.setSalePercentage(0);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New Product"));
        
        // Verify product was saved in database
        Product savedProduct = productRepository.findAll().stream()
            .filter(p -> p.getName().equals("New Product"))
            .findFirst()
            .orElse(null);
        assertNotNull(savedProduct);
        assertEquals(new BigDecimal("199.99"), savedProduct.getSellingPrice());
    }

    @Test
    @DisplayName("Admin creates product with negative selling price → 400 with OCL violations")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateProduct_NegativePrice_Returns400WithOclViolations() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("Invalid Product");
        dto.setDescription("Test");
        dto.setCategory("Test");
        dto.setSellingPrice(new BigDecimal("-10.00")); // Invalid: negative
        dto.setCostPrice(new BigDecimal("5.00"));
        dto.setStockQuantity(10);
        dto.setOnSale(false);
        dto.setSalePercentage(0);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("OCL validation failed")))
            .andExpect(jsonPath("$.violations[0].invariantName").value("sellingPricePositive"));
    }

    @Test
    @DisplayName("Admin creates product with negative stock → 400 with OCL violations")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateProduct_NegativeStock_Returns400WithOclViolations() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("Invalid Product");
        dto.setDescription("Test");
        dto.setCategory("Test");
        dto.setSellingPrice(new BigDecimal("10.00"));
        dto.setCostPrice(new BigDecimal("5.00"));
        dto.setStockQuantity(-1); // Invalid: negative
        dto.setOnSale(false);
        dto.setSalePercentage(0);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.violations[0].invariantName").value("stockQuantityNonNegative"));
    }

    @Test
    @DisplayName("Admin creates product with onSale=true but salePercentage=0 → 400 with OCL violations")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateProduct_OnSaleWithoutPercentage_Returns400WithOclViolations() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("Invalid Product");
        dto.setDescription("Test");
        dto.setCategory("Test");
        dto.setSellingPrice(new BigDecimal("10.00"));
        dto.setCostPrice(new BigDecimal("5.00"));
        dto.setStockQuantity(10);
        dto.setOnSale(true); // Invalid: onSale=true requires salePercentage > 0
        dto.setSalePercentage(0);

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.violations[0].invariantName").value("onSaleImpliesPercentage"));
    }

    @Test
    @DisplayName("Admin updates product → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateProduct_ValidData_Returns200() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("Updated Product");
        dto.setDescription("Updated Description");
        dto.setCategory("Updated Category");
        dto.setSellingPrice(new BigDecimal("249.99"));
        dto.setCostPrice(new BigDecimal("125.00"));
        dto.setSizes("M,L");
        dto.setColors("Green");
        dto.setStockQuantity(25);
        dto.setImageUrl("/assets/images/updated.jpg");
        dto.setNewArrival(false);
        dto.setBestSeller(true);
        dto.setOnSale(true);
        dto.setSalePercentage(15);

        mockMvc.perform(put("/api/admin/products/" + product1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Product"));
        
        // Verify product was updated in database
        Product updatedProduct = productRepository.findById(product1.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(new BigDecimal("249.99"), updatedProduct.getSellingPrice());
    }

    @Test
    @DisplayName("Admin deletes product → 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteProduct_ValidId_Returns200() throws Exception {
        Long productId = product1.getId();
        
        mockMvc.perform(delete("/api/admin/products/" + productId)
                .with(csrf()))
            .andExpect(status().isOk());
        
        // Verify product was deleted from database
        assertFalse(productRepository.existsById(productId));
    }
}

