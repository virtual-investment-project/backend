package com.investment.backend.history.entity;

import com.investment.backend.account.entity.Account;
import com.investment.backend.history.enums.TradeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_history")
public class AccountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradeType tradeType;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal balanceSnapshot;  //거래 직후 잔액

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AccountHistory(Account account, TradeType tradeType, BigDecimal amount,
                          BigDecimal balanceSnapshot, String description) {
        this.account = account;
        this.tradeType = tradeType;
        this.amount = amount;
        this.balanceSnapshot = balanceSnapshot;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}
