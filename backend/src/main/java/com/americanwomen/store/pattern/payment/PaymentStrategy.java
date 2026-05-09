package com.americanwomen.store.pattern.payment;

import com.americanwomen.store.entity.Order;

/**
 * Strategy Pattern for Payment Processing
 * 
 * This interface defines the contract for different payment strategies.
 * Each implementation handles a specific payment method (COD, Visa, etc.)
 */
public interface PaymentStrategy {
    /**
     * Process payment for the given order
     * @param order The order to process payment for
     * @return true if payment was successful, false otherwise
     */
    boolean processPayment(Order order);
    
    /**
     * Get the payment method name this strategy handles
     * @return Payment method name
     */
    String getPaymentMethodName();
}

