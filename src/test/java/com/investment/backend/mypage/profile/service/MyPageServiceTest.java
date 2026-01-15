package com.investment.backend.mypage.profile.service;

import com.investment.backend.mypage.profile.dto.ProfileResponse;
import com.investment.backend.mypage.profile.dto.ProfileUpdateRequest;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;
import com.investment.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    @DisplayName("현재 로그인한 사용자의 프로필 정보를 조회할 수 있다")
    void getProfile() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .role(Role.USER)
                .age(25)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .refreshToken("valid-refresh-token")
                .build();
        
        ReflectionTestUtils.setField(user, "nickname", "투자왕");
        ReflectionTestUtils.setField(user, "school", "서울대학교");
        ReflectionTestUtils.setField(user, "company", "네이버");

        // when
        ProfileResponse response = myPageService.getProfile(user);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getNickname()).isEqualTo("투자왕");
        assertThat(response.getAge()).isEqualTo(25);
        assertThat(response.getSchool()).isEqualTo("서울대학교");
        assertThat(response.getCompany()).isEqualTo("네이버");
    }

    @Test
    @DisplayName("학교와 회사 정보를 수정할 수 있다")
    void updateProfile() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .role(Role.USER)
                .age(25)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .refreshToken("valid-refresh-token")
                .build();
        
        ReflectionTestUtils.setField(user, "school", "연세대학교");
        ReflectionTestUtils.setField(user, "company", "카카오");

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        ReflectionTestUtils.setField(request, "school", "고려대학교");
        ReflectionTestUtils.setField(request, "company", "라인");

        // when
        ProfileResponse response = myPageService.updateProfile(user, request);

        // then
        assertThat(user.getSchool()).isEqualTo("고려대학교");
        assertThat(user.getCompany()).isEqualTo("라인");
        assertThat(response.getSchool()).isEqualTo("고려대학교");
        assertThat(response.getCompany()).isEqualTo("라인");
        
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("학교 정보만 수정할 수 있다")
    void updateProfileOnlySchool() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .role(Role.USER)
                .age(25)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .refreshToken("valid-refresh-token")
                .build();
        
        ReflectionTestUtils.setField(user, "school", "연세대학교");
        ReflectionTestUtils.setField(user, "company", "카카오");

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        ReflectionTestUtils.setField(request, "school", "서울대학교");
        ReflectionTestUtils.setField(request, "company", null);

        // when
        ProfileResponse response = myPageService.updateProfile(user, request);

        // then
        assertThat(user.getSchool()).isEqualTo("서울대학교");
        assertThat(user.getCompany()).isEqualTo("카카오"); // 변경되지 않음
        assertThat(response.getSchool()).isEqualTo("서울대학교");
        assertThat(response.getCompany()).isEqualTo("카카오");
        
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("회사 정보만 수정할 수 있다")
    void updateProfileOnlyCompany() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .role(Role.USER)
                .age(25)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .refreshToken("valid-refresh-token")
                .build();
        
        ReflectionTestUtils.setField(user, "school", "연세대학교");
        ReflectionTestUtils.setField(user, "company", "카카오");

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        ReflectionTestUtils.setField(request, "school", null);
        ReflectionTestUtils.setField(request, "company", "네이버");

        // when
        ProfileResponse response = myPageService.updateProfile(user, request);

        // then
        assertThat(user.getSchool()).isEqualTo("연세대학교"); // 변경되지 않음
        assertThat(user.getCompany()).isEqualTo("네이버");
        assertThat(response.getSchool()).isEqualTo("연세대학교");
        assertThat(response.getCompany()).isEqualTo("네이버");
        
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("로그아웃 시 RefreshToken이 null로 업데이트된다")
    void logout() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("홍길동")
                .role(Role.USER)
                .age(25)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .refreshToken("valid-refresh-token")
                .build();

        // when
        myPageService.logout(user);

        // then
        assertThat(user.getRefreshToken()).isNull();
    }
}
