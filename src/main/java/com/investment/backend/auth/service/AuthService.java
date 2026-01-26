package com.investment.backend.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.investment.backend.auth.dto.GoogleLoginResponse;
import com.investment.backend.auth.dto.TokenResponse;
import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;
import com.investment.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Transactional
    public GoogleLoginResponse googleLogin(String idTokenString) {
        try {
            // 구글 토큰 검증
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new IllegalArgumentException("Invalid ID Token");
            }

            // 유저 정보 추출
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String socialId = payload.getSubject();

            // DB 저장 또는 조회 (비즈니스 로직)
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(User.builder()
                            .email(email)
                            .name(name)
                            .socialType(SocialType.GOOGLE)
                            .socialId(socialId)
                            .role(Role.GUEST)
                            .build()));

            // 앱으로 내려줄 응답 생성 (토큰 + Role)
            String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
            
            // Refresh Token을 SHA-256으로 해시화하여 DB에 저장
            String hashedRefreshToken = DigestUtils.sha256Hex(refreshToken);
            user.updateRefreshToken(hashedRefreshToken);

            return GoogleLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .role(user.getRole().name())
                    .build();

        } catch (Exception e) {
            log.error("Google Login Process Error", e);
            throw new RuntimeException("로그인 처리 중 오류 발생");
        }
    }

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token에서 이메일 추출
        String email = jwtTokenProvider.extractEmail(refreshToken);

        // DB에서 사용자 조회 및 Refresh Token 일치 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DB에 저장된 해시화된 Refresh Token과 비교
        if (user.getRefreshToken() == null) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }
        
        String hashedRefreshToken = DigestUtils.sha256Hex(refreshToken);
        if (!hashedRefreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        // 새로운 Access Token과 Refresh Token 생성 (Token Rotation)
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        
        // 새로운 Refresh Token을 SHA-256으로 해시화하여 DB에 저장 (기존 토큰 무효화)
        String hashedNewRefreshToken = DigestUtils.sha256Hex(newRefreshToken);
        user.updateRefreshToken(hashedNewRefreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken) // 새로운 Refresh Token 반환
                .build();
    }
}
