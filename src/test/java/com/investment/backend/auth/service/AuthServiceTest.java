package com.investment.backend.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.investment.backend.auth.dto.GoogleLoginResponse;
import com.investment.backend.auth.dto.TokenResponse;
import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;
import com.investment.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private final String googleClientId = "test-google-client-id";
    private final String testEmail = "test@gmail.com";
    private final String testName = "테스트 사용자";
    private final String testSocialId = "google123";
    private final String testAccessToken = "test-access-token";
    private final String testRefreshToken = "test-refresh-token";
    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "googleClientId", googleClientId);
    }

    @Test
    @DisplayName("구글 로그인 성공 - 신규 사용자")
    void googleLogin_Success_NewUser() throws Exception {
        // given
        String idTokenString = "valid-id-token";

        // Google 토큰 검증 관련 Mock
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);

        when(mockPayload.getEmail()).thenReturn(testEmail);
        when(mockPayload.get("name")).thenReturn(testName);
        when(mockPayload.getSubject()).thenReturn(testSocialId);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockVerifier.verify(idTokenString)).thenReturn(mockIdToken);

        // 사용자 조회 결과 없음 (신규 사용자)
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // 새 사용자 저장
        User newUser = User.builder()
                .email(testEmail)
                .name(testName)
                .socialType(SocialType.GOOGLE)
                .socialId(testSocialId)
                .role(Role.GUEST)
                .build();
        ReflectionTestUtils.setField(newUser, "id", testUserId);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // 토큰 생성
        when(jwtTokenProvider.createAccessToken(testUserId, Role.GUEST)).thenReturn(testAccessToken);
        when(jwtTokenProvider.createRefreshToken(testUserId)).thenReturn(testRefreshToken);

        // when
        try (MockedConstruction<GoogleIdTokenVerifier.Builder> builderConstruction = mockConstruction(
                GoogleIdTokenVerifier.Builder.class,
                (mock, context) -> {
                    when(mock.setAudience(anyList())).thenReturn(mock);
                    when(mock.build()).thenReturn(mockVerifier);
                })) {

            GoogleLoginResponse result = authService.googleLogin(idTokenString);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(testAccessToken);
            assertThat(result.getRefreshToken()).isEqualTo(testRefreshToken);
            assertThat(result.getRole()).isEqualTo("GUEST");
            assertThat(result.isNewUser()).isTrue();

            verify(userRepository).findByEmail(testEmail);
            verify(userRepository).save(any(User.class));
            verify(jwtTokenProvider).createAccessToken(testUserId, Role.GUEST);
            verify(jwtTokenProvider).createRefreshToken(testUserId);
        }
    }

    @Test
    @DisplayName("구글 로그인 성공 - 기존 사용자")
    void googleLogin_Success_ExistingUser() throws Exception {
        // given
        String idTokenString = "valid-id-token";

        // Google 토큰 검증 관련 Mock
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);

        when(mockPayload.getEmail()).thenReturn(testEmail);
        when(mockPayload.get("name")).thenReturn(testName);
        when(mockPayload.getSubject()).thenReturn(testSocialId);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockVerifier.verify(idTokenString)).thenReturn(mockIdToken);

        // 기존 사용자 조회
        User existingUser = User.builder()
                .email(testEmail)
                .name(testName)
                .socialType(SocialType.GOOGLE)
                .socialId(testSocialId)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(existingUser, "id", testUserId);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(existingUser));

        // 토큰 생성
        when(jwtTokenProvider.createAccessToken(testUserId, Role.USER)).thenReturn(testAccessToken);
        when(jwtTokenProvider.createRefreshToken(testUserId)).thenReturn(testRefreshToken);

        // when
        try (MockedConstruction<GoogleIdTokenVerifier.Builder> builderConstruction = mockConstruction(
                GoogleIdTokenVerifier.Builder.class,
                (mock, context) -> {
                    when(mock.setAudience(anyList())).thenReturn(mock);
                    when(mock.build()).thenReturn(mockVerifier);
                })) {

            GoogleLoginResponse result = authService.googleLogin(idTokenString);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(testAccessToken);
            assertThat(result.getRefreshToken()).isEqualTo(testRefreshToken);
            assertThat(result.getRole()).isEqualTo("USER");
            assertThat(result.isNewUser()).isFalse();

            verify(userRepository).findByEmail(testEmail);
            verify(userRepository, never()).save(any(User.class)); // 기존 사용자이므로 저장 X
            verify(jwtTokenProvider).createAccessToken(testUserId, Role.USER);
            verify(jwtTokenProvider).createRefreshToken(testUserId);
        }
    }

    @Test
    @DisplayName("구글 로그인 실패 - 유효하지 않은 토큰")
    void googleLogin_Fail_InvalidToken() throws Exception {
        // given
        String invalidIdTokenString = "invalid-id-token";

        GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
        when(mockVerifier.verify(invalidIdTokenString)).thenReturn(null); // null 반환으로 유효하지 않은 토큰 시뮬레이션

        // when & then
        try (MockedConstruction<GoogleIdTokenVerifier.Builder> builderConstruction = mockConstruction(
                GoogleIdTokenVerifier.Builder.class,
                (mock, context) -> {
                    when(mock.setAudience(anyList())).thenReturn(mock);
                    when(mock.build()).thenReturn(mockVerifier);
                })) {

            assertThatThrownBy(() -> authService.googleLogin(invalidIdTokenString))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("로그인 처리 중 오류 발생");
        }
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 갱신 성공")
    void refreshAccessToken_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        // SHA-256 해시값 계산
        String hashedRefreshToken = org.apache.commons.codec.digest.DigestUtils.sha256Hex(refreshToken);

        User user = User.builder()
                .email(testEmail)
                .name(testName)
                .role(Role.USER)
                .refreshToken(hashedRefreshToken)
                .build();
        ReflectionTestUtils.setField(user, "id", testUserId);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(refreshToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createAccessToken(testUserId, Role.USER)).thenReturn(newAccessToken);
        when(jwtTokenProvider.createRefreshToken(testUserId)).thenReturn(newRefreshToken);

        // when
        TokenResponse result = authService.refreshAccessToken(refreshToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(jwtTokenProvider).extractUserId(refreshToken);
        verify(userRepository).findById(testUserId);
        verify(jwtTokenProvider).createAccessToken(testUserId, Role.USER);
        verify(jwtTokenProvider).createRefreshToken(testUserId);
    }

    @Test
    @DisplayName("Refresh Token 갱신 실패 - 유효하지 않은 토큰")
    void refreshAccessToken_Fail_InvalidToken() {
        // given
        String invalidRefreshToken = "invalid-refresh-token";
        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(invalidRefreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Refresh Token입니다.");

        verify(jwtTokenProvider).validateToken(invalidRefreshToken);
        verifyNoMoreInteractions(jwtTokenProvider, userRepository);
    }

    @Test
    @DisplayName("Refresh Token 갱신 실패 - 사용자를 찾을 수 없음")
    void refreshAccessToken_Fail_UserNotFound() {
        // given
        String refreshToken = "valid-refresh-token";
        UUID nonExistentUserId = UUID.randomUUID();

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(refreshToken)).thenReturn(nonExistentUserId);
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(jwtTokenProvider).extractUserId(refreshToken);
        verify(userRepository).findById(nonExistentUserId);
    }

    @Test
    @DisplayName("Refresh Token 갱신 실패 - DB에 저장된 토큰과 불일치")
    void refreshAccessToken_Fail_TokenMismatch() {
        // given
        String refreshToken = "valid-refresh-token";
        String wrongHashedToken = "wrong-hashed-token";

        User user = User.builder()
                .email(testEmail)
                .name(testName)
                .role(Role.USER)
                .refreshToken(wrongHashedToken)
                .build();
        ReflectionTestUtils.setField(user, "id", testUserId);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(refreshToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 일치하지 않습니다.");

        verify(jwtTokenProvider, never()).createAccessToken(any(UUID.class), any(Role.class));
    }

    @Test
    @DisplayName("Refresh Token 갱신 실패 - DB에 Refresh Token이 null")
    void refreshAccessToken_Fail_NullRefreshToken() {
        // given
        String refreshToken = "valid-refresh-token";

        User user = User.builder()
                .email(testEmail)
                .name(testName)
                .role(Role.USER)
                .refreshToken(null) // null인 경우
                .build();
        ReflectionTestUtils.setField(user, "id", testUserId);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(refreshToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 일치하지 않습니다.");

        verify(jwtTokenProvider).validateToken(refreshToken);
        verify(jwtTokenProvider).extractUserId(refreshToken);
        verify(userRepository).findById(testUserId);
        verify(jwtTokenProvider, never()).createAccessToken(any(UUID.class), any(Role.class));
    }
}
