package com.fisa.bank.common.application.util;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** JsonNode를 DTO로 변환하는 유틸 클래스 */
@Component
@RequiredArgsConstructor
public class JsonNodeMapper {

  private final ObjectMapper objectMapper;

  /**
   * @param node 변환할 JSON 데이터를 가진 {@link JsonNode} 객체
   * @param clazz 변환하고자 하는 DTO 클래스 타입
   * @param <T> 변환될 DTO 타입
   * @return 변환된 DTO 객체. JsonNode의 데이터가 null이거나 매핑에 실패하면 예외 발생
   * @throws RuntimeException 변환 중 오류가 발생할 경우
   */
  public <T> T map(JsonNode node, Class<T> clazz) {
    try {
      return objectMapper.treeToValue(node, clazz);
    } catch (Exception e) {
      throw new RuntimeException(
          "JsonNode -> DTO 변환 실패 \n" + "@JsonIgnoreProperties(ignoreUnknown = true)을 DTO에 붙여보세요",
          e);
    }
  }
}
