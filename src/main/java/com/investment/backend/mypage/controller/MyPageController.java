package com.investment.backend.mypage.controller;

import com.investment.backend.mypage.dto.ProfileResponse;
import com.investment.backend.mypage.dto.ProfileUpdateRequest;
import com.investment.backend.mypage.dto.SettingsResponse;
import com.investment.backend.mypage.dto.SettingsUpdateRequest;
import com.investment.backend.mypage.service.MyPageService;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        ProfileResponse profile = myPageService.getProfile(user);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal User user,
            @RequestBody ProfileUpdateRequest request) {
        ProfileResponse updatedProfile = myPageService.updateProfile(user, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal User user) {
        myPageService.logout(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingsResponse> getSettings(@AuthenticationPrincipal User user) {
        SettingsResponse settings = myPageService.getSettings(user);
        return ResponseEntity.ok(settings);
    }

    @PatchMapping("/settings")
    public ResponseEntity<SettingsResponse> updateSettings(@AuthenticationPrincipal User user,
            @RequestBody SettingsUpdateRequest request) {
        SettingsResponse updatedSettings = myPageService.updateSettings(user, request);
        return ResponseEntity.ok(updatedSettings);
    }
}
