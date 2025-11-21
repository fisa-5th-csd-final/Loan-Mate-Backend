package com.fisa.bank.common.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.user.application.dto.LoginResponse;
import com.fisa.bank.user.application.dto.UserInfoResponse;
import com.fisa.bank.user.application.usecase.SyncCoreBankUserUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final CoreBankingClient coreBankingClient;
  private final SyncCoreBankUserUseCase syncCoreBankUserUseCase;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    UserInfoResponse me = coreBankingClient.fetchOne("users/me", UserInfoResponse.class);
    syncCoreBankUserUseCase.sync(me);

    // OAuth2 토큰 가져오기
    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    OAuth2AuthorizedClient authorizedClient =
        authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

    String accessToken = authorizedClient.getAccessToken().getTokenValue();
    String refreshToken =
        authorizedClient.getRefreshToken() != null
            ? authorizedClient.getRefreshToken().getTokenValue()
            : null;

    // 응답 생성
    LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken, me.userId());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
  }
}
