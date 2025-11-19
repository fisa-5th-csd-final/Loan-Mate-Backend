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

    UserInfoResponse me = coreBankingClient.fetchOne("users/me", UserInfoResponse.class);
    syncCoreBankUserUseCase.sync(me);

    response.getWriter().write("OK");
  }
}
