package com.americanwomen.store.pattern.shipping;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Standard shipping strategy
 * Rule: FREE if subtotal >= $200, otherwise $20.00
 */
@Component
public class StandardShippingStrategy implements ShippingStrategy {
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200.00");
    private static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("20.00");
    
    @Override
    public BigDecimal calculateShippingFee(BigDecimal subtotal) {
        // Free shipping if subtotal is $200 or more
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }
        return STANDARD_SHIPPING_FEE;
    }
    
    @Override
    public String getShippingMethodName() {
        return "STANDARD";
    }
}

