package com.investment.backend.team.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleType;
import com.investment.backend.battle.enums.MetricType;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.team.dto.CreateTeamRequest;
import com.investment.backend.team.dto.JoinTeamRequest;
import com.investment.backend.team.dto.TeamResponse;
import com.investment.backend.team.entity.Team;
import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.enums.TeamUserRole;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.team.repository.TeamRepository;
import com.investment.backend.team.repository.TeamUserRepository;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

        @Mock
        private TeamRepository teamRepository;

        @Mock
        private TeamUserRepository teamUserRepository;

        @Mock
        private BattleRepository battleRepository;

        @Mock
        private AccountRepository accountRepository;

        @InjectMocks
        private TeamService teamService;

        private User testUser;
        private Battle testBattle;

        @BeforeEach
        void setUp() {
                testUser = User.builder()
                                .email("test@test.com")
                                .name("테스터")
                                .role(Role.USER)
                                .build();
                ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());

                testBattle = Battle.builder()
                                .type(BattleType.NORMAL)
                                .name("테스트 대결")
                                .ticker("BTC")
                                .startAt(LocalDateTime.now().plusDays(1))
                                .endAt(LocalDateTime.now().plusDays(7))
                                .metricType(MetricType.RATE)
                                .initialCapital(1000000)
                                .memberCount(5)
                                .teamCount(3)
                                .build();
                ReflectionTestUtils.setField(testBattle, "id", UUID.randomUUID());
        }

        @Test
        @DisplayName("팀 생성 성공")
        void createTeam_Success() {
                // given
                CreateTeamRequest request = new CreateTeamRequest();
                ReflectionTestUtils.setField(request, "name", "테스트팀");
                ReflectionTestUtils.setField(request, "description", "테스트 설명");

                Team savedTeam = Team.builder()
                                .battle(testBattle)
                                .name("테스트팀")
                                .description("테스트 설명")
                                .build();
                ReflectionTestUtils.setField(savedTeam, "id", 1L);

                when(battleRepository.findById(testBattle.getId())).thenReturn(Optional.of(testBattle));
                when(teamRepository.countByBattleId(testBattle.getId())).thenReturn(0L);
                when(teamUserRepository.existsByTeam_Battle_IdAndUserIdAndStatus(testBattle.getId(), testUser.getId(),
                                TeamUserStatus.ACTIVE)).thenReturn(false);
                when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);
                when(teamUserRepository.save(any(TeamUser.class))).thenReturn(null);
                when(accountRepository.save(any(Account.class))).thenReturn(null);

                // when
                TeamResponse response = teamService.createTeam(testBattle.getId(), request, testUser);

                // then
                assertThat(response.getName()).isEqualTo("테스트팀");
                assertThat(response.getMemberCount()).isEqualTo(1);
                verify(teamUserRepository).save(argThat(tu -> tu.getRole() == TeamUserRole.LEADER));
                verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("팀 수 제한 초과 시 예외 발생")
        void createTeam_TeamCountLimit_ThrowsException() {
                // given
                CreateTeamRequest request = new CreateTeamRequest();
                ReflectionTestUtils.setField(request, "name", "테스트팀");

                when(battleRepository.findById(testBattle.getId())).thenReturn(Optional.of(testBattle));
                when(teamRepository.countByBattleId(testBattle.getId())).thenReturn(3L); // teamCount = 3

                // when & then
                assertThatThrownBy(() -> teamService.createTeam(testBattle.getId(), request, testUser))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("팀 수 제한에 도달했습니다");
        }

        @Test
        @DisplayName("invite_code로 팀 가입 성공")
        void joinTeam_Success() {
                // given
                Team team = Team.builder()
                                .battle(testBattle)
                                .name("테스트팀")
                                .build();
                ReflectionTestUtils.setField(team, "id", 1L);
                UUID inviteCode = team.getInviteCode();

                JoinTeamRequest request = new JoinTeamRequest();
                ReflectionTestUtils.setField(request, "inviteCode", inviteCode);

                when(teamRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(team));
                when(teamUserRepository.existsByTeam_Battle_IdAndUserIdAndStatus(testBattle.getId(), testUser.getId(),
                                TeamUserStatus.ACTIVE)).thenReturn(false);
                when(teamUserRepository.countByTeamIdAndStatus(1L, TeamUserStatus.ACTIVE)).thenReturn(2L);
                when(accountRepository.save(any(Account.class))).thenReturn(null);

                // when
                TeamResponse response = teamService.joinTeam(request, testUser);

                // then
                assertThat(response.getMemberCount()).isEqualTo(3);
                verify(teamUserRepository).save(argThat(tu -> tu.getRole() == TeamUserRole.MEMBER));
                verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("팀원 수 제한 초과 시 예외 발생")
        void joinTeam_MemberCountLimit_ThrowsException() {
                // given
                Team team = Team.builder()
                                .battle(testBattle)
                                .name("테스트팀")
                                .build();
                ReflectionTestUtils.setField(team, "id", 1L);
                UUID inviteCode = team.getInviteCode();

                JoinTeamRequest request = new JoinTeamRequest();
                ReflectionTestUtils.setField(request, "inviteCode", inviteCode);

                when(teamRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(team));
                when(teamUserRepository.existsByTeam_Battle_IdAndUserIdAndStatus(testBattle.getId(), testUser.getId(),
                                TeamUserStatus.ACTIVE)).thenReturn(false);
                when(teamUserRepository.countByTeamIdAndStatus(1L, TeamUserStatus.ACTIVE)).thenReturn(5L); // memberCount
                                                                                                           // = 5

                // when & then
                assertThatThrownBy(() -> teamService.joinTeam(request, testUser))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("팀원 수 제한에 도달했습니다");
        }
}
