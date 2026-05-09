package com.americanwomen.store.pattern.payment;

import com.americanwomen.store.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory Pattern for Payment Strategies
 * 
 * Creates and manages payment strategy instances based on payment method
 */
@Component
public class PaymentStrategyFactory {
    private final Map<PaymentMethod, PaymentStrategy> strategies;
    
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (PaymentStrategy strategy : strategyList) {
            if (strategy instanceof CashOnDeliveryStrategy) {
                strategies.put(PaymentMethod.COD, strategy);
            } else if (strategy instanceof CardPaymentStrategy) {
                strategies.put(PaymentMethod.CARD, strategy);
            }
        }
    }
    
    /**
     * Get payment strategy for the given payment method
     * @param paymentMethod The payment method enum
     * @return PaymentStrategy implementation
     * @throws IllegalArgumentException if payment method is not supported
     */
    public PaymentStrategy getStrategy(PaymentMethod paymentMethod) {
        PaymentStrategy strategy = strategies.get(paymentMethod);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return strategy;
    }
}

