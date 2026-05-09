package com.americanwomen.store.controller;

import com.americanwomen.store.dto.*;
import com.americanwomen.store.service.CartService;
import com.americanwomen.store.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCart() {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping
    public ResponseEntity<CartItemDto> addItem(@Valid @RequestBody AddCartItemRequest request) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItemDto> updateItem(@PathVariable Long id, @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(cartService.updateItem(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        cartService.removeItem(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Long userId = userService.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}



