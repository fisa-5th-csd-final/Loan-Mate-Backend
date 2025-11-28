package com.fisa.bank.common.application.util.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.exception.ExternalApiException;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiClientImpl implements AiClient {
  private final WebClient.Builder builder;

  @Qualifier("aiObjectMapper") private final ObjectMapper aiObjectMapper;

  @Value("${ai-server.api-url}")
  private String baseUrl;

  private final RequesterInfo requesterInfo;

  @Override
  public <T, B> T fetchOne(String endpoint, B body, Class<T> clazz) {
    JsonNode root = executeRequest(endpoint, HttpMethod.POST, body, JsonNode.class);

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    return map(root, clazz);
  }

  private WebClient client() {
    return builder.build();
  }

  private <T, B> T executeRequest(
      String endpoint, HttpMethod method, B body, Class<T> responseType) {
    String url = baseUrl + endpoint;
    if (log.isTraceEnabled()) {
      log.trace("AI request -> method={} url={} body={}", method, url, body);
    }

    return client()
        .method(method)
        .uri(url)
        .bodyValue(body)
        .retrieve()
        .onStatus(
            status -> status.is4xxClientError() || status.is5xxServerError(),
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(
                        bodyStr ->
                            Mono.error(
                                new ExternalApiException(
                                    HttpStatus.valueOf(response.statusCode().value()),
                                    "AI_API_ERROR",
                                    "AI server error "
                                        + response.statusCode().value()
                                        + " : "
                                        + bodyStr,
                                    bodyStr))))
        .toEntity(responseType)
        .doOnSuccess(
            entity -> {
              if (log.isTraceEnabled() && entity != null) {
                log.trace(
                    "AI response <- status={} url={} headers={} body={}",
                    entity.getStatusCodeValue(),
                    url,
                    entity.getHeaders(),
                    entity.getBody());
              }
            })
        .map(ResponseEntity::getBody)
        .block();
  }

  private <T> T map(JsonNode node, Class<T> clazz) {
    try {
      return aiObjectMapper.treeToValue(node, clazz);
    } catch (Exception e) {
      throw new RuntimeException("AI JsonNode -> DTO 변환 실패", e);
    }
  }
}
