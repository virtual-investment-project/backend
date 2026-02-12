package com.investment.backend.battle.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.battle.dto.BattleResponse;
import com.investment.backend.battle.dto.CreateBattleRequest;
import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.enums.BattleType;
import com.investment.backend.battle.enums.MetricType;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.team.entity.Team;
import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.repository.TeamRepository;
import com.investment.backend.team.repository.TeamUserRepository;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private BattleRepository battleRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamUserRepository teamUserRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private BattleService battleService;

    private User createTestUser() {
        User user = User.builder()
                .email("test@gmail.com")
                .name("테스트 유저")
                .role(Role.USER)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    @Test
    @DisplayName("대결 생성 시 올바른 정보가 저장되고 팀이 자동 생성되어야 한다")
    void createBattle_Success() {
        // given
        User user = createTestUser();

        CreateBattleRequest request = new CreateBattleRequest();
        ReflectionTestUtils.setField(request, "type", BattleType.NORMAL);
        ReflectionTestUtils.setField(request, "name", "테스트 대결");
        ReflectionTestUtils.setField(request, "ticker", "BTC");
        ReflectionTestUtils.setField(request, "startAt", LocalDateTime.of(2026, 1, 15, 9, 0));
        ReflectionTestUtils.setField(request, "endAt", LocalDateTime.of(2026, 1, 20, 18, 0));
        ReflectionTestUtils.setField(request, "metricType", MetricType.RATE);
        ReflectionTestUtils.setField(request, "valuationTime", LocalTime.of(15, 0));
        ReflectionTestUtils.setField(request, "initialCapital", 1000000);
        ReflectionTestUtils.setField(request, "memberCount", 5);

        Battle savedBattle = Battle.builder()
                .type(BattleType.NORMAL)
                .name("테스트 대결")
                .ticker("BTC")
                .startAt(LocalDateTime.of(2026, 1, 15, 9, 0))
                .endAt(LocalDateTime.of(2026, 1, 20, 18, 0))
                .metricType(MetricType.RATE)
                .valuationTime(LocalTime.of(15, 0))
                .initialCapital(1000000)
                .memberCount(5)
                .build();
        ReflectionTestUtils.setField(savedBattle, "id", UUID.randomUUID());

        Team savedTeam = Team.builder()
                .battle(savedBattle)
                .name("테스트 유저의 팀")
                .build();
        ReflectionTestUtils.setField(savedTeam, "id", 1L);

        when(battleRepository.save(any(Battle.class))).thenReturn(savedBattle);
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);
        when(teamUserRepository.save(any(TeamUser.class))).thenReturn(null);
        when(accountRepository.save(any(Account.class))).thenReturn(null);

        // when
        BattleResponse response = battleService.createBattle(request, user);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getType()).isEqualTo(BattleType.NORMAL);
        assertThat(response.getName()).isEqualTo("테스트 대결");
        assertThat(response.getTicker()).isEqualTo("BTC");
        assertThat(response.getStatus()).isEqualTo(BattleStatus.YET);
        assertThat(response.getMetricType()).isEqualTo(MetricType.RATE);
        assertThat(response.getInitialCapital()).isEqualTo(1000000);
        assertThat(response.getMemberCount()).isEqualTo(5);

        verify(battleRepository).save(any(Battle.class));
        verify(teamRepository).save(any(Team.class));
        verify(teamUserRepository).save(any(TeamUser.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("대결 생성 시 기본값이 올바르게 적용되어야 한다")
    void createBattle_WithDefaults() {
        // given
        User user = createTestUser();

        CreateBattleRequest request = new CreateBattleRequest();
        ReflectionTestUtils.setField(request, "startAt", LocalDateTime.of(2026, 1, 15, 9, 0));
        ReflectionTestUtils.setField(request, "endAt", LocalDateTime.of(2026, 1, 20, 18, 0));
        ReflectionTestUtils.setField(request, "initialCapital", 500000);

        Battle savedBattle = Battle.builder()
                .startAt(LocalDateTime.of(2026, 1, 15, 9, 0))
                .endAt(LocalDateTime.of(2026, 1, 20, 18, 0))
                .initialCapital(500000)
                .build();
        ReflectionTestUtils.setField(savedBattle, "id", UUID.randomUUID());

        Team savedTeam = Team.builder()
                .battle(savedBattle)
                .name("테스트 유저의 팀")
                .build();
        ReflectionTestUtils.setField(savedTeam, "id", 1L);

        when(battleRepository.save(any(Battle.class))).thenReturn(savedBattle);
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);
        when(teamUserRepository.save(any(TeamUser.class))).thenReturn(null);
        when(accountRepository.save(any(Account.class))).thenReturn(null);

        // when
        BattleResponse response = battleService.createBattle(request, user);

        // then
        assertThat(response.getType()).isEqualTo(BattleType.NORMAL);
        assertThat(response.getName()).isEqualTo("battle");
        assertThat(response.getTicker()).isEqualTo("BTC");
        assertThat(response.getStatus()).isEqualTo(BattleStatus.YET);
        assertThat(response.getMetricType()).isEqualTo(MetricType.RATE);
        assertThat(response.getValuationTime()).isEqualTo(LocalTime.of(0, 0, 0));
        assertThat(response.getMemberCount()).isEqualTo(10);

        verify(battleRepository).save(any(Battle.class));
        verify(teamRepository).save(any(Team.class));
    }
}
