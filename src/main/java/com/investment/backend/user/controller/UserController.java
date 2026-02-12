package com.investment.backend.user.controller;

import com.investment.backend.auth.dto.TokenResponse;
import com.investment.backend.user.dto.UserAdditionalInfoRequest;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/additional-info")
    public ResponseEntity<TokenResponse> updateAdditionalInfo(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserAdditionalInfoRequest request) {

        // detached entity 대신 email로 DB에서 직접 조회하여 업데이트
        TokenResponse response = userService.updateAdditionalInfo(user.getEmail(), request);

        return ResponseEntity.ok(response);
    }
}