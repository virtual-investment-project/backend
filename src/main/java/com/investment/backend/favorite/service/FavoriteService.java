package com.investment.backend.favorite.service;

import com.investment.backend.favorite.dto.AddFavoriteRequest;
import com.investment.backend.favorite.dto.FavoriteStockResponse;
import com.investment.backend.favorite.entity.FavoriteStock;
import com.investment.backend.favorite.repository.FavoriteStockRepository;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final FavoriteStockRepository favoriteStockRepository;

    // 즐겨찾기 목록 조회
    @Transactional(readOnly = true)
    public List<FavoriteStockResponse> getFavorites(User user) {
        return favoriteStockRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(FavoriteStockResponse::from)
                .toList();
    }

    // 즐겨찾기 추가
    public FavoriteStockResponse addFavorite(User user, AddFavoriteRequest request) {
        if (favoriteStockRepository.existsByUserIdAndSymbol(user.getId(), request.getSymbol())) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 종목입니다.");
        }

        FavoriteStock favorite = FavoriteStock.builder()
                .user(user)
                .symbol(request.getSymbol())
                .name(request.getName())
                .koreanName(request.getKoreanName())
                .build();

        FavoriteStock saved = favoriteStockRepository.save(favorite);
        return FavoriteStockResponse.from(saved);
    }

    // 즐겨찾기 제거
    public void removeFavorite(User user, String symbol) {
        favoriteStockRepository.deleteByUserIdAndSymbol(user.getId(), symbol);
    }

    // 즐겨찾기 여부 확인
    @Transactional(readOnly = true)
    public boolean isFavorite(User user, String symbol) {
        return favoriteStockRepository.existsByUserIdAndSymbol(user.getId(), symbol);
    }
}
