package com.investment.backend.favorite.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddFavoriteRequest {

    private String symbol;
    private String name;
    private String koreanName;
}
