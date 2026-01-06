package com.investment.backend.user.entity; // 패키지명 확인!

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String school; // 학교 정보 추가하신 것 아주 좋습니다!

    @Builder
    public User(String email, String name, Role role, SocialType socialType, String socialId) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.socialType = socialType;
        this.socialId = socialId;
    }

    public void updateAdditionalInfo(String nickname, String school) {
        this.nickname = nickname;
        this.school = school;
        this.role = Role.USER; // 정보를 입력하면 정회원(USER)으로 등업
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }
}