package com.investment.backend.team.service;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.team.dto.CreateTeamRequest;
import com.investment.backend.team.dto.JoinTeamRequest;
import com.investment.backend.team.dto.TeamMemberResponse;
import com.investment.backend.team.dto.TeamResponse;
import com.investment.backend.team.entity.Team;
import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.enums.TeamUserRole;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.team.repository.TeamRepository;
import com.investment.backend.team.repository.TeamUserRepository;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final BattleRepository battleRepository;
    private final Random random = new Random();

    /**
     * 팀 생성
     * - Battle의 teamCount 미만일 때만 생성 가능
     * - 생성자는 자동으로 LEADER로 등록
     */
    public TeamResponse createTeam(UUID battleId, CreateTeamRequest request, User user) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle을 찾을 수 없습니다."));

        // 팀 수 제한 체크
        long currentTeamCount = teamRepository.countByBattleId(battleId);
        if (currentTeamCount >= battle.getTeamCount()) {
            throw new IllegalArgumentException("팀 수 제한에 도달했습니다. (최대: " + battle.getTeamCount() + ")");
        }

        // 해당 Battle에서 이미 팀에 가입되어 있는지 체크
        if (teamUserRepository.existsByTeam_Battle_IdAndUserIdAndStatus(battleId, user.getId(),
                TeamUserStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 이 대결에서 다른 팀에 가입되어 있습니다.");
        }

        Team team = Team.builder()
                .battle(battle)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Team savedTeam = teamRepository.save(team);

        // 생성자를 LEADER로 등록
        TeamUser teamUser = TeamUser.builder()
                .team(savedTeam)
                .user(user)
                .role(TeamUserRole.LEADER)
                .build();
        teamUserRepository.save(teamUser);

        return TeamResponse.from(savedTeam, 1);
    }

    /**
     * Battle의 팀 목록 조회
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByBattle(UUID battleId) {
        List<Team> teams = teamRepository.findByBattleId(battleId);

        return teams.stream()
                .map(team -> {
                    int memberCount = (int) teamUserRepository.countByTeamIdAndStatus(team.getId(),
                            TeamUserStatus.ACTIVE);
                    return TeamResponse.from(team, memberCount);
                })
                .toList();
    }

    /**
     * invite_code로 팀 가입
     * - 팀원 수 제한 (Battle.memberCount) 체크
     */
    public TeamResponse joinTeam(JoinTeamRequest request, User user) {
        Team team = teamRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        Battle battle = team.getBattle();

        // 해당 Battle에서 이미 팀에 가입되어 있는지 체크
        if (teamUserRepository.existsByTeam_Battle_IdAndUserIdAndStatus(battle.getId(), user.getId(),
                TeamUserStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 이 대결에서 팀에 가입되어 있습니다.");
        }

        // 팀원 수 제한 체크
        long currentMemberCount = teamUserRepository.countByTeamIdAndStatus(team.getId(), TeamUserStatus.ACTIVE);
        if (currentMemberCount >= battle.getMemberCount()) {
            throw new IllegalArgumentException("팀원 수 제한에 도달했습니다. (최대: " + battle.getMemberCount() + ")");
        }

        TeamUser teamUser = TeamUser.builder()
                .team(team)
                .user(user)
                .role(TeamUserRole.MEMBER)
                .build();
        teamUserRepository.save(teamUser);

        return TeamResponse.from(team, (int) currentMemberCount + 1);
    }

    /**
     * 팀 탈퇴
     * - LEADER가 탈퇴할 경우 랜덤 팀원에게 LEADER 이전
     */
    public void leaveTeam(Long teamId, User user) {
        TeamUser teamUser = teamUserRepository
                .findByTeamIdAndUserIdAndStatus(teamId, user.getId(), TeamUserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("팀에 가입되어 있지 않습니다."));

        // LEADER가 탈퇴하는 경우
        if (teamUser.getRole() == TeamUserRole.LEADER) {
            List<TeamUser> remainingMembers = teamUserRepository
                    .findByTeamIdAndStatusAndRoleNot(teamId, TeamUserStatus.ACTIVE, TeamUserRole.LEADER);

            if (!remainingMembers.isEmpty()) {
                // 랜덤 팀원에게 LEADER 이전
                TeamUser newLeader = remainingMembers.get(random.nextInt(remainingMembers.size()));
                newLeader.promoteToLeader();
            }
            // 남은 팀원이 없으면 그냥 탈퇴 (팀은 빈 상태로 남음)
        }

        teamUser.leave();
    }

    /**
     * 팀원 목록 조회 (개인 수익률 포함)
     */
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        List<TeamUser> members = teamUserRepository.findByTeamIdAndStatus(teamId, TeamUserStatus.ACTIVE);
        return members.stream()
                .map(TeamMemberResponse::from)
                .toList();
    }
}
