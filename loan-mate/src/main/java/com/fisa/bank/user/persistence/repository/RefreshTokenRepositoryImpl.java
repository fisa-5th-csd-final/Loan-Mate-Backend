package com.fisa.bank.user.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.user.application.repository.RefreshTokenRepository;
import com.fisa.bank.user.persistence.entity.RefreshTokenEntity;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

  private final RefreshTokenJpaRepository jpaRepository;

  @Override
  @Transactional
  public void save(Long userId, String token, Instant expiresAt) {
    Optional<RefreshTokenEntity> existing = jpaRepository.findByUserId(userId);

    if (existing.isPresent()) {
      // 기존 토큰 업데이트
      existing.get().updateToken(token, expiresAt);
    } else {
      // 새로운 토큰 생성
      RefreshTokenEntity entity = RefreshTokenEntity.create(userId, token, expiresAt);
      jpaRepository.save(entity);
    }
  }

  @Override
  public void deleteByToken(String refreshToken) {
    jpaRepository.deleteByToken(refreshToken);
  }

  @Override
  public void deleteByUserId(Long UserId) {
    jpaRepository.deleteByUserId(UserId);
  }

  @Override
  public boolean existsByToken(String token) {
    return jpaRepository.findByToken(token).map(entity -> !entity.isExpired()).orElse(false);
  }
}
