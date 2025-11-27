package com.fisa.bank.common.application.util.core_bank;

import com.fisa.bank.common.application.util.jwt.DevAccessTokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.util.JsonNodeMapper;

@Slf4j
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class CoreBankingClientDevImpl implements CoreBankingClient {

  private final DevAccessTokenManager tokenManager;
  private final WebClient.Builder builder;
  private final JsonNodeMapper jsonNodeMapper;

  @Value("${core-bank.api-url}")
  private String baseUrl;

  private WebClient client(String token) {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private <T> T executeRequest(
      String endpoint, String token, HttpMethod method, Class<T> responseType) {
    return client(token)
        .method(method)
        .uri(baseUrl + endpoint)
        .retrieve()
        .bodyToMono(responseType)
        .block();
  }

  /** API 호출 + 토큰 만료 시 자동 재발급 */
  private <T> T callApi(String endpoint, HttpMethod method, Class<T> responseType) {
    String token = tokenManager.getAccessToken();

    try {
      return executeRequest(endpoint, token, method, responseType);

    } catch (WebClientResponseException e) {

      if (e.getStatusCode().value() == 401) {
        log.warn("AccessToken 재발급 후 재요청");

        String newToken = tokenManager.refreshAndGetNewToken();
        return executeRequest(endpoint, newToken, method, responseType);
      }

      log.error("CoreBanking 호출 실패: {} {}", e.getStatusCode(), e.getMessage());
      throw e;

    } catch (Exception e) {
      log.error("알 수 없는 예외", e);
      throw new IllegalStateException("CoreBanking API 호출 중 예외 발생", e);
    }
  }

  @Override
  public <T> T fetchOne(String endpoint, Class<T> clazz) {
    JsonNode root = callApi(endpoint, HttpMethod.GET, JsonNode.class);

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    return jsonNodeMapper.map(root.path("data"), clazz);
  }

  @Override
  public <T> List<T> fetchList(String endpoint, Class<T> clazz) {
    JsonNode root = callApi(endpoint, HttpMethod.GET, JsonNode.class);

    JsonNode arr = root.path("data");

    if (!arr.isArray()) {
      throw new IllegalStateException("data 노드가 배열이 아닙니다.");
    }

    return StreamSupport.stream(arr.spliterator(), false)
        .map(n -> jsonNodeMapper.map(n, clazz))
        .collect(Collectors.toList());
  }

  /**
   * 단일 데이터 삭제
   *
   * @param endpoint 요청 URL
   */
  public void delete(String endpoint) {

    callApi(endpoint, HttpMethod.DELETE, Void.class);
  }

  @Override
  public void patch(String endpoint, Object body) {
    callApi(endpoint, HttpMethod.PATCH, body, Void.class);
  }

  // requestBody 버전 callAPi 오버로딩
  private <T, B> T callApi(String endpoint, HttpMethod method, B body, Class<T> responseType) {
    String token = tokenManager.getAccessToken();

    try {
      return executeRequest(endpoint, token, method, body, responseType);

    } catch (WebClientResponseException e) {

      if (e.getStatusCode().value() == 401) {
        log.warn("AccessToken 재발급 후 재요청");

        String newToken = tokenManager.refreshAndGetNewToken();
        return executeRequest(endpoint, newToken, method, body, responseType);
      }

      log.error("CoreBanking 호출 실패: {} {}", e.getStatusCode(), e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("알 수 없는 예외", e);
      throw new IllegalStateException("CoreBanking API 호출 중 예외 발생", e);
    }
  }

  private <T, B> T executeRequest(
      String endpoint, String token, HttpMethod method, B body, Class<T> responseType) {
    return client(token)
        .method(method)
        .uri(baseUrl + endpoint)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType)
        .block();
  }
}
