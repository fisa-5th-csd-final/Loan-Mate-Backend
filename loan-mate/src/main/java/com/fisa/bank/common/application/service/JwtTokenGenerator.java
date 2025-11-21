package com.fisa.bank.common.application.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtTokenGenerator {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.access-token-expiration:900000}") // 15분
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token-expiration:604800000}") // 7일
  private long refreshTokenExpiration;

  /** Access Token 생성 */
  public String generateAccessToken(Long userId) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(accessTokenExpiration);

    Map<String, Object> claims = new HashMap<>();
    claims.put("user_id", userId);
    claims.put("token_type", "access");

    return Jwts.builder()
        .claims(claims)
        .subject(String.valueOf(userId))
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .id(UUID.randomUUID().toString())
        .signWith(getSigningKey(), Jwts.SIG.HS256)
        .compact();
  }

  /** Refresh Token 생성 */
  public String generateRefreshToken(Long userId) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(refreshTokenExpiration);

    Map<String, Object> claims = new HashMap<>();
    claims.put("user_id", userId);
    claims.put("token_type", "refresh");

    return Jwts.builder()
        .claims(claims)
        .subject(String.valueOf(userId))
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .id(UUID.randomUUID().toString())
        .signWith(getSigningKey(), Jwts.SIG.HS256) // 최신 방식
        .compact();
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }
}
