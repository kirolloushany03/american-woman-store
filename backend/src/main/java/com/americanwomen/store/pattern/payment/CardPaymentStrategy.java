package com.americanwomen.store.pattern.payment;

import com.americanwomen.store.entity.Order;
import org.springframework.stereotype.Component;

/**
 * Card payment strategy (Visa, Mastercard, etc.)
 * Processes card payments securely without storing sensitive data
 */
@Component
public class CardPaymentStrategy implements PaymentStrategy {
    
    @Override
    public boolean processPayment(Order order) {
        // In a real implementation, this would integrate with a payment gateway
        // For academic purposes, we simulate successful payment processing
        // NEVER store full card numbers or CVV - only last 4 digits and brand
        
        // Validate that card information is present (cardLast4 should be set)
        if (order.getCardLast4() == null || order.getCardLast4().isEmpty()) {
            return false;
        }
        
        // Simulate payment gateway call
        // In production: call external payment service (Stripe, PayPal, etc.)
        return simulatePaymentGateway(order);
    }
    
    private boolean simulatePaymentGateway(Order order) {
        // Simulate payment processing
        // In real implementation: 
        // - Call payment gateway API
        // - Handle 3D Secure if required
        // - Process payment authorization
        // - Handle errors and retries
        
        // For demo: always return true if card info is valid
        return true;
    }
    
    @Override
    public String getPaymentMethodName() {
        return "CARD";
    }
}

