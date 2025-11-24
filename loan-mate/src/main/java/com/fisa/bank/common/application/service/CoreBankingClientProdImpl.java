package com.fisa.bank.common.application.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.util.JsonNodeMapper;
import com.fisa.bank.loan.application.dto.request.AutoDepositUpdateRequest;
import com.fisa.bank.common.config.security.ServiceUserAuthentication;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class CoreBankingClientProdImpl implements CoreBankingClient {

  private final OAuth2AuthorizedClientManager authorizedClientManager;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final WebClient.Builder builder;
  private final JsonNodeMapper jsonNodeMapper;

  @Value("${core-bank.api-url}")
  private String BASE_URL;

  private WebClient getClient(String token) {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private Authentication getAuth() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /** OAuth2 AccessToken 만료 시 자동 갱신. */
  private String getAccessToken(Authentication authentication) {

    // 서비스 서버 id로 저장된 인증 객체 가져오기
    if (authentication instanceof ServiceUserAuthentication serviceUserAuth) {
      Long userId = serviceUserAuth.getUserId();

      // OAuth2AuthorizedClientService에서 저장된 OAuth2 토큰 가져오기
      OAuth2AuthorizedClient client =
          authorizedClientService.loadAuthorizedClient("loan-mate", String.valueOf(userId));

      if (client == null) {
        throw new IllegalStateException("저장된 OAuth2 클라이언트를 찾을 수 없습니다. userId: " + userId);
      }

      return client.getAccessToken().getTokenValue();
    }

    if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
      throw new IllegalStateException("지원하지 않는 인증 타입입니다: " + authentication.getClass());
    }

    // 자동으로 코어뱅킹 액세스 토큰 갱신
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      throw new IllegalStateException("Request attributes를 찾을 수 없습니다.");
    }

    OAuth2AuthorizeRequest authorizeRequest =
        OAuth2AuthorizeRequest.withClientRegistrationId(
                oauthToken.getAuthorizedClientRegistrationId())
            .principal(oauthToken)
            .attribute("jakarta.servlet.http.HttpServletRequest", attrs.getRequest())
            .attribute("jakarta.servlet.http.HttpServletResponse", attrs.getResponse())
            .build();

    OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);

    if (client == null) {
      throw new IllegalStateException("AuthorizedClient 를 찾을 수 없습니다.");
    }

    return client.getAccessToken().getTokenValue();
  }

  public <T> T fetchOne(String endpoint, Class<T> clazz) {
    Authentication auth = getAuth();
    String token = getAccessToken(auth);

    JsonNode root =
        getClient(token)
            .get()
            .uri(BASE_URL + endpoint)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    JsonNode dataNode = root.path("data");
    return jsonNodeMapper.map(dataNode, clazz);
  }

  public <T> List<T> fetchList(String endpoint, Class<T> clazz) {
    Authentication auth = getAuth();
    String token = getAccessToken(auth);

    JsonNode root =
        getClient(token)
            .get()
            .uri(BASE_URL + endpoint)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    JsonNode dataNode = root.path("data");

    if (!dataNode.isArray()) {
      throw new IllegalStateException("data 노드가 배열이 아닙니다.");
    }

    return StreamSupport.stream(dataNode.spliterator(), false)
        .map(node -> jsonNodeMapper.map(node, clazz))
        .collect(Collectors.toList());
  }

  /**
   * 단일 데이터 삭제
   *
   * @param endpoint 요청 URL
   */
  public void fetchOneDelete(String endpoint) {
    Authentication auth = getAuth();
    String token = getAccessToken(auth);
    getClient(token).delete().uri(BASE_URL + endpoint).retrieve().bodyToMono(Void.class).block();
  }

  @Override
  public void updateAutoDepositEnabled(Long loanLedgerId, boolean autoDepositEnabled) {

    Authentication auth = getAuth();
    String token = getAccessToken(auth);

    String endpoint = "/loans/" + loanLedgerId + "/auto-deposit";

    getClient(token)
        .patch()
        .uri(BASE_URL + endpoint)
        .bodyValue(new AutoDepositUpdateRequest(autoDepositEnabled))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }
}
