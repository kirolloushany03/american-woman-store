package com.americanwomen.store.pattern.factory;

import com.americanwomen.store.dto.CheckoutRequest;
import com.americanwomen.store.entity.*;
import com.americanwomen.store.repository.CartItemRepository;
import com.americanwomen.store.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory Pattern for Order Creation
 * 
 * Centralizes order creation logic and hides complex object construction
 * Controllers should use this factory instead of manually building Order entities
 */
@Component
public class OrderFactory {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    public OrderFactory(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }
    
    /**
     * Create an Order from CheckoutRequest and user ID
     * 
     * @param userId User ID placing the order
     * @param request Checkout request containing shipping and payment information
     * @return Created Order with OrderItems populated
     * @throws RuntimeException if cart is empty, product not found, or insufficient stock
     */
    public Order createOrder(Long userId, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Create Order entity
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingName(request.getShippingName());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZip(request.getShippingZip());
        order.setShippingPhone(request.getShippingPhone());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.COD);
        order.setShippingMethod(request.getShippingMethod() != null ? request.getShippingMethod() : ShippingMethod.STANDARD);
        order.setReturnStatus(ReturnStatus.NONE);
        order.setCurrency("USD");
        
        // Set card payment details if Visa/Card payment
        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            order.setCardHolderName(request.getCardHolderName());
            // Extract last 4 digits from card number if cardLast4 is not already set
            if (request.getCardLast4() != null && !request.getCardLast4().isEmpty()) {
                order.setCardLast4(request.getCardLast4());
            } else if (request.getCardNumber() != null && request.getCardNumber().length() >= 4) {
                String cardNumber = request.getCardNumber().replaceAll("\\s", "");
                order.setCardLast4(cardNumber.substring(cardNumber.length() - 4));
            }
            order.setCardBrand(request.getCardBrand() != null ? request.getCardBrand() : "VISA");
        }
        
        // Create OrderItems from CartItems
        List<OrderItem> orderItems = createOrderItems(order, cartItems);
        order.setItems(orderItems);
        
        return order;
    }
    
    /**
     * Create OrderItems from CartItems
     * 
     * @param order Parent Order entity
     * @param cartItems List of cart items to convert
     * @return List of OrderItems
     */
    private List<OrderItem> createOrderItems(Order order, List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));
            
            // Check stock availability
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            
            // Create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSize(cartItem.getSize());
            orderItem.setColor(cartItem.getColor());
            orderItem.setPriceSnapshot(cartItem.getPriceSnapshot());
            
            orderItems.add(orderItem);
        }
        
        return orderItems;
    }
    
    /**
     * Calculate order subtotal from cart items
     * 
     * @param cartItems List of cart items
     * @return Calculated subtotal
     */
    public BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            subtotal = subtotal.add(
                cartItem.getPriceSnapshot().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }
        return subtotal;
    }
}

