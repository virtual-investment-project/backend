package com.investment.backend.order.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.external.binance.BinanceApiService;
import com.investment.backend.history.enums.TradeType;
import com.investment.backend.history.service.AccountHistoryService;
import com.investment.backend.holdings.dto.StockHoldingsResponse;
import com.investment.backend.holdings.service.StockHoldingsService;
import com.investment.backend.order.dto.CreateOrderRequest;
import com.investment.backend.order.dto.OrderResponse;
import com.investment.backend.order.entity.Order;
import com.investment.backend.order.enums.OrderStatus;
import com.investment.backend.order.enums.OrderType;
import com.investment.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final StockHoldingsService stockHoldingsService;
    private final AccountHistoryService accountHistoryService;
    private final BinanceApiService binanceApiService;

    
    // 주문 생성
    public OrderResponse createOrder(CreateOrderRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        BigDecimal totalAmount = request.getOrderPrice().multiply(request.getQuantity());

        // 주문 유형별 검증
        if (request.getOrderType() == OrderType.BUY) {
            // 매수: 잔액 확인
            if (BigDecimal.valueOf(account.getBalance()).compareTo(totalAmount) < 0) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }
            // 잔액 차감 (주문 예약)
            account.updateBalance(account.getBalance() - totalAmount.longValue());
        } else {
            // 매도: 보유 수량 확인
            if (!stockHoldingsService.canSell(account.getId(), request.getStockCode(), request.getQuantity())) {
                throw new IllegalArgumentException("보유 수량이 부족합니다.");
            }
        }

        // 주문 생성
        Order order = Order.builder()
                .account(account)
                .stockCode(request.getStockCode())
                .stockName(request.getStockName())
                .orderPrice(request.getOrderPrice())
                .quantity(request.getQuantity())
                .orderType(request.getOrderType())
                .build();

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    
    // 계좌별 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByAccount(UUID accountId) {
        List<Order> orders = orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
        return orders.stream()
                .map(OrderResponse::from)
                .toList();
    }

    
    // 주문 취소
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        order.cancelOrder();

        // 주문 금액 복구 (매수의 경우만)
        if (order.getOrderType() == OrderType.BUY) {
            Account account = order.getAccount();
            BigDecimal totalAmount = order.getTotalAmount();
            account.updateBalance(account.getBalance() + totalAmount.longValue());
        }
    }

    
    // 주문 체결 처리 (외부 호출용)
    public void fillOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 체결할 수 있습니다.");
        }

        Account account = order.getAccount();
        // 지정가 주문이므로 체결가는 항상 주문가
        BigDecimal orderPrice = order.getOrderPrice();
        BigDecimal totalAmount = orderPrice.multiply(order.getQuantity());

        if (order.getOrderType() == OrderType.BUY) {
            // 매수 체결 - 주문 생성 시 이미 차감했으므로 잔액 변동 없음
            stockHoldingsService.addHoldings(
                    account,
                    order.getStockCode(),
                    order.getStockName(),
                    order.getQuantity(),
                    orderPrice
            );
            // 거래 내역 기록
            accountHistoryService.recordHistory(
                    account,
                    TradeType.BUY,
                    totalAmount,
                    String.format("%s %s 매수", order.getStockName(), order.getQuantity())
            );
        } else {
            // 매도 체결
            stockHoldingsService.reduceHoldings(
                    account.getId(),
                    order.getStockCode(),
                    order.getQuantity()
            );
            // 잔액 증가
            account.updateBalance(account.getBalance() + totalAmount.longValue());
            // 거래 내역 기록
            accountHistoryService.recordHistory(
                    account,
                    TradeType.SELL,
                    totalAmount,
                    String.format("%s %s 매도", order.getStockName(), order.getQuantity())
            );
        }

        // 주문 상태 변경
        order.fillOrder();

        // 총 자산 업데이트
        updateTotalAsset(account);
    }

    
    // 계좌의 총 자산 업데이트
    // 총 자산 = 잔액 + 보유 주식의 현재 평가액
    private void updateTotalAsset(Account account) {
        try {
            // 현재 잔액
            long totalAsset = account.getBalance();

            // 보유 주식 목록 조회
            List<StockHoldingsResponse> holdings = stockHoldingsService.getHoldingsByAccount(account.getId());

            // 각 보유 주식의 현재 평가액 계산
            for (StockHoldingsResponse holding : holdings) {
                BigDecimal currentPrice = binanceApiService.getCurrentPrice(holding.getStockCode());
                
                if (currentPrice != null) {
                    BigDecimal stockValue = currentPrice.multiply(holding.getQuantity());
                    totalAsset += stockValue.longValue();
                } else {
                    // 가격 조회 실패 시 평균 매수가로 계산
                    log.warn("가격 조회 실패, 평균 매수가 사용 - 심볼: {}", holding.getStockCode());
                    BigDecimal stockValue = holding.getAveragePrice().multiply(holding.getQuantity());
                    totalAsset += stockValue.longValue();
                }
            }

            // 총 자산 업데이트
            account.updateTotalAsset(totalAsset);
            log.debug("총 자산 업데이트 완료 - 계좌 ID: {}, 총 자산: {}", account.getId(), totalAsset);
            
        } catch (Exception e) {
            log.error("총 자산 업데이트 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
        }
    }
}
