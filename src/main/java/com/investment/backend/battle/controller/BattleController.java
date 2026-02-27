package com.investment.backend.battle.controller;

import com.investment.backend.account.dto.AccountProfitResponse;
import com.investment.backend.battle.dto.BattleListResponse;
import com.investment.backend.battle.dto.BattleResponse;
import com.investment.backend.battle.dto.CreateBattleRequest;
import com.investment.backend.battle.dto.TeamProfitResponse;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.service.BattleProfitService;
import com.investment.backend.battle.service.BattleService;
import com.investment.backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;
    private final BattleProfitService battleProfitService;

    /**
     * Battle 목록 조회 (선택적 상태 필터 + limit)
     * - status 파라미터 없으면 전체 조회
     * - status=YET: 참여 가능한 배틀
     * - status=PROGRESS: 진행중인 배틀
     * - status=END: 종료된 배틀
     * - limit: 최대 조회 개수 (기본값: 10, 범위: 1-100)
     */
    @GetMapping
    public ResponseEntity<List<BattleListResponse>> getBattles(
            @RequestParam(required = false) BattleStatus status,
            @RequestParam(defaultValue = "10") int limit) {
        List<BattleListResponse> battles;
        
        if (status != null) {
            battles = battleService.getBattlesByStatus(status, limit);
        } else {
            battles = battleService.getAllBattles();
        }
        
        return ResponseEntity.ok(battles);
    }

    @PostMapping
    public ResponseEntity<BattleResponse> createBattle(
            @Valid @RequestBody CreateBattleRequest request,
            @AuthenticationPrincipal User user) {
        BattleResponse response = battleService.createBattle(request, user);
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

    /**
     * 배틀 내 계좌별 개인 수익률 조회 (수익률 내림차순)
     * GET /api/battles/{battleId}/profit/accounts
     */
    @GetMapping("/{battleId}/profit/accounts")
    public ResponseEntity<List<AccountProfitResponse>> getBattleAccountProfits(
            @PathVariable UUID battleId) {
        return ResponseEntity.ok(battleProfitService.getAccountProfits(battleId));
    }

    /**
     * 배틀 내 팀별 합산 수익률 조회 (수익률 내림차순, 팀원 개인 수익률 포함)
     * GET /api/battles/{battleId}/profit/teams
     */
    @GetMapping("/{battleId}/profit/teams")
    public ResponseEntity<List<TeamProfitResponse>> getBattleTeamProfits(
            @PathVariable UUID battleId) {
        return ResponseEntity.ok(battleProfitService.getTeamProfits(battleId));
    }
}
