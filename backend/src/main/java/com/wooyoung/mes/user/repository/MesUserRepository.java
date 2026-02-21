package com.wooyoung.mes.user.repository;

import com.wooyoung.mes.user.entity.MesUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MesUserRepository extends JpaRepository<MesUser, Long> {

    Optional<MesUser> findByUserId(String userId);

    Optional<MesUser> findByEmail(String email);
}