package com.investment.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String hello() {
        return "투자 배틀 서버 접속 성공! 🚀";
    }

    // 👇 이 부분이 중요합니다! 핸들러가 여기로 리다이렉트 시켜줍니다.
    @GetMapping("/login-success")
    public String loginSuccess(@RequestParam String accessToken,
                               @RequestParam String refreshToken,
                               @RequestParam String role) {
        // 실제로는 여기서 JSON으로 내려주거나 앱으로 딥링크를 쏘지만,
        // 지금은 눈으로 확인하기 위해 HTML 텍스트로 보여줍니다.
        return "<html>" +
                "<body>" +
                "<h1>🎉 로그인 성공!</h1>" +
                "<h3>Access Token:</h3>" +
                "<p>" + accessToken + "</p>" +
                "<h3>Refresh Token:</h3>" +
                "<p>" + refreshToken + "</p>" +
                "<h3>Role:</h3>" +
                "<p>" + role + "</p>" +
                "</body>" +
                "</html>";
    }
}