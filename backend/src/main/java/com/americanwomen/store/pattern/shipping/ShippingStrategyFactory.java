package com.americanwomen.store.pattern.shipping;

import com.americanwomen.store.entity.ShippingMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory Pattern for Shipping Strategies
 * 
 * Creates and manages shipping strategy instances based on shipping method
 */
@Component
public class ShippingStrategyFactory {
    private final Map<ShippingMethod, ShippingStrategy> strategies;
    
    public ShippingStrategyFactory(List<ShippingStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (ShippingStrategy strategy : strategyList) {
            if (strategy instanceof StandardShippingStrategy) {
                strategies.put(ShippingMethod.STANDARD, strategy);
            } else if (strategy instanceof ExpressShippingStrategy) {
                strategies.put(ShippingMethod.EXPRESS, strategy);
            } else if (strategy instanceof OvernightShippingStrategy) {
                strategies.put(ShippingMethod.OVERNIGHT, strategy);
            }
        }
    }
    
    /**
     * Get shipping strategy for the given shipping method
     * @param shippingMethod The shipping method enum
     * @return ShippingStrategy implementation
     * @throws IllegalArgumentException if shipping method is not supported
     */
    public ShippingStrategy getStrategy(ShippingMethod shippingMethod) {
        ShippingStrategy strategy = strategies.get(shippingMethod);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported shipping method: " + shippingMethod);
        }
        return strategy;
    }
}

