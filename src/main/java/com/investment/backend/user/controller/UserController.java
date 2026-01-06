package com.investment.backend.user.controller;

import com.investment.backend.user.dto.UserAdditionalInfoRequest;
import com.investment.backend.user.entity.User;
import com.investment.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/additional-info")
    public String updateAdditionalInfo(@AuthenticationPrincipal User user,
                                       @RequestBody UserAdditionalInfoRequest request) {

        userService.updateAdditionalInfo(user, request);

        return "정보 입력 완료!";
    }
}