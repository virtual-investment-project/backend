package com.investment.backend.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${oauth.redirect.failure-url}")
    private String failureRedirectUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        log.error("OAuth2 로그인 실패!", exception);
        
        // 실패 원인 로깅
        String errorMessage = exception.getMessage();
        log.error("실패 원인: {}", errorMessage);
        
        // 앱으로 에러 전달 (환경변수로 설정된 URL 사용)
        String targetUrl = failureRedirectUrl + "?error=" + errorMessage;
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
