package com.investment.backend.account.service;

import com.investment.backend.account.dto.AccountResponse;
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
import java.util.Map;
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

    public AccountResponse getBattleAccount(User user, UUID battleId) {
        Account account = accountRepository.findByUserIdAndBattleId(user.getId(), battleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배틀의 계좌가 존재하지 않습니다."));
        return AccountResponse.from(account);
    }
    
    // 계좌의 총 자산 업데이트 (이미 조회된 가격 맵 사용)
    // 배치 조회 최적화를 위해 스케줄러에서 호출
    @Transactional
    public void updateTotalAsset(Account account, Map<String, BigDecimal> priceMap) {
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
                BigDecimal currentPrice = priceMap.get(holding.getStockCode());
                
                if (currentPrice != null) {
                    BigDecimal stockValue = currentPrice.multiply(holding.getQuantity());
                    totalAsset += stockValue.longValue();
                } else {
                    // 가격 조회 실패 시 평균 매수가로 계산
                    log.warn("가격 조회 실패, 평균 매수가 사용 - 심볼: {}", holding.getStockCode());
                    BigDecimal stockValue = holding.getAveragePrice().multiply(holding.getQuantity());
                    totalAsset += stockValue.longValue();
                }
            }

            // 총 자산 업데이트
            account.updateTotalAsset(totalAsset);
            log.debug("총 자산 업데이트 완료 - 계좌 ID: {}, 총 자산: {}", account.getId(), totalAsset);
            
        } catch (Exception e) {
            log.error("총 자산 업데이트 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
        }
    }
}
