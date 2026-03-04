package com.investment.backend.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserAdditionalInfoRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotNull(message = "나이는 필수입니다.")
    @Min(value = 1, message = "나이는 1살 이상이어야 합니다.")
    private Integer age;

    private String school;

    private String company;
}