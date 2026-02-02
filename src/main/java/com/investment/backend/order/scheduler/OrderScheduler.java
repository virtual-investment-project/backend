package com.investment.backend.order.scheduler;

import com.investment.backend.external.binance.BinanceApiService;
import com.investment.backend.order.entity.Order;
import com.investment.backend.order.enums.OrderStatus;
import com.investment.backend.order.enums.OrderType;
import com.investment.backend.order.repository.OrderRepository;
import com.investment.backend.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final BinanceApiService binanceApiService;

    
    // 5초마다 PENDING 주문 확인 및 자동 체결
    @Scheduled(fixedRate = 5000)  // 5초마다 실행
    public void autoFillOrders() {
        try {
            // PENDING 상태인 주문들 조회
            List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
            
            if (pendingOrders.isEmpty()) {
                return;
            }
            
            log.info("자동 체결 스케줄러 실행 - 대기 주문: {}개", pendingOrders.size());
            
            for (Order order : pendingOrders) {
                try {
                    processOrder(order);
                } catch (Exception e) {
                    log.error("주문 처리 실패 - 주문 ID: {}, 에러: {}", order.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("자동 체결 스케줄러 실행 중 에러: {}", e.getMessage());
        }
    }

    
    // 개별 주문 처리
    private void processOrder(Order order) {
        // 바이낸스에서 현재가 조회
        BigDecimal currentPrice = binanceApiService.getCurrentPrice(order.getStockCode());
        
        if (currentPrice == null) {
            log.warn("가격 조회 실패 - 주문 ID: {}, 심볼: {}", order.getId(), order.getStockCode());
            return;
        }
        
        // 체결 조건 확인
        boolean shouldFill = false;
        
        if (order.getOrderType() == OrderType.BUY) {
            // 매수: 주문가 >= 현재가 (현재가가 주문가보다 낮거나 같으면 체결)
            if (order.getOrderPrice().compareTo(currentPrice) >= 0) {
                shouldFill = true;
                log.info("매수 주문 체결 조건 충족 - 주문가: {}, 현재가: {}", order.getOrderPrice(), currentPrice);
            }
        } else {
            // 매도: 주문가 <= 현재가 (현재가가 주문가보다 높거나 같으면 체결)
            if (order.getOrderPrice().compareTo(currentPrice) <= 0) {
                shouldFill = true;
                log.info("매도 주문 체결 조건 충족 - 주문가: {}, 현재가: {}", order.getOrderPrice(), currentPrice);
            }
        }
        
        // 체결 실행
        if (shouldFill) {
            try {
                // 지정가 주문이므로 체결가는 주문가
                orderService.fillOrder(order.getId());
                log.info("주문 자동 체결 완료 - 주문 ID: {}, 심볼: {}, 체결가: {}", 
                        order.getId(), order.getStockCode(), order.getOrderPrice());
            } catch (Exception e) {
                log.error("주문 체결 실패 - 주문 ID: {}, 에러: {}", order.getId(), e.getMessage());
            }
        }
    }
}
