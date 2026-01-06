package com.investment.backend.user.enums;

public enum Role {
    GUEST, // 가입 대기 (추가 정보 입력 전)
    USER,  // 일반 회원 (가입 완료)
    ADMIN  // 관리자
}