package com.investment.backend.user.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("추가 정보를 입력하면 닉네임, 학교가 변경되고 DB에 저장되어야 한다")
    void updateAdditionalInfo() {
        // given
        User user = User.builder()
                .email("test@gmail.com")
                .name("테스트")
                .role(Role.GUEST)
                .age(0)
                .socialType(SocialType.GOOGLE)
                .socialId("google123")
                .refreshToken("")
                .build();


        UserAdditionalInfoRequest request = new UserAdditionalInfoRequest();
        ReflectionTestUtils.setField(request, "nickname", "워렌버핏");
        ReflectionTestUtils.setField(request, "age", 22);
        ReflectionTestUtils.setField(request, "school", "하버드");
        ReflectionTestUtils.setField(request, "company", "");

        // when
        userService.updateAdditionalInfo(user, request);

        // then
        assertThat(user.getNickname()).isEqualTo("워렌버핏");
        assertThat(user.getAge()).isEqualTo(22);
        assertThat(user.getSchool()).isEqualTo("하버드");
        assertThat(user.getCompany()).isEqualTo("");
        assertThat(user.getRole()).isEqualTo(Role.USER);

        verify(userRepository).save(user);
    }
}
