package com.investment.backend.comment.repository;

import com.investment.backend.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 Battle의 원댓글만 조회 (parentId가 null인 것)
     */
    List<Comment> findByBattleIdAndParentIsNullOrderByCreatedAtAsc(UUID battleId);

    /**
     * 특정 댓글의 대댓글 목록 조회
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
