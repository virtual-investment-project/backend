package com.investment.backend.holdings.repository;

import com.investment.backend.holdings.entity.StockHoldings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockHoldingsRepository extends JpaRepository<StockHoldings, UUID> {

    
    // 계좌별 보유 종목 목록 조회
    List<StockHoldings> findByAccountId(UUID accountId);

    
    // 계좌의 특정 종목 보유 내역 조회
    Optional<StockHoldings> findByAccountIdAndStockCode(UUID accountId, String stockCode);

    
    // 계좌의 특정 종목 보유 여부 확인
    boolean existsByAccountIdAndStockCode(UUID accountId, String stockCode);
}
