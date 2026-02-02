package com.investment.backend.external.binance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BinancePriceResponse {
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("priceChange")
    private BigDecimal priceChange;
    
    @JsonProperty("priceChangePercent")
    private BigDecimal priceChangePercent;
    
    @JsonProperty("weightedAvgPrice")
    private BigDecimal weightedAvgPrice;
    
    @JsonProperty("prevClosePrice")
    private BigDecimal prevClosePrice;
    
    @JsonProperty("lastPrice")
    private BigDecimal lastPrice;  // 현재가, 사실상 이것만 사용
    
    @JsonProperty("lastQty")
    private BigDecimal lastQty;
    
    @JsonProperty("bidPrice")
    private BigDecimal bidPrice;
    
    @JsonProperty("bidQty")
    private BigDecimal bidQty;
    
    @JsonProperty("askPrice")
    private BigDecimal askPrice;
    
    @JsonProperty("askQty")
    private BigDecimal askQty;
    
    @JsonProperty("openPrice")
    private BigDecimal openPrice;
    
    @JsonProperty("highPrice")
    private BigDecimal highPrice;
    
    @JsonProperty("lowPrice")
    private BigDecimal lowPrice;
    
    @JsonProperty("volume")
    private BigDecimal volume;
    
    @JsonProperty("quoteVolume")
    private BigDecimal quoteVolume;
    
    @JsonProperty("openTime")
    private Long openTime;
    
    @JsonProperty("closeTime")
    private Long closeTime;
    
    @JsonProperty("firstId")
    private Long firstId;
    
    @JsonProperty("lastId")
    private Long lastId;
    
    @JsonProperty("count")
    private Long count;
}
