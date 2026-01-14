package com.investment.backend.mypage.profile.service;

import com.investment.backend.mypage.profile.dto.ProfileResponse;
import com.investment.backend.mypage.profile.dto.ProfileUpdateRequest;
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
        user.updateProfile(request.getSchool(), request.getCompany());
        userRepository.save(user);
        return ProfileResponse.from(user);
    }

    public void logout(User user) {
        user.updateRefreshToken(null);
    }
}
