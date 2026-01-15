package com.investment.backend.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;
import com.investment.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    public Map<String, Object> googleLogin(String idTokenString) {
        try {
            // 1. 구글 토큰 검증
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new IllegalArgumentException("Invalid ID Token");
            }

            // 2. 유저 정보 추출
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String socialId = payload.getSubject();

            // 3. DB 저장 또는 조회 (비즈니스 로직)
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(User.builder()
                            .email(email)
                            .name(name)
                            .socialType(SocialType.GOOGLE)
                            .socialId(socialId)
                            .role(Role.GUEST)
                            .build()));

            // 4. 앱으로 내려줄 응답 생성 (토큰 + Role)
            String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

            user.updateRefreshToken(refreshToken);

            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("role", user.getRole().name());

            return result;

        } catch (Exception e) {
            log.error("Google Login Process Error", e);
            throw new RuntimeException("로그인 처리 중 오류 발생");
        }
    }
}