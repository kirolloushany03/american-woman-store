package com.americanwomen.store.integration;

import com.americanwomen.store.entity.*;
import com.americanwomen.store.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

/**
 * Base class for integration tests using Testcontainers
 * 
 * Provides:
 * - MySQL container setup
 * - Test data seeding (admin user, regular user, products)
 * - Common test utilities
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
public abstract class BaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("awstore")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-test.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CartItemRepository cartItemRepository;

    @Autowired
    protected OrderRepository orderRepository;


    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected User adminUser;
    protected User regularUser;
    protected Product product1;
    protected Product product2;

    @BeforeEach
    void setUpTestData() {
        // Clean up existing data (OrderItems are deleted via cascade)
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(User.Role.ROLE_ADMIN);
        adminUser = userRepository.save(adminUser);

        // Create regular user
        regularUser = new User();
        regularUser.setUsername("testuser");
        regularUser.setEmail("testuser@test.com");
        regularUser.setPassword(passwordEncoder.encode("password123"));
        regularUser.setFirstName("Test");
        regularUser.setLastName("User");
        regularUser.setRole(User.Role.ROLE_USER);
        regularUser = userRepository.save(regularUser);

        // Create product 1
        product1 = new Product();
        product1.setName("Test Product 1");
        product1.setDescription("Test Description 1");
        product1.setCategory("Test");
        product1.setSellingPrice(new BigDecimal("99.99"));
        product1.setCostPrice(new BigDecimal("50.00"));
        product1.setSizes("S,M,L");
        product1.setColors("Red,Blue");
        product1.setStockQuantity(10);
        product1.setImageUrl("/assets/images/test1.jpg");
        product1.setNewArrival(true);
        product1.setBestSeller(false);
        product1.setOnSale(false);
        product1.setSalePercentage(0);
        product1 = productRepository.save(product1);

        // Create product 2
        product2 = new Product();
        product2.setName("Test Product 2");
        product2.setDescription("Test Description 2");
        product2.setCategory("Test");
        product2.setSellingPrice(new BigDecimal("149.99"));
        product2.setCostPrice(new BigDecimal("75.00"));
        product2.setSizes("M,L,XL");
        product2.setColors("Black,White");
        product2.setStockQuantity(20);
        product2.setImageUrl("/assets/images/test2.jpg");
        product2.setNewArrival(false);
        product2.setBestSeller(true);
        product2.setOnSale(true);
        product2.setSalePercentage(20);
        product2 = productRepository.save(product2);
    }

    @Autowired
    protected com.americanwomen.store.security.JwtTokenProvider jwtTokenProvider;

    /**
     * Helper method to get JWT token for a user
     */
    protected String getTokenForUser(User user) {
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
    }
}

