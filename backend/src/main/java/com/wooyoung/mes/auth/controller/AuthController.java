package com.wooyoung.mes.auth.controller;

import com.wooyoung.mes.auth.dto.LoginRequest;
import com.wooyoung.mes.auth.dto.LoginTokenResponse;
import com.wooyoung.mes.auth.dto.RefreshRequest;
import com.wooyoung.mes.auth.dto.SignupRequest;
import com.wooyoung.mes.auth.dto.ChangePasswordRequest;
import com.wooyoung.mes.auth.jwt.JwtProvider;
import com.wooyoung.mes.common.exception.AuthException;
import com.wooyoung.mes.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    /**
     * 🔐 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("USER CREATED");
    }

    /**
     * 🔐 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginTokenResponse> login(@RequestBody LoginRequest request)
            throws AuthException {

        LoginTokenResponse response =
                userService.login(request.getUserId(), request.getPassword());

        return ResponseEntity.ok(response);
    }

    /**
     * 🔄 AccessToken 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody RefreshRequest request)
            throws AuthException {

        String newAccessToken = userService.refresh(request.getRefreshToken());

        return ResponseEntity.ok(newAccessToken);
    }

    /**
     * 🔓 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String token) {

        String accessToken = token.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(accessToken);

        userService.logout(userId);

        return ResponseEntity.ok("로그아웃 완료");
    }

    /**
     * 🔑 비밀번호 변경
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordRequest request)
            throws AuthException {

        String accessToken = token.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(accessToken);

        userService.changePassword(
                userId,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok("비밀번호 변경 완료");
    }

    /**
     * 🔄 비밀번호 180일 연장 (최대 3회)
     */
    @PostMapping("/password-extend")
    public ResponseEntity<String> extendPassword(
            @RequestHeader("Authorization") String token)
            throws AuthException {

        String accessToken = token.replace("Bearer ", "");
        String userId = jwtProvider.getUserId(accessToken);

        userService.extendPassword(userId);

        return ResponseEntity.ok("비밀번호 180일 연장 완료");
    }
}