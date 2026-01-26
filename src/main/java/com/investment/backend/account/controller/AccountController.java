package com.investment.backend.account.controller;

import com.investment.backend.account.dto.AccountResponse;
import com.investment.backend.account.service.AccountService;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/personal")
    public String createPersonalAccount(@AuthenticationPrincipal User user) {
        accountService.createPersonalAccount(user);
        return "개인 계좌 생성 완료";
    }

    @GetMapping("/personal")
    public AccountResponse getPersonalAccount(@AuthenticationPrincipal User user) {
        return accountService.getPersonalAccount(user);
    }

    @GetMapping("/battle/{battleId}")
    public AccountResponse getBattleAccount(@AuthenticationPrincipal User user, @PathVariable UUID battleId) {
        return accountService.getBattleAccount(user, battleId);
    }
}
