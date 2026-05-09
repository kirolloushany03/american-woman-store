package com.americanwomen.store.dto;

import com.americanwomen.store.ocl.OclViolationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Error response for OCL validation failures
 * Returns HTTP 400 with list of violated invariants
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OclErrorResponse {
    private String message;
    private List<OclViolationDto> violations;
}

