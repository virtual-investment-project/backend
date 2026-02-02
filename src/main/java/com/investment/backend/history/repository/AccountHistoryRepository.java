package com.investment.backend.history.repository;

import com.investment.backend.history.entity.AccountHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, UUID> {

    
    // 계좌별 거래 내역 조회 (최신순)
    List<AccountHistory> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
