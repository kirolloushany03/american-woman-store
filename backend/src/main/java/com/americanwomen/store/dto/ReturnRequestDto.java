package com.americanwomen.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReturnRequestDto {
    @NotBlank(message = "Return reason is required")
    private String reason;
}



