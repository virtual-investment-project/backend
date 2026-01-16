package com.investment.backend.comment.service;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.comment.dto.CommentResponse;
import com.investment.backend.comment.dto.CreateCommentRequest;
import com.investment.backend.comment.entity.Comment;
import com.investment.backend.comment.repository.CommentRepository;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final BattleRepository battleRepository;

    /**
     * 댓글 생성
     * - parentId가 null이면 원댓글
     * - parentId가 있으면 대댓글 (단, parent가 이미 대댓글인 경우 거부)
     */
    public CommentResponse createComment(CreateCommentRequest request, User user) {
        Battle battle = battleRepository.findById(request.getBattleId())
                .orElseThrow(() -> new IllegalArgumentException("Battle을 찾을 수 없습니다."));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("원댓글을 찾을 수 없습니다."));

            // 대대댓글 방지: parent가 이미 대댓글인 경우 거부
            if (parent.isReply()) {
                throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다.");
            }
        }

        Comment comment = Comment.builder()
                .battle(battle)
                .user(user)
                .parent(parent)
                .content(request.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.from(savedComment);
    }

    /**
     * 특정 Battle의 댓글 목록 조회 (계층형)
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByBattle(UUID battleId) {
        List<Comment> parentComments = commentRepository.findByBattleIdAndParentIsNullOrderByCreatedAtAsc(battleId);

        return parentComments.stream()
                .map(parent -> {
                    List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(parent.getId());
                    List<CommentResponse> replyResponses = replies.stream()
                            .map(CommentResponse::from)
                            .toList();
                    return CommentResponse.fromWithReplies(parent, replyResponses);
                })
                .toList();
    }

    /**
     * 댓글 삭제 (soft delete)
     * TODO: 권한 체크 로직 추가 필요 - 어드민 또는 본인만 삭제 가능
     */
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // TODO: 권한 체크 - 어드민이거나 본인인 경우만 삭제 허용
        // if (!user.getRole().equals(Role.ADMIN) &&
        // !comment.getUser().getId().equals(user.getId())) {
        // throw new AccessDeniedException("삭제 권한이 없습니다.");
        // }

        // 임시: 본인만 삭제 가능
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        comment.softDelete();
    }
}
