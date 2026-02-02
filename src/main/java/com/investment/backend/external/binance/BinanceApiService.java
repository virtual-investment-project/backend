package com.investment.backend.external.binance;

import com.investment.backend.external.binance.dto.BinancePriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceApiService {

    private final RestTemplate restTemplate;
    private static final String BINANCE_API_URL = "https://api.binance.com/api/v3/ticker/24hr";

    /**
     * 바이낸스에서 특정 심볼의 현재가 조회
     * @param symbol 심볼 (예: BTCUSDT)
     * @return 현재가
     */
    public BigDecimal getCurrentPrice(String symbol) {
        try {
            String cleanSymbol = symbol.replace("/", "").toUpperCase();
            String url = BINANCE_API_URL + "?symbol=" + cleanSymbol;
            
            log.debug("바이낸스 API 호출: {}", url);
            
            BinancePriceResponse response = restTemplate.getForObject(url, BinancePriceResponse.class);
            
            if (response != null && response.getLastPrice() != null) {
                log.debug("심볼 {} 현재가: {}", symbol, response.getLastPrice());
                return response.getLastPrice();
            }
            
            log.warn("심볼 {}의 가격 정보를 가져올 수 없습니다.", symbol);
            return null;
            
        } catch (Exception e) {
            log.error("바이낸스 API 호출 실패 - 심볼: {}, 에러: {}", symbol, e.getMessage());
            return null;
        }
    }
}
