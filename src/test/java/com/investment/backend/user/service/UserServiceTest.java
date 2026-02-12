package com.investment.backend.user.service;

import com.investment.backend.auth.dto.TokenResponse;
import com.investment.backend.jwt.util.JwtTokenProvider;
import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;
import com.investment.backend.user.dto.UserAdditionalInfoRequest;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("추가 정보를 입력하면 닉네임, 학교가 변경되고 DB에 저장되어야 한다")
    void updateAdditionalInfo() {
        // given
        String email = "test@gmail.com";
        User user = User.builder()
                .email(email)
                .name("테스트")
                .role(Role.GUEST)
                .age(0)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .refreshToken("")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createAccessToken(email, Role.USER)).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken(email)).thenReturn("new-refresh-token");

        UserAdditionalInfoRequest request = new UserAdditionalInfoRequest();
        ReflectionTestUtils.setField(request, "nickname", "워렌버핏");
        ReflectionTestUtils.setField(request, "age", 22);
        ReflectionTestUtils.setField(request, "school", "하버드");
        ReflectionTestUtils.setField(request, "company", "");

        // when
        TokenResponse response = userService.updateAdditionalInfo(email, request);

        // then
        assertThat(user.getNickname()).isEqualTo("워렌버핏");
        assertThat(user.getAge()).isEqualTo(22);
        assertThat(user.getSchool()).isEqualTo("하버드");
        assertThat(user.getCompany()).isEqualTo("");
        assertThat(user.getRole()).isEqualTo(Role.USER);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 추가정보 입력 시 예외 발생")
    void updateAdditionalInfo_UserNotFound() {
        // given
        String email = "nonexistent@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserAdditionalInfoRequest request = new UserAdditionalInfoRequest();
        ReflectionTestUtils.setField(request, "nickname", "테스트");
        ReflectionTestUtils.setField(request, "age", 22);

        // when & then
        assertThatThrownBy(() -> userService.updateAdditionalInfo(email, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }
}
