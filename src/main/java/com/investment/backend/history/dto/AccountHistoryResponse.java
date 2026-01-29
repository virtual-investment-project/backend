package com.investment.backend.history.dto;

import com.investment.backend.history.entity.AccountHistory;
import com.investment.backend.history.enums.TradeType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AccountHistoryResponse {

    private UUID id;
    private UUID accountId;
    private TradeType tradeType;
    private BigDecimal amount;
    private BigDecimal balanceSnapshot;
    private String description;
    private LocalDateTime createdAt;

    public static AccountHistoryResponse from(AccountHistory history) {
        return AccountHistoryResponse.builder()
                .id(history.getId())
                .accountId(history.getAccount().getId())
                .tradeType(history.getTradeType())
                .amount(history.getAmount())
                .balanceSnapshot(history.getBalanceSnapshot())
                .description(history.getDescription())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
