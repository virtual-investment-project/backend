package com.investment.backend.holdings.dto;

import com.investment.backend.holdings.entity.StockHoldings;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class StockHoldingsResponse {

    private UUID id;
    private UUID accountId;
    private String stockCode;
    private String stockName;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private LocalDateTime updatedAt;

    public static StockHoldingsResponse from(StockHoldings holdings) {
        return StockHoldingsResponse.builder()
                .id(holdings.getId())
                .accountId(holdings.getAccount().getId())
                .stockCode(holdings.getStockCode())
                .stockName(holdings.getStockName())
                .quantity(holdings.getQuantity())
                .averagePrice(holdings.getAveragePrice())
                .currentPrice(holdings.getCurrentPrice())
                .updatedAt(holdings.getUpdatedAt())
                .build();
    }
}
