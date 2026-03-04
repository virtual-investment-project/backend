package com.investment.backend.jwt.util;

import com.investment.backend.user.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    // 테스트용 임시 비밀키 (Base64 인코딩됨, 실제값 아니어도 됨)
    private static final String SECRET_KEY = "c2lsdmVyLTI1Mi10ZXN0LWtleS1mb3Itand0LXByb3ZpZGVyLW1ha2UtbW9yZS1sb25nZXI=";
    private static final long ACCESS_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TIME = 1000 * 60 * 60 * 24 * 14; // 2주

    // 테스트할 대상 생성 (설정파일 값 대신 직접 넣어줌)
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET_KEY, ACCESS_TIME, REFRESH_TIME);

    @Test
    @DisplayName("1. 토큰이 정상적으로 생성되는지 확인")
    void createToken() {
        // given
        UUID userId = UUID.randomUUID();
        Role role = Role.USER;

        // when
        String accessToken = jwtTokenProvider.createAccessToken(userId, role);

        // then
        System.out.println("생성된 토큰: " + accessToken);
        assertThat(accessToken).isNotNull(); // 토큰이 null이면 안 됨
        assertThat(accessToken.length()).isGreaterThan(20); // 너무 짧아도 안 됨
    }

    @Test
    @DisplayName("2. 토큰에서 userId(UUID)를 다시 꺼낼 수 있는지 확인")
    void extractUserId() {
        // given
        UUID userId = UUID.randomUUID();
        Role role = Role.USER;
        String token = jwtTokenProvider.createAccessToken(userId, role);

        // when
        UUID extractedUserId = jwtTokenProvider.extractUserId(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId); // 넣은 UUID = 꺼낸 UUID
    }

    @Test
    @DisplayName("3. 유효한 토큰인지 검증하는 기능 확인")
    void validateToken() {
        // given
        String token = jwtTokenProvider.createAccessToken(UUID.randomUUID(), Role.USER);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue(); // 유효해야 함
    }
}
