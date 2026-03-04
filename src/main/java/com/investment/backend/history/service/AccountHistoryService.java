package com.investment.backend.history.service;

import com.investment.backend.account.dto.AccountProfitResponse;
import com.investment.backend.account.entity.Account;
import com.investment.backend.history.dto.AccountHistoryResponse;
import com.investment.backend.history.entity.AccountHistory;
import com.investment.backend.history.enums.TradeType;
import com.investment.backend.history.repository.AccountHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    /**
     * 수익률 스냅샷 저장
     * - REQUIRES_NEW: 증 스냅샷이 독립된 트랜잭션으로 실행되어, 하나가 실패해도
     *   외부 트랜잭션(코레안 rate 갱신 등)에 영향을 주지 않습니다
     * - amount: 수익금 (totalAsset - seedMoney)
     * - balanceSnapshot: 스냅샷 시점의 totalAsset
     * - description: "수익률: X.XX%"
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordProfitSnapshot(Account account) {
        long returnAmount = account.getTotalAsset() - account.getSeedMoney();
        double returnRate = AccountProfitResponse.calculateReturnRate(
                account.getTotalAsset(), account.getSeedMoney());

        AccountHistory snapshot = AccountHistory.builder()
                .account(account)
                .tradeType(TradeType.PROFIT_SNAPSHOT)
                .amount(BigDecimal.valueOf(returnAmount))
                .balanceSnapshot(BigDecimal.valueOf(account.getTotalAsset()))
                .description(String.format("수익률: %.2f%%", returnRate))
                .build();
        accountHistoryRepository.save(snapshot);
    }
}
