package com.fisa.bank.common.application.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.util.JsonNodeMapper;

@Component
@RequiredArgsConstructor
public class CoreBankingClient {
  @Value("${corebanking.token}")
  private String token;

  private final WebClient.Builder builder;

  private WebClient getClient() {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private static final String BASE_URL = "http://localhost:8080/api";

  /**
   * 단일 데이터 조회 및 변환
   *
   * @param endpoint 요청 URL
   * @param clazz 변환하고자 하는 DTO 클래스
   * @param <T> 리턴 타입
   * @return 변환된 객체
   */
  public <T> T fetchOne(String endpoint, Class<T> clazz) {
    JsonNode root =
        getClient().get().uri(BASE_URL + endpoint).retrieve().bodyToMono(JsonNode.class).block();

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    JsonNode dataNode = root.path("data");
    return JsonNodeMapper.map(dataNode, clazz);
  }

  /**
   * 리스트 데이터 조회 및 변환
   *
   * @param endpoint 요청 URL
   * @param clazz 리스트 원소 타입 DTO 클래스
   * @param <T> 리스트 원소 타입
   * @return 변환된 객체 리스트
   */
  public <T> List<T> fetchList(String endpoint, Class<T> clazz) {
    JsonNode root =
        getClient().get().uri(BASE_URL + endpoint).retrieve().bodyToMono(JsonNode.class).block();

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    JsonNode dataNode = root.path("data");
    if (!dataNode.isArray()) {
      throw new IllegalStateException("data 노드가 배열이 아닙니다.");
    }

    return StreamSupport.stream(dataNode.spliterator(), false)
        .map(node -> JsonNodeMapper.map(node, clazz))
        .collect(Collectors.toList());
  }
}
