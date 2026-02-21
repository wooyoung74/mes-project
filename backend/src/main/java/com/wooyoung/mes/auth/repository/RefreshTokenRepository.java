package com.wooyoung.mes.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wooyoung.mes.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(String userId);

    void deleteByToken(String token);
    
    Optional<RefreshToken> findByUserId(String userId);
}
