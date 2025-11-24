package com.fisa.bank.user.presentation.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fisa.bank.user.application.dto.RefreshTokenRequest;
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

  @GetMapping("/login")
  public void login(HttpServletResponse response) throws IOException {
    response.sendRedirect("/oauth2/authorization/loan-mate");
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
    try {
      RefreshTokenResponse response = updateTokenUseCase.execute(request.refreshToken());
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      log.warn("Refresh Token 검증 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (Exception e) {
      log.warn("토큰 갱신 중 예외 발생: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    logoutUseCase.execute();
    return ResponseEntity.noContent().build();
  }
}
