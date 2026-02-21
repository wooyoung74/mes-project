package com.wooyoung.mes.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    //관리자만 사용자 생성 가능
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String createUser() {
        return "사용자 생성 완료";
    }

    //USER도 가능 (본인 정보 조회)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/me")
    public String myInfo() {
        return "내 정보 조회";
    }
}