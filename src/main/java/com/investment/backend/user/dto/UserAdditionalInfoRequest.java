package com.investment.backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserAdditionalInfoRequest {
    private String nickname;
    private String school;
}