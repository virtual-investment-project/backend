package com.investment.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 오류 (잔액 부족, 종목 제한 등)
     * → 400 Bad Request + { "message": "..." }
     *
     * 이 핸들러가 없으면 IllegalArgumentException이 Spring Boot 기본 에러
     * 디스패처(/error)로 전달되고, /error 엔드포인트가 SecurityConfig의
     * permitAll() 목록에 없어 Security 필터에서 401을 반환하는 문제가 발생함.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * 상태 오류 (배틀 미시작, 이미 체결된 주문 등)
     * → 400 Bad Request + { "message": "..." }
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }
}
