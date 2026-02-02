package com.investment.backend.history.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.history.dto.AccountHistoryResponse;
import com.investment.backend.history.entity.AccountHistory;
import com.investment.backend.history.enums.TradeType;
import com.investment.backend.history.repository.AccountHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountHistoryService {

    private final AccountHistoryRepository accountHistoryRepository;

    
    // 계좌의 거래 내역 조회
    public List<AccountHistoryResponse> getHistoriesByAccount(UUID accountId) {
        List<AccountHistory> histories = accountHistoryRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId);
        return histories.stream()
                .map(AccountHistoryResponse::from)
                .toList();
    }

    
    // 거래 내역 기록
    @Transactional
    public void recordHistory(Account account, TradeType tradeType, 
                             BigDecimal amount, String description) {
        AccountHistory history = AccountHistory.builder()
                .account(account)
                .tradeType(tradeType)
                .amount(amount)
                .balanceSnapshot(BigDecimal.valueOf(account.getBalance()))
                .description(description)
                .build();
        accountHistoryRepository.save(history);
    }
}
