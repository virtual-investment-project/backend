package com.investment.backend.favorite.scheduler;

import com.investment.backend.external.binance.BinanceApiService;
import com.investment.backend.favorite.entity.FavoriteStock;
import com.investment.backend.favorite.repository.FavoriteStockRepository;
import com.investment.backend.notification.enums.NotificationType;
import com.investment.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceAlertScheduler {

    private final FavoriteStockRepository favoriteStockRepository;
    private final BinanceApiService binanceApiService;
    private final NotificationService notificationService;

    // ±5% 이상 변동 시 알림
    private static final BigDecimal ALERT_THRESHOLD = new BigDecimal("5.0");

    // 5분마다 관심 종목 가격 확인
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkPriceAlerts() {
        List<FavoriteStock> allFavorites = favoriteStockRepository.findAll();

        if (allFavorites.isEmpty()) {
            return;
        }

        // 유니크 심볼 수집
        Set<String> symbols = allFavorites.stream()
                .map(FavoriteStock::getSymbol)
                .collect(Collectors.toSet());

        // 심볼에서 "BINANCE:" 접두사 제거 후 Binance API 호출
        Set<String> cleanSymbols = symbols.stream()
                .map(s -> s.contains(":") ? s.split(":")[1] : s)
                .map(s -> {
                    // BTCUSDT 형식이면 BTC/USDT로 변환
                    if (s.endsWith("USDT")) {
                        return s.replace("USDT", "/USDT");
                    }
                    return s;
                })
                .collect(Collectors.toSet());

        log.debug("가격 알림 스케줄러 - 심볼 수: {}", cleanSymbols.size());

        Map<String, BigDecimal> priceMap = binanceApiService.getCurrentPrices(cleanSymbols);

        if (priceMap.isEmpty()) {
            log.warn("가격 조회 실패");
            return;
        }

        // 각 즐겨찾기에 대해 가격 변동 확인
        for (FavoriteStock favorite : allFavorites) {
            try {
                processPriceAlert(favorite, priceMap);
            } catch (Exception e) {
                log.error("가격 알림 처리 실패 - 심볼: {}, 에러: {}", favorite.getSymbol(), e.getMessage());
            }
        }
    }

    private void processPriceAlert(FavoriteStock favorite, Map<String, BigDecimal> priceMap) {
        // 심볼 매칭 (BINANCE:BTCUSDT → BTC/USDT)
        String rawSymbol = favorite.getSymbol();
        String cleanSymbol = rawSymbol.contains(":") ? rawSymbol.split(":")[1] : rawSymbol;
        if (cleanSymbol.endsWith("USDT")) {
            cleanSymbol = cleanSymbol.replace("USDT", "/USDT");
        }

        BigDecimal currentPrice = priceMap.get(cleanSymbol);
        if (currentPrice == null) {
            return;
        }

        // lastAlertPrice가 없으면 현재가를 기준으로 설정
        if (favorite.getLastAlertPrice() == null) {
            favorite.updateLastAlertPrice(currentPrice);
            return;
        }

        // 변동률 계산
        BigDecimal lastPrice = favorite.getLastAlertPrice();
        BigDecimal changePercent = currentPrice.subtract(lastPrice)
                .divide(lastPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        // ±5% 임계값 확인
        if (changePercent.abs().compareTo(ALERT_THRESHOLD) >= 0) {
            String direction = changePercent.compareTo(BigDecimal.ZERO) > 0 ? "상승" : "하락";
            String emoji = changePercent.compareTo(BigDecimal.ZERO) > 0 ? "📈" : "⚠️";
            String displayName = favorite.getKoreanName() != null ? favorite.getKoreanName() : favorite.getName();

            String title = String.format("%s %s %s 알림", emoji, displayName, direction);
            String message = String.format("%s이(가) %s%.1f%% %s했습니다. (현재가: $%s)",
                    displayName,
                    changePercent.compareTo(BigDecimal.ZERO) > 0 ? "+" : "",
                    changePercent.doubleValue(),
                    direction,
                    currentPrice.stripTrailingZeros().toPlainString());

            notificationService.createNotification(
                    favorite.getUser(), NotificationType.PRICE_ALERT, title, message, null);

            // 알림 기준 가격 갱신
            favorite.updateLastAlertPrice(currentPrice);

            log.info("가격 알림 발송 - 심볼: {}, 변동률: {}%, 현재가: {}",
                    favorite.getSymbol(), changePercent, currentPrice);
        }
    }
}
