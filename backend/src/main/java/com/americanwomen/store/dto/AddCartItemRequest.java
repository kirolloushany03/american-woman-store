package com.americanwomen.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull
    private Long productId;
    @Min(1)
    private Integer quantity = 1;
    private String size;
    private String color;
}



