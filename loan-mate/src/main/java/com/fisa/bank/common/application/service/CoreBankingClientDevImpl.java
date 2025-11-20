package com.fisa.bank.common.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.util.JsonNodeMapper;
import com.fisa.bank.loan.application.dto.request.AutoDepositUpdateRequest;

@Slf4j
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class CoreBankingClientDevImpl implements CoreBankingClient {

  private final DevAccessTokenManager tokenManager;
  private final WebClient.Builder builder;
  private final JsonNodeMapper jsonNodeMapper;

  @Value("${CORE_BANKING_API_URL}")
  private String baseUrl;

  private WebClient client(String token) {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private JsonNode executeRequest(String endpoint, String token) {
    return client(token)
        .get()
        .uri(baseUrl + endpoint)
        .retrieve()
        .bodyToMono(JsonNode.class)
        .block();
  }

  /** API 호출 + 토큰 만료 시 자동 재발급 */
  private JsonNode callApi(String endpoint) {
    String token = tokenManager.getAccessToken();

    try {
      return executeRequest(endpoint, token);

    } catch (WebClientResponseException e) {

      if (e.getStatusCode().value() == 401) {
        log.warn("AccessToken 재발급 후 재요청");

        String newToken = tokenManager.refreshAndGetNewToken();
        return executeRequest(endpoint, newToken);
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
    JsonNode root = callApi(endpoint);

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    return jsonNodeMapper.map(root.path("data"), clazz);
  }

  @Override
  public <T> List<T> fetchList(String endpoint, Class<T> clazz) {
    JsonNode root = callApi(endpoint);

    JsonNode arr = root.path("data");

    if (!arr.isArray()) {
      throw new IllegalStateException("data 노드가 배열이 아닙니다.");
    }

    return StreamSupport.stream(arr.spliterator(), false)
        .map(n -> jsonNodeMapper.map(n, clazz))
        .collect(Collectors.toList());
  }

  @Override
  public void updateAutoDepositEnabled(Long loanLedgerId, boolean autoDepositEnabled) {

    String endpoint = "/loans/" + loanLedgerId + "/auto-deposit";

    String token = tokenManager.getAccessToken();

    try {
      client(token)
          .patch()
          .uri(baseUrl + endpoint)
          .bodyValue(new AutoDepositUpdateRequest(autoDepositEnabled))
          .retrieve()
          .bodyToMono(Void.class)
          .block();

    } catch (WebClientResponseException e) {

      // 401 → 토큰 재발급 후 재요청
      if (e.getStatusCode().value() == 401) {

        String newToken = tokenManager.refreshAndGetNewToken();

        client(newToken)
            .patch()
            .uri(baseUrl + endpoint)
            .bodyValue(new AutoDepositUpdateRequest(autoDepositEnabled))
            .retrieve()
            .bodyToMono(Void.class)
            .block();

        return;
      }

      throw e;
    }
  }
}
