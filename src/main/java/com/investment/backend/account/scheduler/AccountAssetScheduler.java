package com.investment.backend.account.scheduler;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.account.service.AccountService;
import com.investment.backend.external.binance.BinanceApiService;
import com.investment.backend.holdings.dto.StockHoldingsResponse;
import com.investment.backend.holdings.service.StockHoldingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountAssetScheduler {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final StockHoldingsService stockHoldingsService;
    private final BinanceApiService binanceApiService;

    
    // 1초마다 보유 종목 가격 업데이트 후 모든 계좌의 총 자산 업데이트 
    @Scheduled(fixedRate = 1000)  // 1초 마다 실행
    public void updateAllAccountAssets() {
        try {
            // 모든 계좌 조회
            List<Account> accounts = accountRepository.findAll();
            
            if (accounts.isEmpty()) {
                return;
            }
            
            log.info("계좌 총 자산 업데이트 스케줄러 실행 - 계좌 수: {}개", accounts.size());
            
            // 1. 모든 계좌의 보유 주식에서 유니크한 심볼 수집
            Set<String> allSymbols = new HashSet<>();
            for (Account account : accounts) {
                try {
                    List<StockHoldingsResponse> holdings = stockHoldingsService.getHoldingsByAccount(account.getId());
                    holdings.forEach(holding -> allSymbols.add(holding.getStockCode()));
                } catch (Exception e) {
                    log.error("계좌 보유 주식 조회 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
                }
            }
            
            if (allSymbols.isEmpty()) {
                log.info("모든 계좌에 보유 주식이 없습니다.");
                // accountService.updateTotalAsset 경유 → managed 엔티티로 재조회 후 DB 반영
                for (Account account : accounts) {
                    try {
                        accountService.updateTotalAsset(account);
                    } catch (Exception e) {
                        log.error("계좌 총 자산 업데이트 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
                    }
                }
                return;
            }
            
            log.info("배치 가격 조회 - 유니크 심볼 수: {}개", allSymbols.size());
            
            // 2. 배치로 모든 심볼의 가격을 한 번에 조회
            Map<String, BigDecimal> priceMap = binanceApiService.getCurrentPrices(allSymbols);
            
            if (priceMap.isEmpty()) {
                log.warn("가격 조회 실패 - 조회된 가격 정보가 없습니다.");
                return;
            }
            
            log.info("배치 가격 조회 완료 - 조회 성공: {}개", priceMap.size());
            
            // 3. 보유 종목들의 currentPrice를 DB에 업데이트
            stockHoldingsService.updateCurrentPrices(priceMap);
            
            // 4. 각 계좌의 총 자산을 업데이트 (DB의 currentPrice 사용)
            int successCount = 0;
            int failCount = 0;
            
            for (Account account : accounts) {
                try {
                    accountService.updateTotalAsset(account);
                    successCount++;
                } catch (Exception e) {
                    log.error("계좌 총 자산 업데이트 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
                    failCount++;
                }
            }
            
            log.info("계좌 총 자산 업데이트 완료 - 성공: {}개, 실패: {}개", successCount, failCount);
            
        } catch (Exception e) {
            log.error("계좌 총 자산 업데이트 스케줄러 실행 중 에러: {}", e.getMessage());
        }
    }
}

