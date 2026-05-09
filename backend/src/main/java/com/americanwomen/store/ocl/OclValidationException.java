package com.americanwomen.store.ocl;

import java.util.List;

/**
 * Exception thrown when OCL validation fails.
 * Returns HTTP 400 (Bad Request) with validation details.
 */
public class OclValidationException extends RuntimeException {
    private final List<OclViolationDto> violations;

    public OclValidationException(String message, List<OclViolationDto> violations) {
        super(message);
        this.violations = violations;
    }

    public List<OclViolationDto> getViolations() {
        return violations;
    }
}

