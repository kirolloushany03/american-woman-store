package com.americanwomen.store.dto;

import com.americanwomen.store.entity.Order;
import com.americanwomen.store.entity.OrderStatus;
import com.americanwomen.store.entity.PaymentMethod;
import com.americanwomen.store.entity.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    private Long id;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String currency;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private ReturnStatus returnStatus;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private Boolean canRequestReturn;

    public static OrderSummaryDto from(Order order) {
        OrderSummaryDto dto = new OrderSummaryDto();
        dto.setId(order.getId());
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingFee(order.getShippingFee());
        dto.setTotal(order.getTotal());
        dto.setCurrency(order.getCurrency());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setReturnStatus(order.getReturnStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        
        // Check if return can be requested (DELIVERED, within 14 days, returnStatus is NONE or REJECTED)
        boolean canReturn = order.getStatus() == OrderStatus.DELIVERED
            && (order.getReturnStatus() == ReturnStatus.NONE || order.getReturnStatus() == ReturnStatus.REJECTED)
            && order.getDeliveredAt() != null
            && order.getDeliveredAt().plusDays(14).isAfter(LocalDateTime.now());
        dto.setCanRequestReturn(canReturn);
        
        return dto;
    }
}

