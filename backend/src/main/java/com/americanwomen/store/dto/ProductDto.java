package com.americanwomen.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private String sizes;
    private String colors;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean newArrival;
    private Boolean bestSeller;
    private Boolean onSale;
    private Integer salePercentage;
    private LocalDateTime createdAt;
}



