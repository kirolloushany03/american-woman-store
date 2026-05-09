package com.americanwomen.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String size;
    private String color;
    private BigDecimal priceSnapshot;
    private ProductDto product;
}



