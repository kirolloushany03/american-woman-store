package com.americanwomen.store.controller;

import com.americanwomen.store.dto.*;
import com.americanwomen.store.service.OrderService;
import com.americanwomen.store.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(orderService.checkout(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderSummaryDto>> getMyOrders() {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    @PostMapping("/{id}/confirm-delivery")
    public ResponseEntity<MessageResponse> confirmDelivery(@PathVariable Long id) {
        try {
            Long userId = userService.getCurrentUserId();
            System.out.println("[OrderController] confirmDelivery: orderId=" + id + ", userId=" + userId);
            orderService.confirmDelivery(id, userId);
            return ResponseEntity.ok(new MessageResponse("Order delivery confirmed successfully"));
        } catch (RuntimeException e) {
            String message = e.getMessage();
            System.out.println("[OrderController] confirmDelivery error: " + message);
            // Return 404 for not found, 403 for access denied, 400 for other validation issues
            if (message != null && message.contains("Order not found")) {
                return ResponseEntity.status(404).body(new MessageResponse(message));
            } else if (message != null && message.contains("Access denied")) {
                return ResponseEntity.status(403).body(new MessageResponse(message));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse(message));
            }
        }
    }

    @PostMapping("/{id}/return-request")
    public ResponseEntity<MessageResponse> requestReturn(@PathVariable Long id, @Valid @RequestBody ReturnRequestDto request) {
        try {
            Long userId = userService.getCurrentUserId();
            System.out.println("[OrderController] requestReturn: orderId=" + id + ", userId=" + userId);
            orderService.requestReturn(id, userId, request.getReason());
            return ResponseEntity.ok(new MessageResponse("Return request submitted successfully"));
        } catch (RuntimeException e) {
            String message = e.getMessage();
            System.out.println("[OrderController] requestReturn error: " + message);
            // Return 404 for not found, 403 for access denied, 400 for other validation issues
            if (message != null && message.contains("Order not found")) {
                return ResponseEntity.status(404).body(new MessageResponse(message));
            } else if (message != null && message.contains("Access denied")) {
                return ResponseEntity.status(403).body(new MessageResponse(message));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse(message));
            }
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<MessageResponse> confirmOrder(@PathVariable Long id) {
        orderService.confirmOrder(id);
        return ResponseEntity.ok(new MessageResponse("Order confirmed"));
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<MessageResponse> shipOrder(@PathVariable Long id) {
        orderService.shipOrder(id);
        return ResponseEntity.ok(new MessageResponse("Order shipped"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<MessageResponse> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(new MessageResponse("Order cancelled"));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<MessageResponse> completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id);
        return ResponseEntity.ok(new MessageResponse("Order completed"));
    }

    @PutMapping("/{id}/{status}")
    public ResponseEntity<MessageResponse> updateStatus(
            @PathVariable Long id,
            @PathVariable String status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.ok(new MessageResponse("Order status updated to " + status));
    }
}

