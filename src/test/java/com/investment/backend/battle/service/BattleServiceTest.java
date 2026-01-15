package com.investment.backend.battle.service;

import com.investment.backend.battle.dto.BattleResponse;
import com.investment.backend.battle.dto.CreateBattleRequest;
import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.enums.BattleType;
import com.investment.backend.battle.enums.MetricType;
import com.investment.backend.battle.repository.BattleRepository;
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

    @InjectMocks
    private BattleService battleService;

    @Test
    @DisplayName("대결 생성 시 올바른 정보가 저장되고 응답이 반환되어야 한다")
    void createBattle_Success() {
        // given
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

        when(battleRepository.save(any(Battle.class))).thenReturn(savedBattle);

        // when
        BattleResponse response = battleService.createBattle(request);

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
    }

    @Test
    @DisplayName("대결 생성 시 기본값이 올바르게 적용되어야 한다")
    void createBattle_WithDefaults() {
        // given
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

        when(battleRepository.save(any(Battle.class))).thenReturn(savedBattle);

        // when
        BattleResponse response = battleService.createBattle(request);

        // then
        assertThat(response.getType()).isEqualTo(BattleType.NORMAL);
        assertThat(response.getName()).isEqualTo("battle");
        assertThat(response.getTicker()).isEqualTo("BTC");
        assertThat(response.getStatus()).isEqualTo(BattleStatus.YET);
        assertThat(response.getMetricType()).isEqualTo(MetricType.RATE);
        assertThat(response.getValuationTime()).isEqualTo(LocalTime.of(0, 0, 0));
        assertThat(response.getMemberCount()).isEqualTo(10);

        verify(battleRepository).save(any(Battle.class));
    }
}
