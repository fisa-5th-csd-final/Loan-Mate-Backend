package com.fisa.bank.user.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.JwtTokenGenerator;
import com.fisa.bank.common.application.service.JwtTokenValidator;
import com.fisa.bank.user.application.dto.TokenPair;
import com.fisa.bank.user.application.repository.RefreshTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DefaultUpdateTokenUseCase implements UpdateTokenUseCase {

  private final JwtTokenValidator jwtTokenValidator;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtTokenGenerator jwtTokenGenerator;

  @Value("${jwt.refresh-token-expiration:604800000}")
  private long refreshTokenExpiration;

  @Override
  public TokenPair execute(String refreshToken) {
    log.info("토큰 갱신 요청");

    Long userId = jwtTokenValidator.validateRefreshTokenAndGetUserId(refreshToken);
    log.info("Refresh Token 검증 성공. userId: {}", userId);

    if (!refreshTokenRepository.existsByToken(refreshToken)) {
      log.warn("DB에 저장되지 않은 Refresh Token");
      throw new IllegalArgumentException("유효하지 않은 Refresh Token");
    }

    refreshTokenRepository.deleteByToken(refreshToken);
    log.info("기존 Refresh Token 삭제 완료. userId: {}", userId);

    String newAccessToken = jwtTokenGenerator.generateAccessToken(userId);
    String newRefreshToken = jwtTokenGenerator.generateRefreshToken(userId);
    Instant refreshTokenExpiry = Instant.now().plusMillis(refreshTokenExpiration);
    refreshTokenRepository.save(userId, newRefreshToken, refreshTokenExpiry);
    log.info("새로운 Access/Refresh Token 발급 및 저장 완료. userId: {}", userId);

    return new TokenPair(newAccessToken, newRefreshToken);
  }
}
