package com.investment.backend.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileUpdateRequest {

    private String nickname;

    private String school;

    private String company;
}
