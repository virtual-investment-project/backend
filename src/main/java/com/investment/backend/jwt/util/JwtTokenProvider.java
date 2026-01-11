package com.investment.backend.jwt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    // application.yml에서 설정한 값들을 가져옵니다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-expiration}") long accessTokenValidityInMilliseconds,
                            @Value("${jwt.refresh-expiration}") long refreshTokenValidityInMilliseconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    // 1. Access Token 생성 (이메일 정보를 담음)
    public String createAccessToken(String email) {
        return createToken(email, accessTokenValidityInMilliseconds);
    }

    // 2. Refresh Token 생성 (유효기간만 길게)
    public String createRefreshToken() {
        return createToken(null, refreshTokenValidityInMilliseconds);
    }

    // 토큰 생성 내부 로직
    private String createToken(String subject, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(subject) // 토큰 주인 (이메일)
                .setIssuedAt(now)    // 발행 시간
                .setExpiration(validity) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘
                .compact();
    }

    // 3. 토큰에서 이메일(Subject) 꺼내기
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // 4. 토큰이 유효한지 검사
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 해독(파싱)
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}