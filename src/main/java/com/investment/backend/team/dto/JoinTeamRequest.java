package com.investment.backend.team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class JoinTeamRequest {

    @NotNull(message = "초대 코드는 필수입니다.")
    private UUID inviteCode;
}
