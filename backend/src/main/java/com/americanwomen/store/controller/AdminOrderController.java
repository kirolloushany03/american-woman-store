package com.americanwomen.store.controller;

import com.americanwomen.store.dto.AdminOrderDto;
import com.americanwomen.store.dto.MessageResponse;
import com.americanwomen.store.entity.Order;
import com.americanwomen.store.entity.OrderStatus;
import com.americanwomen.store.entity.ReturnStatus;
import com.americanwomen.store.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<AdminOrderDto>> getAllOrders(
            @RequestParam(required = false) ReturnStatus returnStatus) {
        List<Order> orders;
        if (returnStatus != null) {
            orders = orderService.getOrdersByReturnStatus(returnStatus);
        } else {
            orders = orderService.getAllOrders();
        }
        List<AdminOrderDto> orderDtos = orders.stream()
            .map(AdminOrderDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(orderDtos);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            orderService.updateStatus(id, newStatus);
            return ResponseEntity.ok(new MessageResponse("Order status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid status: " + request.get("status")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/return/approve")
    public ResponseEntity<MessageResponse> approveReturn(@PathVariable Long id) {
        try {
            orderService.approveReturn(id);
            return ResponseEntity.ok(new MessageResponse("Return approved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/return/reject")
    public ResponseEntity<MessageResponse> rejectReturn(@PathVariable Long id) {
        try {
            orderService.rejectReturn(id);
            return ResponseEntity.ok(new MessageResponse("Return rejected"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}

