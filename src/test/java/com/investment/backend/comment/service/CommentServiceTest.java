package com.investment.backend.comment.service;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleType;
import com.investment.backend.battle.enums.MetricType;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.comment.dto.CommentResponse;
import com.investment.backend.comment.dto.CreateCommentRequest;
import com.investment.backend.comment.entity.Comment;
import com.investment.backend.comment.repository.CommentRepository;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BattleRepository battleRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Battle testBattle;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@test.com")
                .name("테스터")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(testUser, "nickname", "테스터닉네임");

        testBattle = Battle.builder()
                .type(BattleType.NORMAL)
                .name("테스트 대결")
                .ticker("BTC")
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(7))
                .metricType(MetricType.RATE)
                .initialCapital(1000000)
                .build();
        ReflectionTestUtils.setField(testBattle, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("원댓글 작성 성공")
    void createComment_ParentComment_Success() {
        // given
        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "battleId", testBattle.getId());
        ReflectionTestUtils.setField(request, "content", "테스트 댓글입니다.");
        ReflectionTestUtils.setField(request, "parentId", null);

        Comment savedComment = Comment.builder()
                .battle(testBattle)
                .user(testUser)
                .parent(null)
                .content("테스트 댓글입니다.")
                .build();
        ReflectionTestUtils.setField(savedComment, "id", 1L);

        when(battleRepository.findById(testBattle.getId())).thenReturn(Optional.of(testBattle));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // when
        CommentResponse response = commentService.createComment(request, testUser);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContent()).isEqualTo("테스트 댓글입니다.");
        assertThat(response.getParentId()).isNull();
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 작성 성공")
    void createComment_Reply_Success() {
        // given
        Comment parentComment = Comment.builder()
                .battle(testBattle)
                .user(testUser)
                .parent(null)
                .content("원댓글입니다.")
                .build();
        ReflectionTestUtils.setField(parentComment, "id", 1L);

        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "battleId", testBattle.getId());
        ReflectionTestUtils.setField(request, "content", "대댓글입니다.");
        ReflectionTestUtils.setField(request, "parentId", 1L);

        Comment savedReply = Comment.builder()
                .battle(testBattle)
                .user(testUser)
                .parent(parentComment)
                .content("대댓글입니다.")
                .build();
        ReflectionTestUtils.setField(savedReply, "id", 2L);

        when(battleRepository.findById(testBattle.getId())).thenReturn(Optional.of(testBattle));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedReply);

        // when
        CommentResponse response = commentService.createComment(request, testUser);

        // then
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getContent()).isEqualTo("대댓글입니다.");
        assertThat(response.getParentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("대대댓글 시도 시 예외 발생")
    void createComment_NestedReply_ThrowsException() {
        // given
        Comment parentComment = Comment.builder()
                .battle(testBattle)
                .user(testUser)
                .parent(null)
                .content("원댓글입니다.")
                .build();
        ReflectionTestUtils.setField(parentComment, "id", 1L);

        Comment replyComment = Comment.builder()
                .battle(testBattle)
                .user(testUser)
                .parent(parentComment) // 이미 대댓글
                .content("대댓글입니다.")
                .build();
        ReflectionTestUtils.setField(replyComment, "id", 2L);

        CreateCommentRequest request = new CreateCommentRequest();
        ReflectionTestUtils.setField(request, "battleId", testBattle.getId());
        ReflectionTestUtils.setField(request, "content", "대대댓글 시도");
        ReflectionTestUtils.setField(request, "parentId", 2L); // 대댓글에 답글 시도

        when(battleRepository.findById(testBattle.getId())).thenReturn(Optional.of(testBattle));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(replyComment));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(request, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대댓글에는 답글을 달 수 없습니다.");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("본인 댓글 삭제 성공")
    void deleteComment_ByOwner_Success() {
        // given
        Comment comment = Comment.builder()
                .battle(testBattle)
                .user(testUser)
                .content("삭제할 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(1L, testUser);

        // then
        assertThat(comment.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("타인 댓글 삭제 시도 시 예외 발생")
    void deleteComment_ByOther_ThrowsException() {
        // given
        User otherUser = User.builder()
                .email("other@test.com")
                .name("다른유저")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(otherUser, "id", UUID.randomUUID());

        Comment comment = Comment.builder()
                .battle(testBattle)
                .user(testUser) // 원래 작성자
                .content("삭제할 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(1L, otherUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한이 없습니다.");
    }
}
