package com.fisa.bank.common.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.util.JsonNodeMapper;

@Component
@RequiredArgsConstructor
public class AiClientImpl implements AiClient {
  private final WebClient.Builder builder;

  private final JsonNodeMapper jsonNodeMapper;

  @Value("${ai-server.api-url}")
  private String baseUrl;

  @Override
  public <T, B> T fetchOne(String endpoint, B body, Class<T> clazz) {
    JsonNode root = executeRequest(endpoint, HttpMethod.POST, body, JsonNode.class);

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    return jsonNodeMapper.map(root.path("data"), clazz);
  }

  private WebClient client() {
    return builder.build();
  }

  private <T, B> T executeRequest(
      String endpoint, HttpMethod method, B body, Class<T> responseType) {
    return client()
        .method(method)
        .uri(baseUrl + endpoint)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType)
        .block();
  }
}
