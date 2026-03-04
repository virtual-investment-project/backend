package com.investment.backend.order.repository;

import com.investment.backend.order.entity.Order;
import com.investment.backend.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    
    // 계좌별 주문 목록 조회 (최신순)
    List<Order> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    
    // 계좌별 특정 상태의 주문 조회
    List<Order> findByAccountIdAndStatus(UUID accountId, OrderStatus status);

    
    // 특정 상태의 모든 주문 조회
    List<Order> findByStatus(OrderStatus status);
}
