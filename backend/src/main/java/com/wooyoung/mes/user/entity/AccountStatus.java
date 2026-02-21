package com.wooyoung.mes.user.entity;

public enum AccountStatus {

    ACTIVE,     // 정상 사용자
    DORMANT,    // 90일 미로그인
    LOCKED,     // 120일 초과 잠금
    DISABLED    // 관리자 강제 비활성화

}
