package com.investment.backend.account.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.external.binance.BinanceApiService;
import com.investment.backend.holdings.dto.StockHoldingsResponse;
import com.investment.backend.holdings.service.StockHoldingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StockHoldingsService stockHoldingsService;

    @Mock
    private BinanceApiService binanceApiService;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testAccount = Account.builder()
                .seedMoney(1000000L)  // 100만원
                .build();
        ReflectionTestUtils.setField(testAccount, "id", testAccountId);
        ReflectionTestUtils.setField(testAccount, "balance", 1000000L);  // 잔액 설정
        ReflectionTestUtils.setField(testAccount, "totalAsset", 1000000L);  // 초기 총 자산
    }

    @Test
    @DisplayName("총 자산 업데이트 (DB의 currentPrice 사용)")
    void updateTotalAsset_WithCurrentPrice() {
        // given
        List<StockHoldingsResponse> holdings = List.of(
                createHoldingsResponse("BTC/USDT", new BigDecimal("0.5"), new BigDecimal("50000"), new BigDecimal("60000")),
                createHoldingsResponse("ETH/USDT", new BigDecimal("2"), new BigDecimal("3000"), new BigDecimal("3500"))
        );
        
        when(stockHoldingsService.getHoldingsByAccount(testAccountId)).thenReturn(holdings);

        // when
        accountService.updateTotalAsset(testAccount);

        // then
        // 잔액: 1,000,000
        // BTC: 0.5 * 60,000 = 30,000
        // ETH: 2 * 3,500 = 7,000
        // 총 자산: 1,037,000
        assertThat(testAccount.getTotalAsset()).isEqualTo(1037000L);
    }

    @Test
    @DisplayName("총 자산 업데이트, 보유 주식 없음")
    void updateTotalAsset_NoHoldings() {
        // given
        when(stockHoldingsService.getHoldingsByAccount(testAccountId))
                .thenReturn(Collections.emptyList());

        // when
        accountService.updateTotalAsset(testAccount);

        // then
        assertThat(testAccount.getTotalAsset()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("총 자산 업데이트, currentPrice 없을 때 averagePrice 사용")
    void updateTotalAsset_FallbackToAveragePrice() {
        // given
        List<StockHoldingsResponse> holdings = List.of(
                createHoldingsResponse("BTC/USDT", new BigDecimal("0.5"), new BigDecimal("50000"), new BigDecimal("60000")),
                createHoldingsResponse("ETH/USDT", new BigDecimal("2"), new BigDecimal("3000"), null)  // currentPrice 없음
        );
        
        when(stockHoldingsService.getHoldingsByAccount(testAccountId)).thenReturn(holdings);

        // when
        accountService.updateTotalAsset(testAccount);

        // then
        // 잔액: 1,000,000
        // BTC: 0.5 * 60,000 = 30,000 (currentPrice)
        // ETH: 2 * 3,000 = 6,000 (averagePrice 사용)
        // 총 자산: 1,036,000
        assertThat(testAccount.getTotalAsset()).isEqualTo(1036000L);
    }

    private StockHoldingsResponse createHoldingsResponse(String stockCode, BigDecimal quantity, BigDecimal averagePrice, BigDecimal currentPrice) {
        return StockHoldingsResponse.builder()
                .stockCode(stockCode)
                .quantity(quantity)
                .averagePrice(averagePrice)
                .currentPrice(currentPrice)
                .build();
    }
}
