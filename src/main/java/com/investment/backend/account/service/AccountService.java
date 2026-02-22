package com.investment.backend.account.service;

import com.investment.backend.account.dto.AccountResponse;
import com.investment.backend.account.dto.AccountRankingResponse;
import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.holdings.dto.StockHoldingsResponse;
import com.investment.backend.holdings.service.StockHoldingsService;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final StockHoldingsService stockHoldingsService;

    @Transactional
    public void createPersonalAccount(User user) {
        // 이미 개인 계좌가 있는지 확인 (선택적)
        boolean exists = accountRepository.existsByUserAndTeamIsNullAndBattleIsNull(user);
        if (exists) {
            throw new IllegalArgumentException("이미 개인 계좌가 존재합니다.");
        }

        Account personalAccount = Account.createPersonalAccount(user);
        accountRepository.save(personalAccount);
    }

    public AccountResponse getPersonalAccount(User user) {
        Account account = accountRepository.findByUserIdAndTeamIsNullAndBattleIsNull(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("개인 계좌가 존재하지 않습니다."));
        return AccountResponse.from(account);
    }

    public Optional<AccountResponse> getBattleAccount(User user, UUID battleId) {
        return accountRepository.findByUserIdAndBattleId(user.getId(), battleId)
                .map(AccountResponse::from);
    }

    /**
     * 개인 계좌 수익률 상위 랭킹 조회
     */
    public List<AccountRankingResponse> getTopAccountsByReturnRate(int limit) {
        return accountRepository.findByTeamIsNullAndBattleIsNull().stream()
                .map(AccountRankingResponse::from)
                .sorted((a, b) -> Double.compare(b.getReturnRate(), a.getReturnRate()))
                .limit(limit)
                .toList();
    }

    // 계좌의 총 자산 업데이트 (DB에 저장된 currentPrice 사용)
    @Transactional
    public void updateTotalAsset(Account account) {
        try {
            // 현재 잔액
            long totalAsset = account.getBalance();

            // 보유 주식 목록 조회
            List<StockHoldingsResponse> holdings = stockHoldingsService.getHoldingsByAccount(account.getId());

            if (holdings.isEmpty()) {
                // 보유 주식이 없으면 잔액만 총 자산
                account.updateTotalAsset(totalAsset);
                log.debug("총 자산 업데이트 완료 (보유 주식 없음) - 계좌 ID: {}, 총 자산: {}", account.getId(), totalAsset);
                return;
            }

            // 각 보유 주식의 현재 평가액 계산
            for (StockHoldingsResponse holding : holdings) {
                // DB에 저장된 currentPrice 사용 (없으면 averagePrice 사용)
                BigDecimal priceToUse = holding.getCurrentPrice() != null
                        ? holding.getCurrentPrice()
                        : holding.getAveragePrice();

                BigDecimal stockValue = priceToUse.multiply(holding.getQuantity());
                totalAsset += stockValue.longValue();
            }

            // 총 자산 업데이트
            account.updateTotalAsset(totalAsset);
            log.debug("총 자산 업데이트 완료 - 계좌 ID: {}, 총 자산: {}", account.getId(), totalAsset);

        } catch (Exception e) {
            log.error("총 자산 업데이트 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
        }
    }
}
