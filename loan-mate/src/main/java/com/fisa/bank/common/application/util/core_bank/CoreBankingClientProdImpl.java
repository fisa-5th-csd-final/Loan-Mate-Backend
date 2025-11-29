package com.fisa.bank.common.application.util.core_bank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.fisa.bank.common.application.exception.ExternalApiException;
import com.fisa.bank.common.application.util.JsonNodeMapper;
import com.fisa.bank.common.application.util.RequesterInfo;
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
  private final RequesterInfo requesterInfo;

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
  public void delete(String endpoint) {
    callApi(endpoint, HttpMethod.DELETE, Void.class);
  }

  @Override
  public void patch(String endpoint, Object body) {
    callApi(endpoint, HttpMethod.PATCH, body, Void.class);
  }

  private WebClient client(String token) {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private <T> void logResponse(String url, ResponseEntity<T> entity) {
    if (!log.isTraceEnabled() || entity == null) {
      return;
    }

    log.trace(
        "CoreBank response <- status={} url={} headers={} body={}",
        entity.getStatusCodeValue(),
        url,
        entity.getHeaders(),
        entity.getBody());
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
    String url = BASE_URL + endpoint;

    if (log.isTraceEnabled()) {
      log.trace("CoreBank request -> method={} url={} body=null", method, url);
    }

    try {
      ResponseEntity<T> entity =
          client(token).method(method).uri(url).retrieve().toEntity(responseType).block();

      logResponse(url, entity);
      return entity != null ? entity.getBody() : null;

    } catch (WebClientResponseException e) {

      if (e.getStatusCode().value() == 401) {
        log.warn("AccessToken 만료 → 자동 재발급 후 재요청");

        String newToken = getAccessToken(auth);

        ResponseEntity<T> entity =
            client(newToken).method(method).uri(url).retrieve().toEntity(responseType).block();

        logResponse(url, entity);
        return entity != null ? entity.getBody() : null;
      }

      String body = e.getResponseBodyAsString();
      log.error("CoreBanking 호출 실패: {} {} body={}", e.getStatusCode(), e.getMessage(), body);
      throw new ExternalApiException(
          HttpStatus.valueOf(e.getStatusCode().value()),
          "CORE_BANK_API_ERROR",
          "CoreBanking error " + e.getStatusCode().value() + " : " + body,
          body);
    }
  }

  // callApi() - body 포함
  private <T, B> T callApi(String endpoint, HttpMethod method, B body, Class<T> responseType) {

    Authentication auth = getAuth();
    String token = getAccessToken(auth);
    String url = BASE_URL + endpoint;

    if (log.isTraceEnabled()) {
      log.trace("CoreBank request -> method={} url={} body={}", method, url, body);
    }

    try {
      ResponseEntity<T> entity =
          client(token)
              .method(method)
              .uri(url)
              .bodyValue(body)
              .retrieve()
              .toEntity(responseType)
              .block();

      logResponse(url, entity);
      return entity != null ? entity.getBody() : null;

    } catch (WebClientResponseException e) {

      if (e.getStatusCode().value() == 401) {
        log.warn("AccessToken 만료 → 자동 재발급 후 재요청 (body 포함 요청)");

        String newToken = getAccessToken(auth);

        ResponseEntity<T> entity =
            client(newToken)
                .method(method)
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .toEntity(responseType)
                .block();

        logResponse(url, entity);
        return entity != null ? entity.getBody() : null;
      }

      String bodyString = e.getResponseBodyAsString();
      log.error("CoreBanking 호출 실패: {} {} body={}", e.getStatusCode(), e.getMessage(), bodyString);
      throw new ExternalApiException(
          HttpStatus.valueOf(e.getStatusCode().value()),
          "CORE_BANK_API_ERROR",
          "CoreBanking error " + e.getStatusCode().value() + " : " + bodyString,
          bodyString);
    }
  }
}
