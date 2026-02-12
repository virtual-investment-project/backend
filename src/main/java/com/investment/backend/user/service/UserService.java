package com.investment.backend.user.service;

import com.investment.backend.auth.dto.TokenResponse;
import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.dto.UserAdditionalInfoRequest;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse updateAdditionalInfo(String email, UserAdditionalInfoRequest request) {
        // DB에서 managed 상태의 엔티티를 직접 조회 (detached entity 문제 방지)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateAdditionalInfo(request.getNickname(), request.getAge(), request.getSchool(), request.getCompany());
        // managed entity이므로 dirty checking으로 자동 flush됨 (save 불필요)

        // Role이 USER로 변경되었으므로 새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}