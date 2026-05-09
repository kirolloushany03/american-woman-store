package com.americanwomen.store.service;

import com.americanwomen.store.dto.*;
import com.americanwomen.store.entity.CartItem;
import com.americanwomen.store.entity.Product;
import com.americanwomen.store.repository.CartItemRepository;
import com.americanwomen.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserService userService) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    public List<CartItemDto> getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream().map(item -> {
            Product product = productRepository.findById(item.getProductId())
                .orElse(null);
            CartItemDto dto = new CartItemDto(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getSize(),
                item.getColor(),
                item.getPriceSnapshot(),
                null
            );
            if (product != null) {
                dto.setProduct(new ProductDto(
                    product.getId(), product.getName(), product.getDescription(),
                    product.getCategory(), product.getSellingPrice(), product.getCostPrice(),
                    product.getSizes(), product.getColors(), product.getStockQuantity(),
                    product.getImageUrl(), product.getNewArrival(), product.getBestSeller(),
                    product.getOnSale(), product.getSalePercentage(), product.getCreatedAt()
                ));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public CartItemDto addItem(Long userId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        CartItem existing = cartItemRepository
            .findByUserIdAndProductIdAndSizeAndColor(userId, request.getProductId(),
                request.getSize(), request.getColor())
            .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            existing = cartItemRepository.save(existing);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setSize(request.getSize());
            item.setColor(request.getColor());
            item.setPriceSnapshot(product.getSellingPrice());
            existing = cartItemRepository.save(item);
        }

        CartItemDto dto = new CartItemDto(
            existing.getId(), existing.getProductId(), existing.getQuantity(),
            existing.getSize(), existing.getColor(), existing.getPriceSnapshot(), null
        );
        dto.setProduct(new ProductDto(
            product.getId(), product.getName(), product.getDescription(),
            product.getCategory(), product.getSellingPrice(), product.getCostPrice(),
            product.getSizes(), product.getColors(), product.getStockQuantity(),
            product.getImageUrl(), product.getNewArrival(), product.getBestSeller(),
            product.getOnSale(), product.getSalePercentage(), product.getCreatedAt()
        ));
        return dto;
    }

    @Transactional
    public CartItemDto updateItem(Long id, Long userId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.getQuantity() != null) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
            if (product.getStockQuantity() < request.getQuantity()) {
                throw new RuntimeException("Insufficient stock");
            }
            item.setQuantity(request.getQuantity());
        }
        if (request.getSize() != null) item.setSize(request.getSize());
        if (request.getColor() != null) item.setColor(request.getColor());

        item = cartItemRepository.save(item);

        Product product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItemDto dto = new CartItemDto(
            item.getId(), item.getProductId(), item.getQuantity(),
            item.getSize(), item.getColor(), item.getPriceSnapshot(), null
        );
        dto.setProduct(new ProductDto(
            product.getId(), product.getName(), product.getDescription(),
            product.getCategory(), product.getSellingPrice(), product.getCostPrice(),
            product.getSizes(), product.getColors(), product.getStockQuantity(),
            product.getImageUrl(), product.getNewArrival(), product.getBestSeller(),
            product.getOnSale(), product.getSalePercentage(), product.getCreatedAt()
        ));
        return dto;
    }

    @Transactional
    public void removeItem(Long id, Long userId) {
        CartItem item = cartItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        cartItemRepository.deleteById(id);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}

