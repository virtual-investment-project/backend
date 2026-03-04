package com.investment.backend.favorite.entity;

import com.investment.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "favorite_stock", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "symbol" }))
public class FavoriteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String koreanName;

    @Column(precision = 20, scale = 8)
    private BigDecimal lastAlertPrice;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public FavoriteStock(User user, String symbol, String name, String koreanName) {
        this.user = user;
        this.symbol = symbol;
        this.name = name;
        this.koreanName = koreanName;
        this.lastAlertPrice = null;
        this.createdAt = LocalDateTime.now();
    }

    // 마지막 알림 가격 업데이트
    public void updateLastAlertPrice(BigDecimal price) {
        this.lastAlertPrice = price;
    }
}
