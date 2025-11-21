package com.fisa.bank.common.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtTokenValidator {

  @Value("${jwt.secret}")
  private String jwtSecret;

  /**
   * Access Token 검증
   *
   * @param accessToken Access Token
   * @return Claims
   */
  public Claims validateAccessToken(String accessToken) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(accessToken)
              .getPayload();

      String tokenType = (String) claims.get("token_type");
      if (!"access".equals(tokenType)) {
        throw new IllegalArgumentException("유효하지 않은 토큰 타입");
      }

      return claims;

    } catch (Exception e) {
      log.error("Access Token 검증 실패: {}", e.getMessage());
      throw new IllegalArgumentException("유효하지 않은 Access Token", e);
    }
  }

  /**
   * Refresh Token 검증 및 userId 추출
   *
   * @param refreshToken Refresh Token
   * @return userId
   */
  public Long validateRefreshTokenAndGetUserId(String refreshToken) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(getSigningKey())
              .build()
              .parseSignedClaims(refreshToken)
              .getPayload();

      String tokenType = (String) claims.get("token_type");
      if (!"refresh".equals(tokenType)) {
        throw new IllegalArgumentException("유효하지 않은 토큰 타입");
      }

      Object userIdObj = claims.get("user_id");
      if (userIdObj == null) {
        throw new IllegalArgumentException("user_id가 없습니다");
      }

      return ((Number) userIdObj).longValue();

    } catch (Exception e) {
      log.error("Refresh Token 검증 실패: {}", e.getMessage());
      throw new IllegalArgumentException("유효하지 않은 Refresh Token", e);
    }
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }
}
