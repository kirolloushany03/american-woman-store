package com.americanwomen.store.pattern.shipping;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Express shipping strategy
 * Rule: FREE if subtotal >= $200, otherwise $35.00
 */
@Component
public class ExpressShippingStrategy implements ShippingStrategy {
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200.00");
    private static final BigDecimal EXPRESS_SHIPPING_FEE = new BigDecimal("35.00");
    
    @Override
    public BigDecimal calculateShippingFee(BigDecimal subtotal) {
        // Free shipping if subtotal is $200 or more
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }
        return EXPRESS_SHIPPING_FEE;
    }
    
    @Override
    public String getShippingMethodName() {
        return "EXPRESS";
    }
}

