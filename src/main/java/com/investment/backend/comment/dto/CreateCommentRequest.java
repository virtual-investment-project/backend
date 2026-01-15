package com.investment.backend.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "Battle ID는 필수입니다.")
    private UUID battleId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long parentId; // NULL이면 원댓글, 값이 있으면 대댓글
}
