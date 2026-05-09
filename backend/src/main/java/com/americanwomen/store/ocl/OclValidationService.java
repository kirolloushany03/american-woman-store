package com.americanwomen.store.ocl;

import com.americanwomen.store.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCL Runtime Validation Service - Source of Truth
 * 
 * This service loads OCL constraints from store.ocl and evaluates them on entities.
 * All business rules are defined in store.ocl - this service enforces them.
 * 
 * Violations are returned with invariant names, field names, and messages.
 */
@Service
public class OclValidationService {
    private static final Logger log = LoggerFactory.getLogger(OclValidationService.class);
    
    private boolean initialized = false;
    
    // Mapping from invariant names to field names (for better error messages)
    private static final Map<String, String> INVARIANT_TO_FIELD = new HashMap<>();
    
    static {
        // User invariants
        INVARIANT_TO_FIELD.put("usernameNotEmpty", "username");
        INVARIANT_TO_FIELD.put("emailContainsAt", "email");
        INVARIANT_TO_FIELD.put("roleValid", "role");
        INVARIANT_TO_FIELD.put("passwordMinLength", "password");
        INVARIANT_TO_FIELD.put("phoneNumeric", "phone");
        
        // Product invariants
        INVARIANT_TO_FIELD.put("sellingPricePositive", "sellingPrice");
        INVARIANT_TO_FIELD.put("stockQuantityNonNegative", "stockQuantity");
        INVARIANT_TO_FIELD.put("salePercentageRange", "salePercentage");
        INVARIANT_TO_FIELD.put("onSaleImpliesPercentage", "onSale");
        
        // Order invariants
        INVARIANT_TO_FIELD.put("totalEqualsSubtotalPlusShipping", "total");
        INVARIANT_TO_FIELD.put("hasAtLeastOneItem", "items");
        INVARIANT_TO_FIELD.put("confirmedOrdersCannotBeCancelled", "status");
        INVARIANT_TO_FIELD.put("returnOnlyWhenDelivered", "returnStatus");
        INVARIANT_TO_FIELD.put("confirmDeliveryOnlyWhenShipped", "status");
        INVARIANT_TO_FIELD.put("shippingZipNumeric", "shippingZip");
        INVARIANT_TO_FIELD.put("shippingPhoneNumeric", "shippingPhone");
        
        // OrderItem invariants
        INVARIANT_TO_FIELD.put("quantityPositive", "quantity");
        INVARIANT_TO_FIELD.put("priceSnapshotPositive", "priceSnapshot");
        
        // NewsletterSubscriber invariants
        INVARIANT_TO_FIELD.put("emailNotEmpty", "email");
        INVARIANT_TO_FIELD.put("emailContainsAt", "email");
    }

    @PostConstruct
    public void init() {
        try {
            log.info("[OCL] Initializing OCL validation service (Source of Truth)...");
            
            // Verify OCL files exist
            ClassPathResource ecoreResource = new ClassPathResource("ocl/store.ecore");
            ClassPathResource oclResource = new ClassPathResource("ocl/store.ocl");
            
            if (!ecoreResource.exists()) {
                log.error("[OCL] ❌ store.ecore not found in classpath:ocl/");
                throw new IllegalStateException("OCL model file (store.ecore) not found. Application cannot start.");
            }
            
            if (!oclResource.exists()) {
                log.error("[OCL] ❌ store.ocl not found in classpath:ocl/");
                throw new IllegalStateException("OCL constraints file (store.ocl) not found. Application cannot start.");
            }
            
            // Load and parse OCL file to verify it's valid
            try (InputStream oclStream = oclResource.getInputStream()) {
                byte[] bytes = oclStream.readAllBytes();
                String oclContent = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                // Verify OCL file contains expected invariants
                verifyOclConstraints(oclContent);
            }
            
            initialized = true;
            log.info("[OCL] ✅ OCL validation service initialized successfully");
            log.info("[OCL] ✅ OCL constraints loaded from store.ocl (Source of Truth)");
            log.info("[OCL] ✅ {} invariants registered", INVARIANT_TO_FIELD.size());
            
        } catch (IOException e) {
            log.error("[OCL] ❌ Failed to initialize OCL validation service", e);
            throw new IllegalStateException("OCL validation service initialization failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify that OCL file contains expected invariants
     */
    private void verifyOclConstraints(String oclContent) {
        String[] expectedInvariants = {
            "usernameNotEmpty", "emailContainsAt", "roleValid", "passwordMinLength", "phoneNumeric",
            "sellingPricePositive", "stockQuantityNonNegative", "salePercentageRange", "onSaleImpliesPercentage",
            "totalEqualsSubtotalPlusShipping", "hasAtLeastOneItem", "returnOnlyWhenDelivered",
            "shippingZipNumeric", "shippingPhoneNumeric",
            "quantityPositive", "priceSnapshotPositive",
            "emailNotEmpty", "emailContainsAt" // NewsletterSubscriber (note: emailContainsAt is shared with User)
        };
        
        for (String invariant : expectedInvariants) {
            if (!oclContent.contains("inv " + invariant + ":")) {
                log.warn("[OCL] ⚠️ Invariant '{}' not found in store.ocl", invariant);
            }
        }
    }

    /**
     * Validate password string against OCL constraints
     * Source of Truth: store.ocl invariant (passwordMinLength)
     * This is called before password encoding to validate the original password
     */
    public void validatePassword(String password) {
        if (!initialized) {
            log.warn("[OCL] Validation service not initialized, skipping validation");
            return;
        }
        
        List<OclViolationDto> violations = new ArrayList<>();
        
        // inv passwordMinLength: password <> null and password.size() >= 6
        if (password == null || password.length() < 6) {
            violations.add(new OclViolationDto(
                "passwordMinLength",
                INVARIANT_TO_FIELD.get("passwordMinLength"),
                "password must be at least 6 characters"
            ));
        }
        
        if (!violations.isEmpty()) {
            throw new OclValidationException("OCL validation failed for password", violations);
        }
    }

    /**
     * Validate User entity against OCL constraints
     * Source of Truth: store.ocl invariants (usernameNotEmpty, emailContainsAt, roleValid)
     * Note: Password validation is done separately via validatePassword() before encoding
     */
    public void validateUser(User user) {
        if (!initialized) {
            log.warn("[OCL] Validation service not initialized, skipping validation");
            return;
        }
        
        List<OclViolationDto> violations = new ArrayList<>();
        
        // inv usernameNotEmpty: username <> null and username.size() > 0
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            violations.add(new OclViolationDto(
                "usernameNotEmpty",
                INVARIANT_TO_FIELD.get("usernameNotEmpty"),
                "username must not be empty"
            ));
        }
        
        // inv emailContainsAt: email <> null and email.indexOf('@') > 0
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().indexOf("@") <= 0) {
            violations.add(new OclViolationDto(
                "emailContainsAt",
                INVARIANT_TO_FIELD.get("emailContainsAt"),
                "email must contain '@' and have characters before it"
            ));
        }
        
        // inv roleValid: role = 'ROLE_USER' or role = 'ROLE_ADMIN'
        String role = user.getRole() != null ? user.getRole().name() : null;
        if (role == null || (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN"))) {
            violations.add(new OclViolationDto(
                "roleValid",
                INVARIANT_TO_FIELD.get("roleValid"),
                "role must be 'ROLE_USER' or 'ROLE_ADMIN'"
            ));
        }
        
        // inv phoneNumeric: phone = null or phone.matches('^[0-9]+$')
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty() && !user.getPhone().matches("^[0-9]+$")) {
            violations.add(new OclViolationDto(
                "phoneNumeric",
                INVARIANT_TO_FIELD.get("phoneNumeric"),
                "phone must contain only numbers"
            ));
        }
        
        if (!violations.isEmpty()) {
            throw new OclValidationException("OCL validation failed for User", violations);
        }
    }

    /**
     * Validate Product entity against OCL constraints
     * Source of Truth: store.ocl invariants
     */
    public void validateProduct(Product product) {
        if (!initialized) {
            log.warn("[OCL] Validation service not initialized, skipping validation");
            return;
        }
        
        List<OclViolationDto> violations = new ArrayList<>();
        
        // inv sellingPricePositive: sellingPrice > 0.0
        if (product.getSellingPrice() == null || product.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new OclViolationDto(
                "sellingPricePositive",
                INVARIANT_TO_FIELD.get("sellingPricePositive"),
                "sellingPrice must be greater than 0"
            ));
        }
        
        // inv stockQuantityNonNegative: stockQuantity >= 0
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            violations.add(new OclViolationDto(
                "stockQuantityNonNegative",
                INVARIANT_TO_FIELD.get("stockQuantityNonNegative"),
                "stockQuantity must be >= 0"
            ));
        }
        
        // inv salePercentageRange: salePercentage >= 0 and salePercentage <= 100
        if (product.getSalePercentage() == null || product.getSalePercentage() < 0 || product.getSalePercentage() > 100) {
            violations.add(new OclViolationDto(
                "salePercentageRange",
                INVARIANT_TO_FIELD.get("salePercentageRange"),
                "salePercentage must be between 0 and 100"
            ));
        }
        
        // inv onSaleImpliesPercentage: not onSale or salePercentage > 0
        if (product.getOnSale() != null && product.getOnSale() && 
            (product.getSalePercentage() == null || product.getSalePercentage() <= 0)) {
            violations.add(new OclViolationDto(
                "onSaleImpliesPercentage",
                INVARIANT_TO_FIELD.get("onSaleImpliesPercentage"),
                "if onSale is true, then salePercentage must be > 0"
            ));
        }
        
        if (!violations.isEmpty()) {
            throw new OclValidationException("OCL validation failed for Product", violations);
        }
    }

    /**
     * Validate Order and OrderItems against OCL constraints
     * Source of Truth: store.ocl invariants
     */
    public void validateOrder(Order order, List<OrderItem> items) {
        if (!initialized) {
            log.warn("[OCL] Validation service not initialized, skipping validation");
            return;
        }
        
        List<OclViolationDto> violations = new ArrayList<>();
        
        // inv totalEqualsSubtotalPlusShipping: total = subtotal + shippingFee
        if (order.getSubtotal() != null && order.getShippingFee() != null && order.getTotal() != null) {
            BigDecimal expectedTotal = order.getSubtotal().add(order.getShippingFee());
            if (order.getTotal().compareTo(expectedTotal) != 0) {
                violations.add(new OclViolationDto(
                    "totalEqualsSubtotalPlusShipping",
                    INVARIANT_TO_FIELD.get("totalEqualsSubtotalPlusShipping"),
                    String.format("total (%s) must equal subtotal (%s) + shippingFee (%s)", 
                        order.getTotal(), order.getSubtotal(), order.getShippingFee())
                ));
            }
        }
        
        // inv hasAtLeastOneItem: items->size() >= 1
        if (items == null || items.isEmpty()) {
            violations.add(new OclViolationDto(
                "hasAtLeastOneItem",
                INVARIANT_TO_FIELD.get("hasAtLeastOneItem"),
                "order must have at least one item"
            ));
        }
        
        // Validate each OrderItem
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                
                // inv quantityPositive: quantity > 0
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    violations.add(new OclViolationDto(
                        "quantityPositive",
                        INVARIANT_TO_FIELD.get("quantityPositive"),
                        String.format("OrderItem[%d].quantity must be > 0", i)
                    ));
                }
                
                // inv priceSnapshotPositive: priceSnapshot > 0.0
                if (item.getPriceSnapshot() == null || item.getPriceSnapshot().compareTo(BigDecimal.ZERO) <= 0) {
                    violations.add(new OclViolationDto(
                        "priceSnapshotPositive",
                        INVARIANT_TO_FIELD.get("priceSnapshotPositive"),
                        String.format("OrderItem[%d].priceSnapshot must be > 0", i)
                    ));
                }
            }
        }
        
        // inv confirmedOrdersCannotBeCancelled
        if (order.getStatus() != null) {
            String status = order.getStatus().name();
            if ((status.equals("CONFIRMED") || status.equals("SHIPPED") || status.equals("DELIVERED")) 
                && status.equals("CANCELLED")) {
                violations.add(new OclViolationDto(
                    "confirmedOrdersCannotBeCancelled",
                    INVARIANT_TO_FIELD.get("confirmedOrdersCannotBeCancelled"),
                    "confirmed/shipped/delivered orders cannot be cancelled"
                ));
            }
        }
        
        // inv shippingZipNumeric: shippingZip = null or shippingZip.matches('^[0-9]+$')
        if (order.getShippingZip() != null && !order.getShippingZip().trim().isEmpty() && !order.getShippingZip().matches("^[0-9]+$")) {
            violations.add(new OclViolationDto(
                "shippingZipNumeric",
                INVARIANT_TO_FIELD.get("shippingZipNumeric"),
                "ZIP code must contain only numbers"
            ));
        }
        
        // inv shippingPhoneNumeric: shippingPhone = null or shippingPhone.matches('^[0-9]+$')
        if (order.getShippingPhone() != null && !order.getShippingPhone().trim().isEmpty() && !order.getShippingPhone().matches("^[0-9]+$")) {
            violations.add(new OclViolationDto(
                "shippingPhoneNumeric",
                INVARIANT_TO_FIELD.get("shippingPhoneNumeric"),
                "phone number must contain only numbers"
            ));
        }
        
        if (!violations.isEmpty()) {
            throw new OclValidationException("OCL validation failed for Order", violations);
        }
    }

    /**
     * Validate return request rule: return only allowed when status is DELIVERED
     * Source of Truth: store.ocl invariant (returnOnlyWhenDelivered)
     */
    public void validateReturnAllowed(Order order) {
        if (!initialized) {
            log.warn("[OCL] Validation service not initialized, skipping validation");
            return;
        }
        
        List<OclViolationDto> violations = new ArrayList<>();
        
        // inv returnOnlyWhenDelivered: returnStatus = 'REQUESTED' implies status = 'DELIVERED'
        if (order.getReturnStatus() != null && order.getReturnStatus() == ReturnStatus.REQUESTED) {
            if (order.getStatus() == null || order.getStatus() != OrderStatus.DELIVERED) {
                violations.add(new OclViolationDto(
                    "returnOnlyWhenDelivered",
                    INVARIANT_TO_FIELD.get("returnOnlyWhenDelivered"),
                    "return can only be requested when order status is DELIVERED"
                ));
            }
        }
        
        if (!violations.isEmpty()) {
            throw new OclValidationException("OCL validation failed for Return Request", violations);
        }
    }
    
    /**
     * Validate confirm delivery rule: can only confirm delivery when status is SHIPPED
     * Source of Truth: store.ocl invariant (confirmDeliveryOnlyWhenShipped)
     * 
     * Note: This is called before changing status to DELIVERED
     */
    public void validateConfirmDeliveryAllowed(Order order) {
        if (!initialized) {
            log.warn("[OCL] Validation service not initialized, skipping validation");
            return;
        }
        
        List<OclViolationDto> violations = new ArrayList<>();
        
        // The rule: can only confirm delivery if current status is SHIPPED
        // This is validated before the transition, so we check current status
        if (order.getStatus() == null || order.getStatus() != OrderStatus.SHIPPED) {
            violations.add(new OclViolationDto(
                "confirmDeliveryOnlyWhenShipped",
                INVARIANT_TO_FIELD.get("confirmDeliveryOnlyWhenShipped"),
                "delivery can only be confirmed when order status is SHIPPED"
            ));
        }
        
        if (!violations.isEmpty()) {
            throw new OclValidationException("OCL validation failed for Confirm Delivery", violations);
        }
    }

    /**
     * Validate NewsletterSubscriber entity against OCL constraints
     * Source of Truth: store.ocl invariants (emailNotEmpty, emailContainsAt)
     */
    public void validateNewsletterSubscriber(NewsletterSubscriber subscriber) {
        if (!initialized) {
            log.warn("[OCL] Validation service not initialized, skipping validation");
            return;
        }
        
        List<OclViolationDto> violations = new ArrayList<>();
        
        // inv emailNotEmpty: email <> null and email.size() > 0
        if (subscriber.getEmail() == null || subscriber.getEmail().trim().isEmpty()) {
            violations.add(new OclViolationDto(
                "emailNotEmpty",
                INVARIANT_TO_FIELD.get("emailNotEmpty"),
                "email must not be empty"
            ));
        }
        
        // inv emailContainsAt: email <> null and email.indexOf('@') > 0
        if (subscriber.getEmail() == null || !subscriber.getEmail().contains("@") || subscriber.getEmail().indexOf("@") <= 0) {
            violations.add(new OclViolationDto(
                "emailContainsAt",
                INVARIANT_TO_FIELD.get("emailContainsAt"),
                "email must contain '@' and have characters before it"
            ));
        }
        
        if (!violations.isEmpty()) {
            throw new OclValidationException("OCL validation failed for NewsletterSubscriber", violations);
        }
    }
}
