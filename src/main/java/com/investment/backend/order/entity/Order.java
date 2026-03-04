package com.investment.backend.order.entity;

import com.investment.backend.account.entity.Account;
import com.investment.backend.order.enums.OrderStatus;
import com.investment.backend.order.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 20)
    private String stockCode;

    @Column(nullable = false, length = 100)
    private String stockName;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal orderPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Order(Account account, String stockCode, String stockName,
                 BigDecimal orderPrice, BigDecimal quantity, OrderType orderType) {
        this.account = account;
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.orderPrice = orderPrice;
        this.quantity = quantity;
        this.orderType = orderType;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    
    // 주문 체결
    public void fillOrder() {
        this.status = OrderStatus.FILLED;
    }

    // 주문 취소
    public void cancelOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 취소할 수 있습니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // 총 주문 금액 계산
    public BigDecimal getTotalAmount() {
        return orderPrice.multiply(quantity);
    }
}
