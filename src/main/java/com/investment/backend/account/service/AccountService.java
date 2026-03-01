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
import java.util.stream.Collectors;

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

    // 내 전체 계좌 목록 조회(개인 + 배틀)
    public List<AccountResponse> getMyAccounts(User user) {
        return accountRepository.findByUserId(user.getId()).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    // 계좌의 보유 종목 조회 (소유자 검증 포함)
    public List<StockHoldingsResponse> getAccountStocks(User user, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 존재하지 않습니다."));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 계좌에 접근 권한이 없습니다.");
        }
        return stockHoldingsService.getHoldingsByAccount(accountId);
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
            // detached 상태로 넘어온 경우를 대비해 DB에서 managed 엔티티로 재조회
            Account managed = accountRepository.findById(account.getId())
                    .orElseThrow(() -> new IllegalArgumentException("계좌가 존재하지 않습니다: " + account.getId()));

            // 현재 잔액
            long totalAsset = managed.getBalance();

            // 보유 주식 목록 조회
            List<StockHoldingsResponse> holdings = stockHoldingsService.getHoldingsByAccount(managed.getId());

            if (holdings.isEmpty()) {
                // 보유 주식이 없으면 잔액만 총 자산
                managed.updateTotalAsset(totalAsset);
                log.debug("총 자산 업데이트 완료 (보유 주식 없음) - 계좌 ID: {}, 총 자산: {}", managed.getId(), totalAsset);
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

            // 총 자산 업데이트 (managed 엔티티이므로 dirty checking으로 자동 반영)
            managed.updateTotalAsset(totalAsset);
            log.debug("총 자산 업데이트 완료 - 계좌 ID: {}, 총 자산: {}", managed.getId(), totalAsset);

        } catch (Exception e) {
            log.error("총 자산 업데이트 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
        }
    }
}
