package com.wooyoung.mes.user.service;

import com.wooyoung.mes.auth.dto.LoginTokenResponse;
import com.wooyoung.mes.auth.dto.SignupRequest;
import com.wooyoung.mes.auth.entity.RefreshToken;
import com.wooyoung.mes.auth.repository.RefreshTokenRepository;
import com.wooyoung.mes.auth.jwt.JwtProvider;
import com.wooyoung.mes.common.exception.AuthException;
import com.wooyoung.mes.user.entity.AccountStatus;
import com.wooyoung.mes.user.entity.MesUser;
import com.wooyoung.mes.user.repository.MesUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MesUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 🔐 회원가입
     */
    @Transactional
    public void signup(SignupRequest request) {

        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        MesUser user = new MesUser();
        user.setUserId(request.getUserId());
        user.setUserName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoleCode("USER");
        user.setCompanyType(request.getCompanyType());

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setLoginFailCount(0);

        // 🔥 비밀번호 정책 초기 세팅
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordExpiredAt(LocalDateTime.now().plusDays(180));
        user.setPasswordExtendCount(0);
        user.setPasswordForceChange(false);

        userRepository.save(user);
    }

    /**
     * 🔐 로그인
     */
    @Transactional
    public LoginTokenResponse login(String userId, String password) throws AuthException {

        MesUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("존재하지 않는 사용자"));

        // 🔥 계정 상태 체크
        if (user.getAccountStatus() == AccountStatus.DORMANT) {
            throw new AuthException("휴면 계정입니다. 관리자에게 문의하세요.");
        }

        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            throw new AuthException("잠긴 계정입니다.");
        }

        if (user.getAccountStatus() == AccountStatus.DISABLED) {
            throw new AuthException("비활성화된 계정입니다.");
        }

        // 🔥 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {

            user.setLoginFailCount(user.getLoginFailCount() + 1);

            if (user.getLoginFailCount() >= 5) {
                user.setAccountStatus(AccountStatus.LOCKED);
                user.setLockedAt(LocalDateTime.now());
            }

            throw new AuthException("비밀번호가 틀렸습니다.");
        }

        // 🔥 비밀번호 강제 변경 여부 체크
        if (Boolean.TRUE.equals(user.getPasswordForceChange())) {
            throw new AuthException("비밀번호 변경이 필요합니다.");
        }

        // 🔥 비밀번호 만료 체크
        if (user.getPasswordExpiredAt() != null &&
                user.getPasswordExpiredAt().isBefore(LocalDateTime.now())) {

            if (user.getPasswordExtendCount() >= 3) {
                user.setPasswordForceChange(true);
                throw new AuthException("비밀번호 변경이 필요합니다.");
            }

            throw new AuthException("비밀번호가 만료되었습니다. 연장 또는 변경하세요.");
        }

        // 🔥 로그인 성공 처리
        user.setLoginFailCount(0);
        user.setLastLoginAt(LocalDateTime.now());

        String accessToken = jwtProvider.createAccessToken(
                user.getUserId(),
                user.getRoleCode()
        );

        String refreshTokenValue = jwtProvider.createRefreshToken(user.getUserId());

        refreshTokenRepository.deleteByUserId(user.getUserId());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getUserId())
                .token(refreshTokenValue)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);

        return new LoginTokenResponse(accessToken, refreshTokenValue);
    }

    /**
     * 🔑 비밀번호 변경
     */
    @Transactional
    public void changePassword(String userId,
                               String currentPassword,
                               String newPassword) throws AuthException {

        MesUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("사용자 없음"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AuthException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordExpiredAt(LocalDateTime.now().plusDays(180));
        user.setPasswordExtendCount(0);
        user.setPasswordForceChange(false);

        // 보안상 재로그인 요구
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 🔄 비밀번호 180일 연장 (최대 3회)
     */
    @Transactional
    public void extendPassword(String userId) throws AuthException {

        MesUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("사용자 없음"));

        if (Boolean.TRUE.equals(user.getPasswordForceChange())) {
            throw new AuthException("강제 변경 대상입니다.");
        }

        if (user.getPasswordExtendCount() >= 3) {
            user.setPasswordForceChange(true);
            throw new AuthException("연장 횟수 초과. 비밀번호 변경 필요.");
        }

        user.setPasswordExpiredAt(user.getPasswordExpiredAt().plusDays(180));
        user.setPasswordExtendCount(user.getPasswordExtendCount() + 1);
    }

    /**
     * 🔓 로그아웃
     */
    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);
    }
    
    /**
     * 🔄 Refresh Token으로 AccessToken 재발급
     */
    @Transactional
    public String refresh(String refreshToken) throws AuthException {

        // 1️ JWT 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthException("유효하지 않은 Refresh Token");
        }

        // 2️ DB에서 refreshToken 조회
        RefreshToken savedToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new AuthException("DB에 없는 Refresh Token"));

        // 3️ 만료 시간 체크
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("Refresh Token 만료");
        }

        // 4️ 사용자 조회
        MesUser user = userRepository.findByUserId(savedToken.getUserId())
                .orElseThrow(() -> new AuthException("사용자 없음"));

        // 5️ 새 AccessToken 생성
        return jwtProvider.createAccessToken(
                user.getUserId(),
                user.getRoleCode()
        );
    }
}