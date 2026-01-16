package com.investment.backend.battle.controller;

import com.investment.backend.battle.dto.BattleListResponse;
import com.investment.backend.battle.dto.BattleResponse;
import com.investment.backend.battle.dto.CreateBattleRequest;
import com.investment.backend.battle.service.BattleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;

    /**
     * Battle 목록 조회 (팀 수익률 요약 포함)
     */
    @GetMapping
    public ResponseEntity<List<BattleListResponse>> getAllBattles() {
        List<BattleListResponse> battles = battleService.getAllBattles();
        return ResponseEntity.ok(battles);
    }

    @PostMapping
    public ResponseEntity<BattleResponse> createBattle(@Valid @RequestBody CreateBattleRequest request) {
        BattleResponse response = battleService.createBattle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Battle 세부 정보 조회
     */
    @GetMapping("/{battleId}")
    public ResponseEntity<BattleResponse> getBattle(@PathVariable UUID battleId) {
        BattleResponse response = battleService.getBattle(battleId);
        return ResponseEntity.ok(response);
    }

    // TODO: 실시간 현재가 조회 API (외부 API 연동 필요)
}
