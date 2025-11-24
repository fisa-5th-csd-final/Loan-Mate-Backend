package com.fisa.bank.user.application.repository;

import java.time.Instant;

public interface RefreshTokenRepository {

  void save(Long userId, String token, Instant expiresAt);

  void deleteByToken(String refreshToken);

  boolean existsByToken(String token);

  void deleteByUserId(Long UserId);
}
