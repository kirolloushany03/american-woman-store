package com.americanwomen.store.service;

import com.americanwomen.store.dto.ProductDto;
import com.americanwomen.store.entity.Product;
import com.americanwomen.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final com.americanwomen.store.ocl.OclValidationService oclValidationService;

    public ProductService(ProductRepository productRepository,
                         com.americanwomen.store.ocl.OclValidationService oclValidationService) {
        this.productRepository = productRepository;
        this.oclValidationService = oclValidationService;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        String searchTerm = query.trim();
        return productRepository.searchProducts(searchTerm).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<ProductDto> getNewArrivals() {
        return productRepository.findByNewArrivalTrueOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<ProductDto> getBestSellers() {
        return productRepository.findByBestSellerTrueOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<ProductDto> getFlashSale() {
        return productRepository.findByOnSaleTrueOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = toEntity(dto);
        
        // OCL validation before saving
        oclValidationService.validateProduct(product);
        
        product = productRepository.save(product);
        return toDto(product);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Always update all fields - don't skip null/empty values
        // This ensures all changes are persisted
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            product.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription().trim());
        }
        if (dto.getCategory() != null && !dto.getCategory().trim().isEmpty()) {
            product.setCategory(dto.getCategory().trim());
        }
        if (dto.getSellingPrice() != null) {
            product.setSellingPrice(dto.getSellingPrice());
        }
        if (dto.getCostPrice() != null) {
            product.setCostPrice(dto.getCostPrice());
        }
        if (dto.getSizes() != null) {
            product.setSizes(dto.getSizes().trim());
        }
        if (dto.getColors() != null) {
            product.setColors(dto.getColors().trim());
        }
        if (dto.getStockQuantity() != null) {
            product.setStockQuantity(dto.getStockQuantity());
        }
        // Always update imageUrl if provided (even if empty string)
        if (dto.getImageUrl() != null) {
            String normalizedUrl = normalizeImageUrl(dto.getImageUrl());
            product.setImageUrl(normalizedUrl != null && !normalizedUrl.trim().isEmpty() ? normalizedUrl : null);
        }
        if (dto.getNewArrival() != null) {
            product.setNewArrival(dto.getNewArrival());
        }
        if (dto.getBestSeller() != null) {
            product.setBestSeller(dto.getBestSeller());
        }
        if (dto.getOnSale() != null) {
            product.setOnSale(dto.getOnSale());
        }
        if (dto.getSalePercentage() != null) {
            product.setSalePercentage(dto.getSalePercentage());
        }

        // OCL validation before saving
        oclValidationService.validateProduct(product);

        // Always save to ensure changes are persisted
        product = productRepository.save(product);
        return toDto(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getSellingPrice(),
            product.getCostPrice(),
            product.getSizes(),
            product.getColors(),
            product.getStockQuantity(),
            product.getImageUrl(),
            product.getNewArrival(),
            product.getBestSeller(),
            product.getOnSale(),
            product.getSalePercentage(),
            product.getCreatedAt()
        );
    }

    private Product toEntity(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setSellingPrice(dto.getSellingPrice());
        product.setCostPrice(dto.getCostPrice());
        product.setSizes(dto.getSizes());
        product.setColors(dto.getColors());
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        product.setImageUrl(normalizeImageUrl(dto.getImageUrl()));
        product.setNewArrival(dto.getNewArrival() != null ? dto.getNewArrival() : false);
        product.setBestSeller(dto.getBestSeller() != null ? dto.getBestSeller() : false);
        product.setOnSale(dto.getOnSale() != null ? dto.getOnSale() : false);
        product.setSalePercentage(dto.getSalePercentage() != null ? dto.getSalePercentage() : 0);
        return product;
    }

    private String normalizeImageUrl(String url) {
        if (url == null) return null;
        return url.replace("\\", "/").replace("\"", "").trim();
    }
}

