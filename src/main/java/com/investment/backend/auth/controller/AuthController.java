package com.investment.backend.auth.controller;

import com.investment.backend.auth.dto.GoogleLoginRequest;
import com.investment.backend.auth.dto.GoogleLoginResponse;
import com.investment.backend.auth.dto.RefreshTokenRequest;
import com.investment.backend.auth.dto.TokenResponse;
import com.investment.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            GoogleLoginResponse response = authService.googleLogin(request.getIdToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Google token: {}", e.getMessage());
            return ResponseEntity.status(401).body("유효하지 않은 구글 토큰입니다.");
        } catch (Exception e) {
            log.error("Google login error", e);
            return ResponseEntity.status(500).body("서버 내부 오류가 발생했습니다.");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            TokenResponse response = authService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 내부 오류가 발생했습니다.");
        }
    }
}
