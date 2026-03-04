package com.investment.backend.mypage.service;

import com.investment.backend.mypage.dto.ProfileResponse;
import com.investment.backend.mypage.dto.ProfileUpdateRequest;
import com.investment.backend.mypage.dto.SettingsResponse;
import com.investment.backend.mypage.dto.SettingsUpdateRequest;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(User user) {
        return ProfileResponse.from(user);
    }

    public ProfileResponse updateProfile(User user, ProfileUpdateRequest request) {
        user.updateProfile(request.getNickname(), request.getSchool(), request.getCompany());
        userRepository.save(user);
        return ProfileResponse.from(user);
    }

    public void logout(User user) {
        user.updateRefreshToken(null);
    }

    @Transactional(readOnly = true)
    public SettingsResponse getSettings(User user) {
        return SettingsResponse.builder()
                .darkMode(user.getDarkMode())
                .orderExecution(user.getOrderExecution())
                .battleStart(user.getBattleStart())
                .rankChange(user.getRankChange())
                .stockPriceAlert(user.getStockPriceAlert())
                .build();
    }

    public SettingsResponse updateSettings(User user, SettingsUpdateRequest request) {
        // 다크모드 설정
        if (request.getDarkMode() != null) {
            user.updateDarkMode(request.getDarkMode());
        }

        // 알림 설정
        user.updateNotificationSettings(
                request.getOrderExecution(),
                request.getBattleStart(),
                request.getRankChange(),
                request.getStockPriceAlert());

        userRepository.save(user);
        return getSettings(user);
    }
}
