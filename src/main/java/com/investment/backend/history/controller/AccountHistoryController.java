package com.investment.backend.history.controller;

import com.investment.backend.history.dto.AccountHistoryResponse;
import com.investment.backend.history.service.AccountHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class AccountHistoryController {

    private final AccountHistoryService accountHistoryService;

    
    // 계좌별 거래 내역 조회
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<AccountHistoryResponse>> getHistoriesByAccount(@PathVariable UUID accountId) {
        List<AccountHistoryResponse> histories = accountHistoryService.getHistoriesByAccount(accountId);
        return ResponseEntity.ok(histories);
    }
}
