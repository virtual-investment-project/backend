package com.investment.backend.comment.controller;

import com.investment.backend.comment.dto.CommentResponse;
import com.investment.backend.comment.dto.CreateCommentRequest;
import com.investment.backend.comment.service.CommentService;
import com.investment.backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/battles/{battleId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID battleId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User user) {

        CommentResponse response = commentService.createComment(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 Battle의 댓글 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID battleId) {
        List<CommentResponse> comments = commentService.getCommentsByBattle(battleId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 삭제 (soft delete)
     * 삭제 가능: 본인 | 해당 배틀 팀장 | ADMIN
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID battleId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {

        commentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }
}
