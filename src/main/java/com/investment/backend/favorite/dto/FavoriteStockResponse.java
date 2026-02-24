package com.investment.backend.favorite.dto;

import com.investment.backend.favorite.entity.FavoriteStock;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FavoriteStockResponse {

    private Long id;
    private String symbol;
    private String name;
    private String koreanName;
    private LocalDateTime createdAt;

    public static FavoriteStockResponse from(FavoriteStock favoriteStock) {
        return FavoriteStockResponse.builder()
                .id(favoriteStock.getId())
                .symbol(favoriteStock.getSymbol())
                .name(favoriteStock.getName())
                .koreanName(favoriteStock.getKoreanName())
                .createdAt(favoriteStock.getCreatedAt())
                .build();
    }
}
