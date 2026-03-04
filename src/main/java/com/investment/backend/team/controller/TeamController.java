package com.investment.backend.team.controller;

import com.investment.backend.team.dto.CreateTeamRequest;
import com.investment.backend.team.dto.JoinTeamRequest;
import com.investment.backend.team.dto.TeamMemberResponse;
import com.investment.backend.team.dto.TeamResponse;
import com.investment.backend.team.service.TeamService;
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
@RequestMapping("/api")
public class TeamController {

    private final TeamService teamService;

    /**
     * 팀 생성
     */
    @PostMapping("/battles/{battleId}/teams")
    public ResponseEntity<TeamResponse> createTeam(
            @PathVariable UUID battleId,
            @Valid @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal User user) {

        TeamResponse response = teamService.createTeam(battleId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Battle의 팀 목록 조회
     */
    @GetMapping("/battles/{battleId}/teams")
    public ResponseEntity<List<TeamResponse>> getTeams(
            @PathVariable UUID battleId,
            @AuthenticationPrincipal User user) {
        List<TeamResponse> teams = teamService.getTeamsByBattle(battleId, user);
        return ResponseEntity.ok(teams);
    }

    /**
     * 팀원 목록 조회 (개인 수익률 포함)
     */
    @GetMapping("/teams/{teamId}/members")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(@PathVariable Long teamId) {
        List<TeamMemberResponse> members = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(members);
    }

    /**
     * invite_code로 팀 가입
     */
    @PostMapping("/teams/join")
    public ResponseEntity<TeamResponse> joinTeam(
            @Valid @RequestBody JoinTeamRequest request,
            @AuthenticationPrincipal User user) {

        TeamResponse response = teamService.joinTeam(request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 팀 탈퇴
     */
    @DeleteMapping("/teams/{teamId}/leave")
    public ResponseEntity<Void> leaveTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal User user) {

        teamService.leaveTeam(teamId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * 팀원 추방 (LEADER 전용)
     */
    @DeleteMapping("/teams/{teamId}/members/{teamUserId}/kick")
    public ResponseEntity<Void> kickMember(
            @PathVariable Long teamId,
            @PathVariable Long teamUserId,
            @AuthenticationPrincipal User user) {

        teamService.kickMember(teamId, teamUserId, user);
        return ResponseEntity.noContent().build();
    }
}
