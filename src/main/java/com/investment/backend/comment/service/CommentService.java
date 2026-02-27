package com.investment.backend.comment.service;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.comment.dto.CommentResponse;
import com.investment.backend.comment.dto.CreateCommentRequest;
import com.investment.backend.comment.entity.Comment;
import com.investment.backend.comment.repository.CommentRepository;
import com.investment.backend.team.repository.TeamUserRepository;
import com.investment.backend.team.enums.TeamUserRole;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
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
    private final TeamUserRepository teamUserRepository;

    /**
     * 댓글 생성
     * - parentId가 null이면 원댓글
     * - parentId가 있으면 대댓글 (단, parent가 이미 대댓글인 경우 거부)
     */
    public CommentResponse createComment(CreateCommentRequest request, User user) {
        Battle battle = battleRepository.findById(request.getBattleId())
                .orElseThrow(() -> new IllegalArgumentException("Battle을 찾을 수 없습니다."));

        // 배틀에 참여한 팀의 팀원만 댓글 작성 가능
        boolean isBattleParticipant = teamUserRepository.existsByTeam_Battle_IdAndUserIdAndStatus(
                battle.getId(), user.getId(), TeamUserStatus.ACTIVE);
        if (!isBattleParticipant) {
            throw new IllegalArgumentException("해당 배틀에 참여한 팀원만 댓글을 작성할 수 있습니다.");
        }

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
     * 삭제 가능: 본인 | 해당 배틀 팀장 | ADMIN
     */
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isBattleLeader = teamUserRepository.existsByTeam_Battle_IdAndUserIdAndStatusAndRole(
                comment.getBattle().getId(), user.getId(),
                TeamUserStatus.ACTIVE, TeamUserRole.LEADER);

        if (!isAuthor && !isAdmin && !isBattleLeader) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        comment.softDelete();
    }
}
