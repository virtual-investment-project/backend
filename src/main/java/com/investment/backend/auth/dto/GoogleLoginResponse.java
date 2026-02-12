package com.investment.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GoogleLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
    private boolean isNewUser;
}
