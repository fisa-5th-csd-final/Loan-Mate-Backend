package com.fisa.bank.user.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.user.persistence.entity.RefreshTokenEntity;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

  Optional<RefreshTokenEntity> findByToken(String token);

  Optional<RefreshTokenEntity> findByUserId(Long userId);

  void deleteByUserId(Long userId);

  void deleteByToken(String refreshToken);
}
