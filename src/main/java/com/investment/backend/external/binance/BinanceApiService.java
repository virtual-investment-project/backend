package com.investment.backend.external.binance;

import com.investment.backend.external.binance.dto.BinancePriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceApiService {

    private final RestTemplate restTemplate;
    private static final String BINANCE_PRICE_API_URL = "https://api.binance.com/api/v3/ticker/price";

    /**
     * 여러 심볼의 현재가를 한 번에 조회 (배치 조회)
     * @param symbols 심볼 리스트 (예: ["BTC/USDT", "ETH/USDT"])
     * @return 심볼별 현재가 맵 (Key: 심볼, Value: 가격)
     */
    public Map<String, BigDecimal> getCurrentPrices(Set<String> symbols) {
        Map<String, BigDecimal> priceMap = new HashMap<>();
        
        if (symbols == null || symbols.isEmpty()) {
            return priceMap;
        }
        
        try {
            // 심볼을 바이낸스 형식으로 변환 (BTC/USDT -> BTCUSDT)
            List<String> cleanSymbols = symbols.stream()
                    .map(symbol -> symbol.replace("/", "").toUpperCase())
                    .collect(Collectors.toList());
            
            // 바이낸스 API는 symbols 파라미터로 배열을 받음
            // 예: ?symbols=["BTCUSDT","ETHUSDT"]
            String symbolsParam = "[" + cleanSymbols.stream()
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(",")) + "]";
            
            String url = BINANCE_PRICE_API_URL + "?symbols=" + symbolsParam;
            
            log.debug("바이낸스 배치 API 호출 - 심볼 수: {}, URL: {}", symbols.size(), url);
            
            // 응답은 배열 형태
            BinancePriceResponse[] responses = restTemplate.getForObject(url, BinancePriceResponse[].class);
            
            if (responses != null) {
                for (BinancePriceResponse response : responses) {
                    if (response != null && response.getSymbol() != null && response.getPrice() != null) {
                        // 원래 형식으로 복원 (BTCUSDT -> BTC/USDT)
                        String originalSymbol = findOriginalSymbol(response.getSymbol(), symbols);
                        priceMap.put(originalSymbol, response.getPrice());
                        log.debug("심볼 {} 현재가: {}", originalSymbol, response.getPrice());
                    }
                }
                // log.info("배치 가격 조회 완료 - 조회 심볼: {}개, 성공: {}개", symbols.size(), priceMap.size());
            } else {
                log.warn("배치 가격 조회 응답이 null입니다.");
            }
            
        } catch (Exception e) {
            log.error("바이낸스 배치 API 호출 실패 - 에러: {}", e.getMessage(), e);
        }
        
        return priceMap;
    }

    
    // 클린 심볼(BTCUSDT)로부터 원래 심볼(BTC/USDT) 찾기
    private String findOriginalSymbol(String cleanSymbol, Set<String> originalSymbols) {
        return originalSymbols.stream()
                .filter(s -> s.replace("/", "").equalsIgnoreCase(cleanSymbol))
                .findFirst()
                .orElse(cleanSymbol);
    }
}
