package com.wooyoung.mes.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "MES_USER")
@Getter
@Setter
public class MesUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String roleCode;

    @Column(nullable = false)
    private String companyType;

    // 계정 상태 (ACTIVE / DORMANT / LOCKED / DISABLED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    // 마지막 로그인 시간
    private LocalDateTime lastLoginAt;

    // 휴면 전환 시점
    private LocalDateTime dormantAt;

    // 계정 잠금 시점
    private LocalDateTime lockedAt;

    // 로그인 실패 횟수
    @Column(nullable = false)
    private Integer loginFailCount = 0;

    // 비밀번호 변경일
    private LocalDateTime passwordChangedAt;
    
    @Column
    private LocalDateTime passwordExpiredAt;

    @Column(nullable = false)
    private Integer passwordExtendCount = 0;

    @Column(nullable = false)
    private Boolean passwordForceChange = false;
}