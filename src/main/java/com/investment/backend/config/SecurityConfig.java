package com.investment.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.investment.backend.jwt.filter.JwtAuthenticationFilter;
import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.repository.UserRepository;


@Configuration // 설정 파일 등록
@EnableWebSecurity // 스프링 시큐리티 기능 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Bean // 이 메소드가 리턴하는 객체를 스프링이 관리하게 함
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 끄기 (테스트 편의상. 실무에선 앱 방식에 따라 설정)
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 안 씀
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 안 씀
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 끄기

                // URL별 권한 관리
                .authorizeHttpRequests(auth -> auth
                        // 인증 관련 API (공개)
                        .requestMatchers("/api/auth/**").permitAll()
                        // 메인페이지 공개 API (조회만 허용)
                        .requestMatchers(HttpMethod.GET, "/api/battles", "/api/battles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rankings/**").permitAll()
                        // 기타 공개 페이지
                        .requestMatchers("/test", "/login-success").permitAll()
                        // 나머지는 모두 인증 필요
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
