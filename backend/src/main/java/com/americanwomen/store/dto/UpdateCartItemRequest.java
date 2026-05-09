package com.americanwomen.store.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @Min(1)
    private Integer quantity;
    private String size;
    private String color;
}



