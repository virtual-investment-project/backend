package com.investment.backend.comment.dto;

import com.investment.backend.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private UUID battleId;
    private UUID userId;
    private String userNickname;
    private Long parentId;
    private String content;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> replies; // 대댓글 목록

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .battleId(comment.getBattle().getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .isDeleted(comment.getIsDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(null)
                .build();
    }

    public static CommentResponse fromWithReplies(Comment comment, List<CommentResponse> replies) {
        return CommentResponse.builder()
                .id(comment.getId())
                .battleId(comment.getBattle().getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .parentId(null)
                .content(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .isDeleted(comment.getIsDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replies)
                .build();
    }
}
