package com.investment.backend.holdings.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.holdings.dto.StockHoldingsResponse;
import com.investment.backend.holdings.entity.StockHoldings;
import com.investment.backend.holdings.repository.StockHoldingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockHoldingsServiceTest {

    @Mock
    private StockHoldingsRepository stockHoldingsRepository;

    @InjectMocks
    private StockHoldingsService stockHoldingsService;

    private Account testAccount;
    private UUID testAccountId;
    private StockHoldings testHoldings;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testAccount = Account.builder()
                .seedMoney(1000000L)
                .build();
        ReflectionTestUtils.setField(testAccount, "id", testAccountId);

        testHoldings = StockHoldings.builder()
                .account(testAccount)
                .stockCode("BTCUSDT")
                .stockName("Bitcoin")
                .quantity(new BigDecimal("0.5"))
                .averagePrice(new BigDecimal("50000"))
                .build();
        ReflectionTestUtils.setField(testHoldings, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("계좌별 보유 종목 목록 조회")
    void getHoldingsByAccount_Success() {
        // given
        List<StockHoldings> holdings = List.of(testHoldings);
        when(stockHoldingsRepository.findByAccountId(testAccountId)).thenReturn(holdings);

        // when
        List<StockHoldingsResponse> responses = stockHoldingsService.getHoldingsByAccount(testAccountId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStockCode()).isEqualTo("BTCUSDT");
        assertThat(responses.get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("0.5"));
        verify(stockHoldingsRepository).findByAccountId(testAccountId);
    }

    @Test
    @DisplayName("신규 보유 종목 추가")
    void addHoldings_NewStock() {
        // given
        String stockCode = "ETHUSDT";
        String stockName = "Ethereum";
        BigDecimal quantity = new BigDecimal("1");
        BigDecimal price = new BigDecimal("3000");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.empty());
        when(stockHoldingsRepository.save(any(StockHoldings.class))).thenAnswer(i -> i.getArguments()[0]);

        // when
        stockHoldingsService.addHoldings(testAccount, stockCode, stockName, quantity, price);

        // then
        verify(stockHoldingsRepository).save(argThat(holdings ->
                holdings.getStockCode().equals(stockCode) &&
                holdings.getStockName().equals(stockName) &&
                holdings.getQuantity().compareTo(quantity) == 0 &&
                holdings.getAveragePrice().compareTo(price) == 0
        ));
    }

    @Test
    @DisplayName("기존 보유 종목에 추가 매수 - 평균가 재계산")
    void addHoldings_ExistingStock() {
        // given
        String stockCode = "BTCUSDT";
        BigDecimal additionalQuantity = new BigDecimal("0.3");
        BigDecimal additionalPrice = new BigDecimal("60000");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.of(testHoldings));

        // when
        stockHoldingsService.addHoldings(testAccount, stockCode, "Bitcoin", additionalQuantity, additionalPrice);

        // then
        // 기존: 0.5 BTC @ 50,000 = 25,000
        // 추가: 0.3 BTC @ 60,000 = 18,000
        // 합계: 0.8 BTC @ 53,750 = 43,000
        verify(stockHoldingsRepository, never()).save(any());
        assertThat(testHoldings.getQuantity()).isEqualByComparingTo(new BigDecimal("0.8"));
        assertThat(testHoldings.getAveragePrice()).isEqualByComparingTo(new BigDecimal("53750"));
    }

    @Test
    @DisplayName("보유 종목 일부 매도")
    void reduceHoldings_PartialSell() {
        // given
        String stockCode = "BTCUSDT";
        BigDecimal sellQuantity = new BigDecimal("0.2");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.of(testHoldings));

        // when
        stockHoldingsService.reduceHoldings(testAccountId, stockCode, sellQuantity);

        // then
        assertThat(testHoldings.getQuantity()).isEqualByComparingTo(new BigDecimal("0.3"));
        verify(stockHoldingsRepository, never()).delete(any());
    }

    @Test
    @DisplayName("보유 종목 전량 매도 - 삭제")
    void reduceHoldings_FullSell() {
        // given
        String stockCode = "BTCUSDT";
        BigDecimal sellQuantity = new BigDecimal("0.5");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.of(testHoldings));

        // when
        stockHoldingsService.reduceHoldings(testAccountId, stockCode, sellQuantity);

        // then
        assertThat(testHoldings.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(stockHoldingsRepository).delete(testHoldings);
    }

    @Test
    @DisplayName("보유 종목 매도 실패 - 보유하지 않은 종목")
    void reduceHoldings_Fail_NotOwned() {
        // given
        String stockCode = "ETHUSDT";
        BigDecimal sellQuantity = new BigDecimal("1");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stockHoldingsService.reduceHoldings(testAccountId, stockCode, sellQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보유하지 않은 종목입니다.");
    }

    @Test
    @DisplayName("매도 가능 여부 확인 - 충분한 수량")
    void canSell_EnoughQuantity() {
        // given
        String stockCode = "BTCUSDT";
        BigDecimal sellQuantity = new BigDecimal("0.3");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.of(testHoldings));

        // when
        boolean result = stockHoldingsService.canSell(testAccountId, stockCode, sellQuantity);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("매도 가능 여부 확인 - 부족한 수량")
    void canSell_InsufficientQuantity() {
        // given
        String stockCode = "BTCUSDT";
        BigDecimal sellQuantity = new BigDecimal("1.0");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.of(testHoldings));

        // when
        boolean result = stockHoldingsService.canSell(testAccountId, stockCode, sellQuantity);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("매도 가능 여부 확인 - 보유하지 않은 종목")
    void canSell_NotOwned() {
        // given
        String stockCode = "ETHUSDT";
        BigDecimal sellQuantity = new BigDecimal("1.0");

        when(stockHoldingsRepository.findByAccountIdAndStockCode(testAccountId, stockCode))
                .thenReturn(Optional.empty());

        // when
        boolean result = stockHoldingsService.canSell(testAccountId, stockCode, sellQuantity);

        // then
        assertThat(result).isFalse();
    }
}
