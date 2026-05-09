package com.americanwomen.store.ocl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single OCL invariant violation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OclViolationDto {
    /**
     * Name of the violated invariant (as defined in store.ocl)
     * Example: "usernameNotEmpty", "sellingPricePositive"
     */
    private String invariantName;
    
    /**
     * Field name that caused the violation (mapped from invariant)
     * Example: "username", "sellingPrice", "items"
     */
    private String field;
    
    /**
     * Human-readable violation message
     * Example: "username must not be empty", "sellingPrice must be greater than 0"
     */
    private String message;
}

