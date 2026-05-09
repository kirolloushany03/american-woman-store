package com.americanwomen.store.dto;

import com.americanwomen.store.entity.PaymentMethod;
import com.americanwomen.store.entity.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotBlank
    private String shippingName;
    @NotBlank
    private String shippingAddress;
    @NotBlank
    private String shippingCity;
    @NotBlank
    private String shippingState;
    @NotBlank
    private String shippingZip;
    @NotBlank
    private String shippingPhone;
    private PaymentMethod paymentMethod = PaymentMethod.COD;
    private ShippingMethod shippingMethod = ShippingMethod.STANDARD;
    
    // Card payment fields (required when paymentMethod is CARD)
    private String cardHolderName;
    private String cardNumber;  // Only used for validation, NEVER stored
    private String cardExpiry;  // MM/YY format, only used for validation
    private String cardCvv;     // Only used for validation, NEVER stored
    private String cardLast4;   // Last 4 digits to store
    private String cardBrand;   // VISA, MASTERCARD, etc.
}

