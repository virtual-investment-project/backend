package com.investment.backend.holdings.entity;

import com.investment.backend.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock_holdings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "stock_code"}))
public class StockHoldings {

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
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal averagePrice;

    @Column(precision = 20, scale = 8)
    private BigDecimal currentPrice;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public StockHoldings(Account account, String stockCode, String stockName,
                         BigDecimal quantity, BigDecimal averagePrice) {
        this.account = account;
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.updatedAt = LocalDateTime.now();
    }

    
    // 매수: 수량 증가 및 평단가 재계산
    public void addPosition(BigDecimal addQuantity, BigDecimal buyPrice) {
        BigDecimal totalCost = this.quantity.multiply(this.averagePrice)
                .add(addQuantity.multiply(buyPrice));
        this.quantity = this.quantity.add(addQuantity);
        this.averagePrice = totalCost.divide(this.quantity, 8, RoundingMode.HALF_UP);
        this.updatedAt = LocalDateTime.now();
    }

    
    // 매도: 수량 감소
    public void reducePosition(BigDecimal sellQuantity) {
        if (this.quantity.compareTo(sellQuantity) < 0) {
            throw new IllegalArgumentException("보유 수량이 부족합니다.");
        }
        this.quantity = this.quantity.subtract(sellQuantity);
        this.updatedAt = LocalDateTime.now();
    }

    
    // 보유 수량이 0인지 확인
    public boolean isEmpty() {
        return this.quantity.compareTo(BigDecimal.ZERO) == 0;
    }

    
    // 현재가 업데이트
    public void updateCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        this.updatedAt = LocalDateTime.now();
    }
}
