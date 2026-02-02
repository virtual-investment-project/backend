package com.investment.backend.order.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.history.enums.TradeType;
import com.investment.backend.history.service.AccountHistoryService;
import com.investment.backend.holdings.service.StockHoldingsService;
import com.investment.backend.order.dto.CreateOrderRequest;
import com.investment.backend.order.dto.OrderResponse;
import com.investment.backend.order.entity.Order;
import com.investment.backend.order.enums.OrderStatus;
import com.investment.backend.order.enums.OrderType;
import com.investment.backend.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StockHoldingsService stockHoldingsService;

    @Mock
    private AccountHistoryService accountHistoryService;

    @InjectMocks
    private OrderService orderService;

    private Account testAccount;
    private UUID testAccountId;
    private Order testBuyOrder;
    private Order testSellOrder;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testAccount = Account.builder()
                .seedMoney(1000000L)  // 100만원
                .build();
        ReflectionTestUtils.setField(testAccount, "id", testAccountId);

        testBuyOrder = Order.builder()
                .account(testAccount)
                .stockCode("BTCUSDT")
                .stockName("Bitcoin")
                .orderPrice(new BigDecimal("50000"))
                .quantity(new BigDecimal("0.1"))
                .orderType(OrderType.BUY)
                .build();
        ReflectionTestUtils.setField(testBuyOrder, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(testBuyOrder, "status", OrderStatus.PENDING);
        ReflectionTestUtils.setField(testBuyOrder, "createdAt", LocalDateTime.now());

        testSellOrder = Order.builder()
                .account(testAccount)
                .stockCode("ETHUSDT")
                .stockName("Ethereum")
                .orderPrice(new BigDecimal("3000"))
                .quantity(new BigDecimal("1"))
                .orderType(OrderType.SELL)
                .build();
        ReflectionTestUtils.setField(testSellOrder, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(testSellOrder, "status", OrderStatus.PENDING);
        ReflectionTestUtils.setField(testSellOrder, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("매수 주문 생성 성공")
    void createBuyOrder_Success() {
        // given
        CreateOrderRequest request = createBuyOrderRequest();
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(orderRepository.save(any(Order.class))).thenReturn(testBuyOrder);

        // when
        OrderResponse response = orderService.createOrder(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStockCode()).isEqualTo("BTCUSDT");
        assertThat(response.getOrderType()).isEqualTo(OrderType.BUY);
        assertThat(testAccount.getBalance()).isEqualTo(995000L);  // 100만 - 5천 = 99.5만
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("매수 주문 생성 실패 - 잔액 부족")
    void createBuyOrder_Fail_InsufficientBalance() {
        // given
        CreateOrderRequest request = createOrderRequest(testAccountId, "BTCUSDT", "Bitcoin",
                new BigDecimal("100000"), new BigDecimal("20"), OrderType.BUY);  // 200만원 필요
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔액이 부족합니다.");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("매도 주문 생성 성공")
    void createSellOrder_Success() {
        // given
        CreateOrderRequest request = createSellOrderRequest();
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(stockHoldingsService.canSell(testAccountId, "ETHUSDT", new BigDecimal("1")))
                .thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testSellOrder);

        // when
        OrderResponse response = orderService.createOrder(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStockCode()).isEqualTo("ETHUSDT");
        assertThat(response.getOrderType()).isEqualTo(OrderType.SELL);
        assertThat(testAccount.getBalance()).isEqualTo(1000000L);  // 잔액 변화 없음
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("매도 주문 생성 실패 - 보유 수량 부족")
    void createSellOrder_Fail_InsufficientHoldings() {
        // given
        CreateOrderRequest request = createSellOrderRequest();
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(stockHoldingsService.canSell(testAccountId, "ETHUSDT", new BigDecimal("1")))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보유 수량이 부족합니다.");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 실패 - 계좌를 찾을 수 없음")
    void createOrder_Fail_AccountNotFound() {
        // given
        CreateOrderRequest request = createBuyOrderRequest();
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계좌를 찾을 수 없습니다.");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("계좌별 주문 목록 조회")
    void getOrdersByAccount_Success() {
        // given
        List<Order> orders = List.of(testBuyOrder, testSellOrder);
        when(orderRepository.findByAccountIdOrderByCreatedAtDesc(testAccountId))
                .thenReturn(orders);

        // when
        List<OrderResponse> responses = orderService.getOrdersByAccount(testAccountId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getStockCode()).isEqualTo("BTCUSDT");
        assertThat(responses.get(1).getStockCode()).isEqualTo("ETHUSDT");
        verify(orderRepository).findByAccountIdOrderByCreatedAtDesc(testAccountId);
    }

    @Test
    @DisplayName("매수 주문 취소 성공")
    void cancelBuyOrder_Success() {
        // given
        UUID orderId = testBuyOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testBuyOrder));
        testAccount.updateBalance(995000L);  // 주문으로 이미 차감된 상태

        // when
        orderService.cancelOrder(orderId);

        // then
        assertThat(testAccount.getBalance()).isEqualTo(1000000L);  // 잔액 복구됨
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("매도 주문 취소 성공")
    void cancelSellOrder_Success() {
        // given
        UUID orderId = testSellOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testSellOrder));
        long initialBalance = testAccount.getBalance();

        // when
        orderService.cancelOrder(orderId);

        // then
        assertThat(testAccount.getBalance()).isEqualTo(initialBalance);  // 잔액 변화 없음
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("주문 취소 실패 - 주문을 찾을 수 없음")
    void cancelOrder_Fail_OrderNotFound() {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("매수 주문 체결 성공")
    void fillBuyOrder_Success() {
        // given
        UUID orderId = testBuyOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testBuyOrder));
        doNothing().when(stockHoldingsService).addHoldings(
                any(Account.class), anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class));
        doNothing().when(accountHistoryService).recordHistory(
                any(Account.class), any(TradeType.class), any(BigDecimal.class), anyString());

        // when
        orderService.fillOrder(orderId);

        // then
        verify(stockHoldingsService).addHoldings(
                eq(testAccount), eq("BTCUSDT"), eq("Bitcoin"),
                eq(new BigDecimal("0.1")), eq(new BigDecimal("50000")));
        verify(accountHistoryService).recordHistory(
                eq(testAccount), eq(TradeType.BUY), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("매도 주문 체결 성공")
    void fillSellOrder_Success() {
        // given
        UUID orderId = testSellOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testSellOrder));
        doNothing().when(stockHoldingsService).reduceHoldings(
                any(UUID.class), anyString(), any(BigDecimal.class));
        doNothing().when(accountHistoryService).recordHistory(
                any(Account.class), any(TradeType.class), any(BigDecimal.class), anyString());

        // when
        orderService.fillOrder(orderId);

        // then
        assertThat(testAccount.getBalance()).isEqualTo(1003000L);  // 100만 + 3000 = 100.3만
        verify(stockHoldingsService).reduceHoldings(
                eq(testAccountId), eq("ETHUSDT"), eq(new BigDecimal("1")));
        verify(accountHistoryService).recordHistory(
                eq(testAccount), eq(TradeType.SELL), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("주문 체결 실패 - 주문을 찾을 수 없음")
    void fillOrder_Fail_OrderNotFound() {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.fillOrder(orderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 체결 실패 - PENDING 상태가 아님")
    void fillOrder_Fail_NotPendingStatus() {
        // given
        UUID orderId = testBuyOrder.getId();
        ReflectionTestUtils.setField(testBuyOrder, "status", OrderStatus.FILLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testBuyOrder));

        // when & then
        assertThatThrownBy(() -> orderService.fillOrder(orderId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 중인 주문만 체결할 수 있습니다.");
    }

    // Helper methods
    private CreateOrderRequest createBuyOrderRequest() {
        return createOrderRequest(testAccountId, "BTCUSDT", "Bitcoin",
                new BigDecimal("50000"), new BigDecimal("0.1"), OrderType.BUY);
    }

    private CreateOrderRequest createSellOrderRequest() {
        return createOrderRequest(testAccountId, "ETHUSDT", "Ethereum",
                new BigDecimal("3000"), new BigDecimal("1"), OrderType.SELL);
    }

    private CreateOrderRequest createOrderRequest(UUID accountId, String stockCode, String stockName,
                                                   BigDecimal orderPrice, BigDecimal quantity, OrderType orderType) {
        CreateOrderRequest request = new CreateOrderRequest();
        ReflectionTestUtils.setField(request, "accountId", accountId);
        ReflectionTestUtils.setField(request, "stockCode", stockCode);
        ReflectionTestUtils.setField(request, "stockName", stockName);
        ReflectionTestUtils.setField(request, "orderPrice", orderPrice);
        ReflectionTestUtils.setField(request, "quantity", quantity);
        ReflectionTestUtils.setField(request, "orderType", orderType);
        return request;
    }
}
