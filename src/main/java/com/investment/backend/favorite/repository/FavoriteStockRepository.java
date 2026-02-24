package com.investment.backend.favorite.repository;

import com.investment.backend.favorite.entity.FavoriteStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteStockRepository extends JpaRepository<FavoriteStock, Long> {

    // 유저의 즐겨찾기 조회
    List<FavoriteStock> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // 유저의 특정 심볼 즐겨찾기 조회
    Optional<FavoriteStock> findByUserIdAndSymbol(UUID userId, String symbol);

    // 유저의 특정 심볼 즐겨찾기 존재 여부 확인
    boolean existsByUserIdAndSymbol(UUID userId, String symbol);

    // 유저의 특정 심볼 즐겨찾기 삭제
    void deleteByUserIdAndSymbol(UUID userId, String symbol);

    // 모든 즐겨찾기 조회 (가격 알림용)
    List<FavoriteStock> findAll();
}
