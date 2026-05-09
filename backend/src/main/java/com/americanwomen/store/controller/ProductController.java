package com.americanwomen.store.controller;

import com.americanwomen.store.dto.ProductDto;
import com.americanwomen.store.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(@RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(productService.searchProducts(q));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/new")
    public ResponseEntity<List<ProductDto>> getNewArrivals() {
        return ResponseEntity.ok(productService.getNewArrivals());
    }

    @GetMapping("/bestsellers")
    public ResponseEntity<List<ProductDto>> getBestSellers() {
        return ResponseEntity.ok(productService.getBestSellers());
    }

    @GetMapping("/flash-sale")
    public ResponseEntity<List<ProductDto>> getFlashSale() {
        return ResponseEntity.ok(productService.getFlashSale());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}

