package com.investment.backend.order.dto;

import com.investment.backend.order.entity.Order;
import com.investment.backend.order.enums.OrderStatus;
import com.investment.backend.order.enums.OrderType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrderResponse {

    private UUID id;
    private UUID accountId;
    private String stockCode;
    private String stockName;
    private BigDecimal orderPrice;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private OrderType orderType;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .accountId(order.getAccount().getId())
                .stockCode(order.getStockCode())
                .stockName(order.getStockName())
                .orderPrice(order.getOrderPrice())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
