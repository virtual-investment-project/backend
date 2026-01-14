package com.investment.backend.config;

import com.investment.backend.auth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.investment.backend.auth.handler.OAuth2LoginSuccessHandler;
import com.investment.backend.auth.handler.OAuth2LoginFailureHandler;
import com.investment.backend.jwt.filter.JwtAuthenticationFilter;
import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.repository.UserRepository;


@Configuration // 설정 파일 등록
@EnableWebSecurity // 스프링 시큐리티 기능 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Bean // 이 메소드가 리턴하는 객체를 스프링이 관리하게 함
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 끄기 (테스트 편의상. 실무에선 앱 방식에 따라 설정)
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 안 씀
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 안 씀

                // URL별 권한 관리
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/test", "/login/**").permitAll() // 메인, 테스트, 로그인은 누구나 접속 가능
                        .requestMatchers("/api/users/additional-info").hasAuthority("GUEST")
                        .anyRequest().authenticated() // 그 외 모든 요청은 로그인해야 함
                )

                // 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
