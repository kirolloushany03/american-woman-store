package com.americanwomen.store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    private String sizes; // JSON string or comma-separated
    private String colors; // JSON string or comma-separated

    @Min(0)
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    private String imageUrl;

    @Column(nullable = false)
    private Boolean newArrival = false;

    @Column(nullable = false)
    private Boolean bestSeller = false;

    @Column(nullable = false)
    private Boolean onSale = false;

    @Min(0)
    @Max(100)
    private Integer salePercentage = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}



