package com.americanwomen.store.dto;

import com.americanwomen.store.entity.Order;
import com.americanwomen.store.entity.OrderStatus;
import com.americanwomen.store.entity.PaymentMethod;
import com.americanwomen.store.entity.ReturnStatus;
import com.americanwomen.store.entity.ShippingMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDto {
    private Long id;
    private Long userId;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String currency;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private ShippingMethod shippingMethod;
    private ReturnStatus returnStatus;
    private String shippingName;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingPhone;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private String returnReason;
    private LocalDateTime returnRequestedAt;
    private String cardHolderName;
    private String cardLast4;
    private String cardBrand;

    public static AdminOrderDto from(Order order) {
        AdminOrderDto dto = new AdminOrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingFee(order.getShippingFee());
        dto.setTotal(order.getTotal());
        dto.setCurrency(order.getCurrency());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setShippingMethod(order.getShippingMethod());
        dto.setReturnStatus(order.getReturnStatus());
        dto.setShippingName(order.getShippingName());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingState(order.getShippingState());
        dto.setShippingZip(order.getShippingZip());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setReturnReason(order.getReturnReason());
        dto.setReturnRequestedAt(order.getReturnRequestedAt());
        dto.setCardHolderName(order.getCardHolderName());
        dto.setCardLast4(order.getCardLast4());
        dto.setCardBrand(order.getCardBrand());
        return dto;
    }
}

