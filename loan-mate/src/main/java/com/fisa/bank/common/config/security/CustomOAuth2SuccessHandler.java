package com.fisa.bank.common.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.common.application.service.JwtTokenGenerator;
import com.fisa.bank.common.presentation.util.CookieUtil;
import com.fisa.bank.user.application.dto.UserInfoResponse;
import com.fisa.bank.user.application.repository.RefreshTokenRepository;
import com.fisa.bank.user.application.repository.UserAuthRepository;
import com.fisa.bank.user.application.usecase.SyncCoreBankUserUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final CoreBankingClient coreBankingClient;
  private final SyncCoreBankUserUseCase syncCoreBankUserUseCase;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final ObjectMapper objectMapper;
  private final JwtTokenGenerator jwtTokenGenerator;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserAuthRepository userAuthRepository;
  private final CookieUtil cookieUtil;

  @Value("${jwt.refresh-token-expiration}")
  private Long refreshTokenExpiration;

  @Value("${jwt.access-token-expiration}")
  private Long accessTokenExpiration;

  @Value("${app.front-success-url}")
  private String frontSuccessUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    // 코어 뱅킹 사용자 정보 조회 및 동기화
    UserInfoResponse me = coreBankingClient.fetchOne("/users/me", UserInfoResponse.class);
    syncCoreBankUserUseCase.sync(me);

    // serviceUserId 조회
    Long serviceUserId =
        userAuthRepository
            .findByCoreBankingUserId(me.userId())
            .orElseThrow(() -> new IllegalStateException("사용자 동기화 후 매핑 정보를 찾을 수 없습니다."))
            .getServiceUserId();

    // OAuth2 토큰 저장 (코어 뱅킹 서버용)
    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    OAuth2AuthorizedClient authorizedClient =
        authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

    // serviceUserId를 principal name으로 사용하도록 새로운 OAuth2User와 인증 객체 생성
    DefaultOAuth2User newPrincipal =
        new DefaultOAuth2User(
            Collections.emptyList(),
            Collections.singletonMap("sub", serviceUserId.toString()),
            "sub");

    OAuth2AuthenticationToken newAuthToken =
        new OAuth2AuthenticationToken(
            newPrincipal, Collections.emptyList(), oauthToken.getAuthorizedClientRegistrationId());

    authorizedClientService.saveAuthorizedClient(authorizedClient, newAuthToken);

    // 우리 서버의 JWT 토큰 생성
    String accessToken = jwtTokenGenerator.generateAccessToken(serviceUserId);
    String refreshToken = jwtTokenGenerator.generateRefreshToken(serviceUserId);
    // Refresh Token DB에 저장 (7일 후 만료)
    Instant refreshTokenExpiry = Instant.now().plusMillis(refreshTokenExpiration);
    refreshTokenRepository.save(serviceUserId, refreshToken, refreshTokenExpiry);

    ResponseCookie accessCookie =
        cookieUtil.createHttpOnlyCookie(
            "accessToken", accessToken, (int) (accessTokenExpiration / 1000L));

    ResponseCookie refreshCookie =
        cookieUtil.createHttpOnlyCookie(
            "refreshToken", refreshToken, (int) (accessTokenExpiration / 1000L));

    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    String redirectUrl = frontSuccessUrl;
    log.info("로그인 성공. 프론트엔드로 리다이렉트: {}", redirectUrl);

    response.setContentType(MediaType.TEXT_HTML_VALUE);
    response.sendRedirect(redirectUrl);
  }
}
