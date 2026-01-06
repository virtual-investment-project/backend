package com.investment.backend.auth.service;

import com.investment.backend.user.enums.Role;
import com.investment.backend.user.enums.SocialType;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j // 로그를 찍기 위한 어노테이션 (System.out.println 대신 씀)
@Service // 스프링에게 "이건 비즈니스 로직을 담당하는 녀석이야"라고 알려줌
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글에서 유저 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 어떤 소셜 서비스인지 확인 (google, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialType socialType = getSocialType(registrationId);

        // 3. 구글에서 가져온 속성들(이메일, 이름 등) 뽑아내기
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 4. 유저 정보를 저장하거나 업데이트
        User user = saveOrUpdate(attributes, socialType);

        // 5. 스프링 시큐리티가 사용할 수 있는 형태로 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }

    private User saveOrUpdate(Map<String, Object> attributes, SocialType socialType) {
        // 구글은 "sub"이라는 이름으로 고유 ID를 줌
        String socialId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // DB에 이미 있는 이메일인지 확인
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // 이미 있으면 그대로 반환 (나중에 정보 업데이트 로직 추가 가능)
            return userOptional.get();
        } else {
            // 없으면 새로 만들기 (회원가입)
            User user = User.builder()
                    .email(email)
                    .name(name)
                    .socialType(socialType)
                    .socialId(socialId)
                    .role(Role.GUEST) // 처음 가입하면 GUEST 권한
                    .build();
            return userRepository.save(user);
        }
    }

    private SocialType getSocialType(String registrationId) {
        if("google".equals(registrationId)) {
            return SocialType.GOOGLE;
        }
        return SocialType.KAKAO;
    }
}