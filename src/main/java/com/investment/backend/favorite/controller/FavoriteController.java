package com.investment.backend.favorite.controller;

import com.investment.backend.favorite.dto.AddFavoriteRequest;
import com.investment.backend.favorite.dto.FavoriteStockResponse;
import com.investment.backend.favorite.service.FavoriteService;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 즐겨찾기 목록 조회
    @GetMapping
    public ResponseEntity<List<FavoriteStockResponse>> getFavorites(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(favoriteService.getFavorites(user));
    }

    // 즐겨찾기 추가
    @PostMapping
    public ResponseEntity<FavoriteStockResponse> addFavorite(
            @AuthenticationPrincipal User user,
            @RequestBody AddFavoriteRequest request) {
        return ResponseEntity.ok(favoriteService.addFavorite(user, request));
    }

    // 즐겨찾기 제거
    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable String symbol) {
        favoriteService.removeFavorite(user, symbol);
        return ResponseEntity.ok().build();
    }

    // 즐겨찾기 여부 확인
    @GetMapping("/{symbol}/check")
    public ResponseEntity<Map<String, Boolean>> isFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable String symbol) {
        return ResponseEntity.ok(Map.of("isFavorite", favoriteService.isFavorite(user, symbol)));
    }
}
