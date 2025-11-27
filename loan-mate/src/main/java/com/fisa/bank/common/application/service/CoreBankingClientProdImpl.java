package com.fisa.bank.common.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.util.JsonNodeMapper;
import com.fisa.bank.common.config.security.ServiceUserAuthentication;

@Slf4j
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

  @Override
  public <T> T fetchOne(String endpoint, Class<T> clazz) {
    JsonNode root = callApi(endpoint, HttpMethod.GET, JsonNode.class);
    return jsonNodeMapper.map(root.path("data"), clazz);
  }

  @Override
  public <T> List<T> fetchList(String endpoint, Class<T> clazz) {
    JsonNode root = callApi(endpoint, HttpMethod.GET, JsonNode.class);

    JsonNode arr = root.path("data");

    return StreamSupport.stream(arr.spliterator(), false)
        .map(n -> jsonNodeMapper.map(n, clazz))
        .collect(Collectors.toList());
  }

  @Override
  public void fetchOneDelete(String endpoint) {
    callApi(endpoint, HttpMethod.DELETE, Void.class);
  }

  @Override
  public void patch(String endpoint, Object body) {
    callApi(endpoint, HttpMethod.PATCH, body, Void.class);
  }

  private WebClient client(String token) {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private Authentication getAuth() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /** OAuth2 AccessToken 만료 시 자동 갱신 */
  private String getAccessToken(Authentication authentication) {

    // 서버 내부 ServiceUserAuthentication 케이스
    if (authentication instanceof ServiceUserAuthentication serviceUserAuth) {
      Long userId = serviceUserAuth.getUserId();

      OAuth2AuthorizedClient client =
          authorizedClientService.loadAuthorizedClient("loan-mate", String.valueOf(userId));

      if (client == null) {
        throw new IllegalStateException("저장된 OAuth2 클라이언트를 찾을 수 없습니다. userId: " + userId);
      }

      return client.getAccessToken().getTokenValue();
    }

    // 일반 OAuth2 로그인 유저
    if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
      throw new IllegalStateException("지원하지 않는 인증 타입: " + authentication.getClass());
    }

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

  // callApi()
  private <T> T callApi(String endpoint, HttpMethod method, Class<T> responseType) {

    Authentication auth = getAuth();
    String token = getAccessToken(auth);

    try {
      return client(token)
          .method(method)
          .uri(BASE_URL + endpoint)
          .retrieve()
          .bodyToMono(responseType)
          .block();

    } catch (WebClientResponseException e) {

      if (e.getStatusCode().value() == 401) {
        log.warn("AccessToken 만료 → 자동 재발급 후 재요청");

        String newToken = getAccessToken(auth);

        return client(newToken)
            .method(method)
            .uri(BASE_URL + endpoint)
            .retrieve()
            .bodyToMono(responseType)
            .block();
      }

      throw e;
    }
  }

  // callApi() - body 포함
  private <T, B> T callApi(String endpoint, HttpMethod method, B body, Class<T> responseType) {

    Authentication auth = getAuth();
    String token = getAccessToken(auth);

    try {
      return client(token)
          .method(method)
          .uri(BASE_URL + endpoint)
          .bodyValue(body)
          .retrieve()
          .bodyToMono(responseType)
          .block();

    } catch (WebClientResponseException e) {

      if (e.getStatusCode().value() == 401) {
        log.warn("AccessToken 만료 → 자동 재발급 후 재요청 (body 포함 요청)");

        String newToken = getAccessToken(auth);

        return client(newToken)
            .method(method)
            .uri(BASE_URL + endpoint)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .block();
      }

      throw e;
    }
  }
}
