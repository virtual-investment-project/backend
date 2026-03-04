package com.investment.backend.mypage.dto;

import com.investment.backend.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProfileResponse {

    private UUID userId;
    private String email;
    private String name;
    private String nickname;
    private Integer age;
    private String school;
    private String company;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .age(user.getAge())
                .school(user.getSchool())
                .company(user.getCompany())
                .build();
    }
}
