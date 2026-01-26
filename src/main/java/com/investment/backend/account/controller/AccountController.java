package com.investment.backend.account.controller;

import com.investment.backend.account.service.AccountService;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
