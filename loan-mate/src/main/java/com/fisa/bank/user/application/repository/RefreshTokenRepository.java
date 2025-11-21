package com.fisa.bank.user.application.repository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

  void save(Long userId, String token, Instant expiresAt);

  Optional<String> findByUserId(Long userId);

  Optional<Long> findUserIdByToken(String token);

  void deleteByUserId(Long userId);

  boolean existsByToken(String token);
}
