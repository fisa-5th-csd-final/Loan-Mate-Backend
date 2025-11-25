package com.fisa.bank.common.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fisa.bank.common.util.CookieUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

  @Value("${app.front-fail-url}")
  private String frontFailUrl;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {

    log.error("OAuth2 로그인 실패: {}", exception.getMessage());

    // accessToken 삭제
    response.addCookie(CookieUtil.deleteCookie("accessToken"));

    // refreshToken 삭제
    response.addCookie(CookieUtil.deleteCookie("refreshToken"));

    // 실패 시 도메인으로 리다이렉트
    response.sendRedirect(frontFailUrl);
  }
}
