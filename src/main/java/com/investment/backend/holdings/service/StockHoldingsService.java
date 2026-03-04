package com.investment.backend.holdings.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.holdings.dto.StockHoldingsResponse;
import com.investment.backend.holdings.entity.StockHoldings;
import com.investment.backend.holdings.repository.StockHoldingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockHoldingsService {

    private final StockHoldingsRepository stockHoldingsRepository;

    
    // 계좌의 보유 종목 목록 조회
    public List<StockHoldingsResponse> getHoldingsByAccount(UUID accountId) {
        List<StockHoldings> holdings = stockHoldingsRepository.findByAccountId(accountId);
        return holdings.stream()
                .map(StockHoldingsResponse::from)
                .toList();
    }

    
    // 매수 후 보유 자산(평균가) 업데이트
    @Transactional
    public void addHoldings(Account account, String stockCode, String stockName,
                           BigDecimal quantity, BigDecimal price) {
        StockHoldings holdings = stockHoldingsRepository
                .findByAccountIdAndStockCode(account.getId(), stockCode)
                .orElse(null);

        if (holdings == null) {
            // 신규 보유
            holdings = StockHoldings.builder()
                    .account(account)
                    .stockCode(stockCode)
                    .stockName(stockName)
                    .quantity(quantity)
                    .averagePrice(price)
                    .build();
            stockHoldingsRepository.save(holdings);
        } else {
            // 기존 보유에 추가
            holdings.addPosition(quantity, price);
        }
    }

    
    // 매도 후 보유 자산(평균가) 업데이트
    @Transactional
    public void reduceHoldings(UUID accountId, String stockCode, BigDecimal quantity) {
        StockHoldings holdings = stockHoldingsRepository
                .findByAccountIdAndStockCode(accountId, stockCode)
                .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 종목입니다."));

        holdings.reducePosition(quantity);

        // 보유 수량이 0이 되면 삭제
        if (holdings.isEmpty()) {
            stockHoldingsRepository.delete(holdings);
        }
    }

    
    // 매도 가능 수량 확인
    public boolean canSell(UUID accountId, String stockCode, BigDecimal quantity) {
        return stockHoldingsRepository.findByAccountIdAndStockCode(accountId, stockCode)
                .map(holdings -> holdings.getQuantity().compareTo(quantity) >= 0)
                .orElse(false);
    }

    
    // 보유 종목들의 현재가 배치 업데이트
    @Transactional
    public void updateCurrentPrices(Map<String, BigDecimal> priceMap) {
        if (priceMap == null || priceMap.isEmpty()) {
            return;
        }

        // 모든 보유 종목 조회
        List<StockHoldings> allHoldings = stockHoldingsRepository.findAll();
        
        int updatedCount = 0;
        for (StockHoldings holdings : allHoldings) {
            BigDecimal currentPrice = priceMap.get(holdings.getStockCode());
            if (currentPrice != null) {
                holdings.updateCurrentPrice(currentPrice);
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            log.debug("보유 종목 현재가 업데이트 완료 - {}개 종목", updatedCount);
        }
    }
}

