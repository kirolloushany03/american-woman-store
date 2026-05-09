package com.americanwomen.store.pattern.shipping;

import java.math.BigDecimal;

/**
 * Strategy Pattern for Shipping Calculation
 * 
 * This interface defines the contract for different shipping calculation strategies.
 * Each implementation calculates shipping fee based on different shipping methods.
 */
public interface ShippingStrategy {
    /**
     * Calculate shipping fee for the given subtotal
     * @param subtotal Order subtotal amount
     * @return Calculated shipping fee
     */
    BigDecimal calculateShippingFee(BigDecimal subtotal);
    
    /**
     * Get the shipping method name this strategy handles
     * @return Shipping method name
     */
    String getShippingMethodName();
}

