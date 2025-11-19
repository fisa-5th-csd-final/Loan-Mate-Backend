package com.fisa.bank.common.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.user.application.dto.UserInfoResponse;
import com.fisa.bank.user.application.usecase.SyncCoreBankUserUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final CoreBankingClient coreBankingClient;
  private final SyncCoreBankUserUseCase syncCoreBankUserUseCase;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    // 코어뱅킹 /me API 호출
    UserInfoResponse me = coreBankingClient.fetchOne("users/me", UserInfoResponse.class);

    // 로그인/회원가입 처리
    var result = syncCoreBankUserUseCase.sync(me);
    log.info("Login result: {}", result);

    // 끝! (리디렉션, 쿠키 설정 등 없음)
    response.getWriter().write("OK");
    response.getWriter().flush();
  }
}
