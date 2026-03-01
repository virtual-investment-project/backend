package com.investment.backend.jwt.filter;

import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청 헤더에서 토큰 꺼내기
        String token = resolveToken(request);

        // 2. 토큰이 있고, 유효하다면?
        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                // 3. 토큰에서 userId(UUID) 꺼내기
                UUID userId = jwtTokenProvider.extractUserId(token);
                log.info("유효한 토큰 발견! 사용자 ID: {}", userId);

                // 4. DB에서 유저 정보 찾아서 인증 객체 만들기 (SecurityContext에 저장)
                userRepository.findById(userId).ifPresentOrElse(user -> {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("SecurityContext에 인증 객체 저장 완료: {}", userId);
                }, () -> {
                    log.warn("토큰은 유효하지만 DB에서 유저를 찾을 수 없습니다. userId: {}", userId);
                });
            } else {
                log.warn("토큰 검증 실패 (유효하지 않은 토큰)");
            }
        } else {
            log.info("요청에 토큰이 없습니다. URI: {}", request.getRequestURI());
        }

        // 5. 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }

    // 헤더에서 "Bearer " 떼고 순수 토큰만 가져오는 메소드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
