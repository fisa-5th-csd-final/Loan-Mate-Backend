package com.fisa.bank.user.presentation.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fisa.bank.common.application.service.JwtTokenGenerator;
import com.fisa.bank.common.application.service.JwtTokenValidator;
import com.fisa.bank.user.application.dto.RefreshTokenRequest;
import com.fisa.bank.user.application.dto.RefreshTokenResponse;
import com.fisa.bank.user.application.repository.RefreshTokenRepository;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenGenerator jwtTokenGenerator;
  private final JwtTokenValidator jwtTokenValidator;
  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${jwt.refresh-token-expiration:604800000}")
  private long refreshTokenExpiration;

  @GetMapping("/api/login")
  public void login(HttpServletResponse response) throws IOException {
    response.sendRedirect("/oauth2/authorization/loan-mate");
  }

  @PostMapping("/api/auth/refresh")
  public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
    try {
      log.info("토큰 갱신 요청. refreshToken: {}", request.refreshToken());

      // Refresh Token 검증 및 userId 추출
      Long userId = jwtTokenValidator.validateRefreshTokenAndGetUserId(request.refreshToken());
      log.info("Refresh Token 검증 성공. userId: {}", userId);

      // DB에서 Refresh Token 확인
      if (!refreshTokenRepository.existsByToken(request.refreshToken())) {
        log.warn("DB에 저장되지 않은 Refresh Token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      // 기존 Refresh Token 제거 (rotation)
      refreshTokenRepository.deleteByUserId(userId);
      log.info("기존 Refresh Token 삭제 완료. userId: {}", userId);

      // 새로운 Access/Refresh Token 발급
      String newAccessToken = jwtTokenGenerator.generateAccessToken(userId);
      String newRefreshToken = jwtTokenGenerator.generateRefreshToken(userId);
      Instant refreshTokenExpiry = Instant.now().plusMillis(refreshTokenExpiration);
      refreshTokenRepository.save(userId, newRefreshToken, refreshTokenExpiry);
      log.info("새로운 Access/Refresh Token 발급 및 저장 완료. userId: {}", userId);

      return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, newRefreshToken));

    } catch (IllegalArgumentException e) {
      log.error("Refresh Token 검증 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      log.error("토큰 갱신 중 예외 발생: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
