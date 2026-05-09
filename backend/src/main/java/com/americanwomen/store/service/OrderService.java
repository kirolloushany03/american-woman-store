package com.americanwomen.store.service;

import com.americanwomen.store.dto.CheckoutRequest;
import com.americanwomen.store.dto.CheckoutResponse;
import com.americanwomen.store.dto.OrderSummaryDto;
import com.americanwomen.store.dto.ReturnRequestDto;
import com.americanwomen.store.entity.*;
import com.americanwomen.store.entity.ShippingMethod;
import com.americanwomen.store.pattern.factory.OrderFactory;
import com.americanwomen.store.pattern.payment.PaymentStrategyFactory;
import com.americanwomen.store.pattern.shipping.ShippingStrategyFactory;
import com.americanwomen.store.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderFactory orderFactory;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final ShippingStrategyFactory shippingStrategyFactory;
    private final com.americanwomen.store.ocl.OclValidationService oclValidationService;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                       ProductRepository productRepository, OrderFactory orderFactory,
                       PaymentStrategyFactory paymentStrategyFactory,
                       ShippingStrategyFactory shippingStrategyFactory,
                       com.americanwomen.store.ocl.OclValidationService oclValidationService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderFactory = orderFactory;
        this.paymentStrategyFactory = paymentStrategyFactory;
        this.shippingStrategyFactory = shippingStrategyFactory;
        this.oclValidationService = oclValidationService;
    }

    @Transactional
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        // Validate card information if payment method is CARD
        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            validateCardPayment(request);
            // Extract last 4 digits from card number
            if (request.getCardNumber() != null && request.getCardNumber().length() >= 4) {
                String cardNumber = request.getCardNumber().replaceAll("\\s", "");
                request.setCardLast4(cardNumber.substring(cardNumber.length() - 4));
            }
        }

        // Use Factory Pattern to create Order (replaces manual entity construction)
        Order order = orderFactory.createOrder(userId, request);

        // Calculate subtotal using factory method
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        BigDecimal subtotal = orderFactory.calculateSubtotal(cartItems);

        // Use Strategy Pattern for shipping calculation
        ShippingMethod shippingMethod = order.getShippingMethod();
        var shippingStrategy = shippingStrategyFactory.getStrategy(shippingMethod);
        BigDecimal shippingFee = shippingStrategy.calculateShippingFee(subtotal);

        // Set calculated amounts
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotal(subtotal.add(shippingFee));

        // OCL validation before saving (validates Order + OrderItems)
        oclValidationService.validateOrder(order, order.getItems());

        // Save order
        order = orderRepository.save(order);

        // Process payment using Strategy Pattern
        PaymentMethod paymentMethod = order.getPaymentMethod();
        var paymentStrategy = paymentStrategyFactory.getStrategy(paymentMethod);
        boolean paymentSuccess = paymentStrategy.processPayment(order);
        
        if (!paymentSuccess) {
            throw new RuntimeException("Payment processing failed");
        }

        // Update order after payment processing
        order = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteByUserId(userId);

        return new CheckoutResponse(
            order.getId(), 
            order.getSubtotal(),
            order.getShippingFee(),
            order.getTotal(), 
            order.getCurrency(), 
            order.getStatus().name()
        );
    }

    /**
     * Validate card payment information
     * Security: Never store full card number or CVV
     */
    private void validateCardPayment(CheckoutRequest request) {
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Card holder name is required");
        }
        
        if (request.getCardNumber() == null || request.getCardNumber().replaceAll("\\s", "").length() < 12) {
            throw new IllegalArgumentException("Card number must be at least 12 digits");
        }
        
        if (request.getCardExpiry() == null || !request.getCardExpiry().matches("\\d{2}/\\d{2}")) {
            throw new IllegalArgumentException("Card expiry must be in MM/YY format");
        }
        
        if (request.getCardCvv() == null || request.getCardCvv().length() < 3 || request.getCardCvv().length() > 4) {
            throw new IllegalArgumentException("CVV must be 3-4 digits");
        }
    }

    public List<OrderSummaryDto> getMyOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
            .map(OrderSummaryDto::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Validate status transition using OCL (Source of Truth)
        OrderStatus currentStatus = order.getStatus();
        
        // If changing to DELIVERED, ensure it's from SHIPPED (OCL: confirmDeliveryOnlyWhenShipped)
        if (newStatus == OrderStatus.DELIVERED && currentStatus != OrderStatus.SHIPPED) {
            // Create temporary order object for OCL validation
            Order tempOrder = new Order();
            tempOrder.setStatus(OrderStatus.DELIVERED);
            oclValidationService.validateConfirmDeliveryAllowed(tempOrder);
        }
        
        // If changing to CANCELLED, validate OCL: confirmedOrdersCannotBeCancelled
        if (newStatus == OrderStatus.CANCELLED) {
            Order tempOrder = new Order();
            tempOrder.setStatus(newStatus);
            // Check if current status is CONFIRMED, SHIPPED, or DELIVERED
            if (currentStatus == OrderStatus.CONFIRMED || 
                currentStatus == OrderStatus.SHIPPED || 
                currentStatus == OrderStatus.DELIVERED) {
                // This violates confirmedOrdersCannotBeCancelled
                List<com.americanwomen.store.ocl.OclViolationDto> violations = new ArrayList<>();
                violations.add(new com.americanwomen.store.ocl.OclViolationDto(
                    "confirmedOrdersCannotBeCancelled",
                    "status",
                    "confirmed/shipped/delivered orders cannot be cancelled"
                ));
                throw new com.americanwomen.store.ocl.OclValidationException(
                    "OCL validation failed for Order status update", violations);
            }
        }
        
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            // Always set deliveredAt when status changes to DELIVERED
            if (order.getDeliveredAt() == null) {
                order.setDeliveredAt(LocalDateTime.now());
                System.out.println("[OrderService] Set deliveredAt for order " + orderId + " to " + order.getDeliveredAt());
            }
        }
        orderRepository.save(order);
        System.out.println("[OrderService] Order " + orderId + " status updated from " + currentStatus + " to " + newStatus);
    }

    @Transactional
    public void updateStatus(Long orderId, String rawStatus) {
        String status = rawStatus.toUpperCase().trim();
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        OrderStatus newStatus;
        switch (status) {
            case "CONFIRMED":
                if (order.getStatus() != OrderStatus.PENDING) {
                    throw new RuntimeException("Only pending orders can be confirmed");
                }
                newStatus = OrderStatus.CONFIRMED;
                break;
            case "SHIPPING":
            case "IN_SHIPPING":
            case "SHIPPED":
                if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PENDING) {
                    throw new RuntimeException("Only confirmed or pending orders can be shipped");
                }
                newStatus = OrderStatus.SHIPPED;
                break;
            case "CANCELLED":
            case "CANCELED":
                if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
                    throw new RuntimeException("Cannot cancel a delivered or already cancelled order");
                }
                newStatus = OrderStatus.CANCELLED;
                break;
            case "COMPLETED":
            case "DELIVERED":
                if (order.getStatus() != OrderStatus.SHIPPED) {
                    throw new RuntimeException("Only shipped orders can be completed");
                }
                newStatus = OrderStatus.DELIVERED;
                order.setDeliveredAt(LocalDateTime.now());
                break;
            default:
                throw new IllegalArgumentException("Unsupported status: " + status);
        }
        
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be confirmed");
        }
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only confirmed or pending orders can be shipped");
        }
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel a delivered or already cancelled order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Only shipped orders can be completed");
        }
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public void confirmDelivery(Long orderId, Long userId) {
        System.out.println("[OrderService] confirmDelivery called: orderId=" + orderId + ", userId=" + userId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> {
                System.out.println("[OrderService] ❌ Order not found: " + orderId);
                return new RuntimeException("Order not found: " + orderId);
            });
        
        System.out.println("[OrderService] Order found: id=" + order.getId() + ", orderUserId=" + order.getUserId() + ", currentUserId=" + userId + ", status=" + order.getStatus());
        
        // Ownership check - must be strict
        if (order.getUserId() == null) {
            System.out.println("[OrderService] ❌ Access denied: Order " + orderId + " has null userId");
            throw new RuntimeException("Access denied: Order does not belong to user");
        }
        if (!order.getUserId().equals(userId)) {
            System.out.println("[OrderService] ❌ Access denied: Order " + orderId + " belongs to user " + order.getUserId() + " but current user is " + userId);
            throw new RuntimeException("Access denied: Order does not belong to user");
        }
        
        // OCL validation: confirmDeliveryOnlyWhenShipped (Source of Truth)
        oclValidationService.validateConfirmDeliveryAllowed(order);
        
        System.out.println("[OrderService] ✅ Ownership and OCL validation passed. Confirming delivery for order " + orderId);
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
        System.out.println("[OrderService] ✅ Delivery confirmed successfully");
    }

    @Transactional
    public void requestReturn(Long orderId, Long userId, String reason) {
        System.out.println("[OrderService] requestReturn called: orderId=" + orderId + ", userId=" + userId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> {
                System.out.println("[OrderService] Order not found: " + orderId);
                return new RuntimeException("Order not found: " + orderId);
            });
        
        System.out.println("[OrderService] Order found: id=" + order.getId() + ", orderUserId=" + order.getUserId() + ", status=" + order.getStatus() + ", returnStatus=" + order.getReturnStatus());
        
        // Ownership check - must be strict
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            System.out.println("[OrderService] ❌ Access denied: Order " + orderId + " belongs to user " + order.getUserId() + " but current user is " + userId);
            throw new RuntimeException("Access denied: Order does not belong to user");
        }
        
        if (order.getStatus() != OrderStatus.DELIVERED) {
            System.out.println("[OrderService] ❌ Invalid status: Order " + orderId + " is " + order.getStatus() + ", must be DELIVERED");
            throw new RuntimeException("Order must be DELIVERED to request return. Current status: " + order.getStatus());
        }
        
        if (order.getDeliveredAt() == null) {
            System.out.println("[OrderService] ❌ Delivery date not set for order " + orderId);
            throw new RuntimeException("Order delivery date is not set");
        }
        
        if (order.getDeliveredAt().plusDays(14).isBefore(LocalDateTime.now())) {
            System.out.println("[OrderService] ❌ Return window expired for order " + orderId);
            throw new RuntimeException("Return request must be within 14 days of delivery");
        }
        
        if (order.getReturnStatus() != ReturnStatus.NONE && order.getReturnStatus() != ReturnStatus.REJECTED) {
            System.out.println("[OrderService] ❌ Return already requested: order " + orderId + " has returnStatus " + order.getReturnStatus());
            throw new RuntimeException("Return already requested or processed. Current return status: " + order.getReturnStatus());
        }
        
        System.out.println("[OrderService] ✅ Processing return request for order " + orderId);
        order.setReturnStatus(ReturnStatus.REQUESTED);
        order.setReturnReason(reason);
        order.setReturnRequestedAt(LocalDateTime.now());
        
        // OCL validation for return rule
        oclValidationService.validateReturnAllowed(order);
        
        orderRepository.save(order);
    }

    @Transactional
    public void approveReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // OCL validation: return must be in REQUESTED status
        if (order.getReturnStatus() != ReturnStatus.REQUESTED) {
            List<com.americanwomen.store.ocl.OclViolationDto> violations = new ArrayList<>();
            violations.add(new com.americanwomen.store.ocl.OclViolationDto(
                "returnApprovalOnlyWhenRequested",
                "returnStatus",
                "return can only be approved when returnStatus is REQUESTED"
            ));
            throw new com.americanwomen.store.ocl.OclValidationException(
                "OCL validation failed for Return Approval", violations);
        }
        
        order.setReturnStatus(ReturnStatus.APPROVED);
        order.setStatus(OrderStatus.CANCELLED);
        
        // OCL validation: after setting status to CANCELLED, validate confirmedOrdersCannotBeCancelled
        // But since we're cancelling a DELIVERED order (which had a return request), this is allowed
        // The OCL constraint applies to preventing cancellation of active orders, not returns
        orderRepository.save(order);
    }

    @Transactional
    public void rejectReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // OCL validation: return must be in REQUESTED status
        if (order.getReturnStatus() != ReturnStatus.REQUESTED) {
            List<com.americanwomen.store.ocl.OclViolationDto> violations = new ArrayList<>();
            violations.add(new com.americanwomen.store.ocl.OclViolationDto(
                "returnRejectionOnlyWhenRequested",
                "returnStatus",
                "return can only be rejected when returnStatus is REQUESTED"
            ));
            throw new com.americanwomen.store.ocl.OclValidationException(
                "OCL validation failed for Return Rejection", violations);
        }
        
        order.setReturnStatus(ReturnStatus.REJECTED);
        orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByReturnStatus(ReturnStatus returnStatus) {
        return orderRepository.findByReturnStatusOrderByCreatedAtDesc(returnStatus);
    }
}

