package com.investment.backend.account.controller;

import com.investment.backend.account.dto.AccountResponse;
import com.investment.backend.account.service.AccountService;
import com.investment.backend.holdings.dto.StockHoldingsResponse;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    // 개인 계좌 생성
    @PostMapping("/personal")
    public String createPersonalAccount(@AuthenticationPrincipal User user) {
        accountService.createPersonalAccount(user);
        return "개인 계좌 생성 완료";
    }

    // 개인 계좌 조회
    @GetMapping("/personal")
    public AccountResponse getPersonalAccount(@AuthenticationPrincipal User user) {
        return accountService.getPersonalAccount(user);
    }

    // 대결 계좌 조회
    @GetMapping("/battle/{battleId}")
    public ResponseEntity<AccountResponse> getBattleAccount(@AuthenticationPrincipal User user, @PathVariable UUID battleId) {
        return accountService.getBattleAccount(user, battleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // 204: 참여하지 않은 경우
    }

    // 내 전체 계좌 목록 조회
    @GetMapping("/my")
    public List<AccountResponse> getMyAccounts(@AuthenticationPrincipal User user) {
        return accountService.getMyAccounts(user);
    }

    // 계좌 보유 종목 조회
    @GetMapping("/{accountId}/stocks")
    public List<StockHoldingsResponse> getAccountStocks(
            @AuthenticationPrincipal User user,
            @PathVariable UUID accountId) {
        return accountService.getAccountStocks(user, accountId);
    }
}
