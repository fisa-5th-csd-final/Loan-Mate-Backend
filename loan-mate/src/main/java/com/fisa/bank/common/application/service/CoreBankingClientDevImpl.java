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

  @Value("${CORE_BANKING_API_URL}")
  private String baseUrl;

  /** WebClient 생성 */
  private WebClient client(String token) {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  /** CoreBanking GET 호출 */
  private JsonNode callApi(String endpoint) {
    String token = tokenManager.getAccessToken();

    try {
      return client(token)
          .get()
          .uri(baseUrl + endpoint)
          .retrieve()
          .bodyToMono(JsonNode.class)
          .block();

    } catch (Exception ex) {
      log.warn("CoreBanking 요청 실패, 재발급 시도: " + ex.getMessage());

      // 토큰 재발급
      String newToken = tokenManager.refreshAndGetNewToken();

      // 재시도
      return client(newToken)
          .get()
          .uri(baseUrl + endpoint)
          .retrieve()
          .bodyToMono(JsonNode.class)
          .block();
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

    return StreamSupport.stream(arr.spliterator(), false)
        .map(n -> jsonNodeMapper.map(n, clazz))
        .collect(Collectors.toList());
  }
}
