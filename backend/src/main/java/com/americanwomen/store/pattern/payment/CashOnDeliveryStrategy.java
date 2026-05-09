package com.americanwomen.store.pattern.payment;

import com.americanwomen.store.entity.Order;
import org.springframework.stereotype.Component;

/**
 * Cash on Delivery payment strategy
 * Payment is processed when order is delivered
 */
@Component
public class CashOnDeliveryStrategy implements PaymentStrategy {
    
    @Override
    public boolean processPayment(Order order) {
        // COD: Payment is collected upon delivery
        // No immediate payment processing needed
        return true;
    }
    
    @Override
    public String getPaymentMethodName() {
        return "COD";
    }
}

