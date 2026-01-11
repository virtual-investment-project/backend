package com.investment.backend.auth.handler;

import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공! 토큰 생성 시작");

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // 1. 로그인된 유저의 이메일 가져오기
            // (구글, 카카오마다 속성 이름이 달라서 이렇게 가져옵니다)
            var attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            // 만약 카카오라면 map에서 kakao_account를 꺼내고 그 안에서 email을 찾아야 할 수도 있습니다.
            // 일단 구글 기준으로는 attributes.get("email")이 맞습니다.

            // 2. DB에서 유저 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));

            // 3. 토큰 생성 (Access, Refresh)
            String accessToken = jwtTokenProvider.createAccessToken(email);
            String refreshToken = jwtTokenProvider.createRefreshToken();

            // 4. DB에 Refresh Token 저장 (로그인 유지용)
            user.updateRefreshToken(refreshToken);
            userRepository.saveAndFlush(user);

            // 5. 토큰을 담아서 리다이렉트 시킬 주소 만들기
            // 나중에 프론트엔드 주소(localhost:3000 등)나 앱 스킴(investmentapp://)으로 바꿔야 합니다.
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/login-success")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .queryParam("role", user.getRole().name())
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            log.info("로그인 성공! 토큰 발급 완료. 리다이렉트 합니다: {}", targetUrl);

            // 6. 진짜로 보내버리기 (Redirect)
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("로그인 성공 후 처리 중 에러 발생", e);
            throw e;
        }
    }
}