package com.investment.backend.user.entity; // 패키지명 확인!

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String nickname;

    @Column(nullable = false)
    private Integer age;

    private String school;

    private String company;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String name, Role role, Integer age,
            SocialType socialType, String socialId, String refreshToken) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.age = (age != null) ? age : 0;
        this.socialType = socialType;
        this.socialId = socialId;
        this.refreshToken = (refreshToken != null) ? refreshToken : "";
        this.createdAt = LocalDateTime.now();
    }

    public void updateAdditionalInfo(String nickname, Integer age, String school, String company) {
        this.nickname = nickname;
        this.age = age;
        this.school = school;
        this.company = company;
        this.role = Role.USER; // 등업
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }

    public void updateProfile(String school, String company) {
        if (school != null) {
            this.school = school;
        }
        if (company != null) {
            this.company = company;
        }
    }
}
