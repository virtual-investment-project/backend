package com.investment.backend.user.service;

import com.investment.backend.user.dto.UserAdditionalInfoRequest;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public void updateAdditionalInfo(User user, UserAdditionalInfoRequest request) {
        user.updateAdditionalInfo(request.getNickname(), request.getSchool());
        userRepository.save(user);
    }
}