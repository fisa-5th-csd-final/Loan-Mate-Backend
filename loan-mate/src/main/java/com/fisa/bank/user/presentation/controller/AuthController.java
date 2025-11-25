package com.fisa.bank.user.presentation.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fisa.bank.common.util.CookieUtil;
import com.fisa.bank.user.application.dto.TokenPair;
import com.fisa.bank.user.application.usecase.LogoutUseCase;
import com.fisa.bank.user.application.usecase.UpdateTokenUseCase;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

  private final UpdateTokenUseCase updateTokenUseCase;
  private final LogoutUseCase logoutUseCase;
  private final CookieUtil cookieUtil;

  @Value("${jwt.refresh-token-expiration}")
  private Long refreshTokenExpiration;

  @Value("${jwt.access-token-expiration}")
  private Long accessTokenExpiration;

  @GetMapping("/login")
  public void login(HttpServletResponse response) throws IOException {
    response.sendRedirect("/oauth2/authorization/loan-mate");
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<Void> refresh(
      @CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {

    if (refreshToken == null) {
      log.warn("Refresh Token 없음 (쿠키 없음)");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    TokenPair newTokens;
    try {
      newTokens = updateTokenUseCase.execute(refreshToken);
    } catch (IllegalArgumentException e) {
      log.warn("Refresh Token 검증 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      log.error("토큰 갱신 중 예외 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // 쿠키
    ResponseCookie newAccessCookie =
        cookieUtil.createHttpOnlyCookie(
            "accessToken", newTokens.accessToken(), (int) (accessTokenExpiration / 1000L));

    ResponseCookie newRefreshCookie =
        cookieUtil.createHttpOnlyCookie(
            "accessToken", newTokens.refreshToken(), (int) (refreshTokenExpiration / 1000L));

    response.addHeader("Set-Cookie", newRefreshCookie.toString());
    response.addHeader("Set-Cookie", newAccessCookie.toString());

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    try {
      // refreshToken 삭제
      logoutUseCase.execute();
      // 쿠키 삭제
      ResponseCookie deleteAccessToken = cookieUtil.deleteCookie("accessToken");
      ResponseCookie deleteRefreshToken = cookieUtil.deleteCookie("refreshToken");

      response.addHeader("Set-Cookie", deleteAccessToken.toString());
      response.addHeader("Set-Cookie", deleteRefreshToken.toString());

      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("로그아웃 처리 중 예외가 발생했습니다.", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
