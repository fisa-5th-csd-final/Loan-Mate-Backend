package com.fisa.bank.user.presentation.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fisa.bank.common.util.CookieUtil;
import com.fisa.bank.user.application.dto.RefreshTokenResponse;
import com.fisa.bank.user.application.usecase.LogoutUseCase;
import com.fisa.bank.user.application.usecase.UpdateTokenUseCase;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

  private final UpdateTokenUseCase updateTokenUseCase;
  private final LogoutUseCase logoutUseCase;

  @Value("${jwt.refresh-token-expiration}")
  private Long refreshTokenExpiration;

  @Value("${jwt.access-token-expiration}")
  private Long accessTokenExpiration;

  @Value("${app.cookie-secure:false}")
  private boolean cookieSecure;

  @GetMapping("/login")
  public void login(HttpServletResponse response) throws IOException {
    response.sendRedirect("/oauth2/authorization/loan-mate");
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<RefreshTokenResponse> refresh(
      @CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {

    if (refreshToken == null) {
      log.warn("Refresh Token 없음 (쿠키 없음)");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    RefreshTokenResponse newTokens;
    try {
      newTokens = updateTokenUseCase.execute(refreshToken);
    } catch (IllegalArgumentException e) {
      log.warn("Refresh Token 검증 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      log.error("토큰 갱신 중 예외 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // 쿠키 재발급
    boolean secure = cookieSecure;

    Cookie newAccessCookie =
        CookieUtil.createHttpOnlyCookie(
            "accessToken",
            newTokens.accessToken(),
            (int) (accessTokenExpiration / 1000L), // 30분
            secure);

    Cookie newRefreshCookie =
        CookieUtil.createHttpOnlyCookie(
            "refreshToken",
            newTokens.refreshToken(),
            (int) (refreshTokenExpiration / 1000L), // 7일
            secure);

    response.addCookie(newAccessCookie);
    response.addCookie(newRefreshCookie);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    try {
      logoutUseCase.execute();
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("로그아웃 처리 중 예외가 발생했습니다.", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
