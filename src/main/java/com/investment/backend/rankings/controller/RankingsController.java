package com.investment.backend.rankings.controller;

import com.investment.backend.account.dto.AccountRankingResponse;
import com.investment.backend.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
public class RankingsController {

    private final AccountService accountService;

    /**
     * 개인 계좌 수익률 상위 조회
     * GET /api/rankings/accounts/top?limit=3
     */
    @GetMapping("/accounts/top")
    public List<AccountRankingResponse> getTopAccounts(
            @RequestParam(defaultValue = "10", name = "limit") int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit은 1 이상 100 이하의 값이어야 합니다.");
        }
        return accountService.getTopAccountsByReturnRate(limit);
    }
}
