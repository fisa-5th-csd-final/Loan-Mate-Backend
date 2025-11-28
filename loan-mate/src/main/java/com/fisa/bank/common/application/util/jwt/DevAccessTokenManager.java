package com.fisa.bank.common.application.util.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

@Slf4j
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class DevAccessTokenManager {

  private final WebClient.Builder builder;

  @Value("${dev.login.url}")
  private String loginUrl;

  @Value("${dev.login.id}")
  private String loginId;

  @Value("${dev.login.password}")
  private String password;

  private String cachedToken;

  /** 토큰 발급 요청 */
  private String requestNewToken() {
    log.info("[DEV LOGIN] 자동 로그인으로 Access Token 발급 시도");

    JsonNode res =
        builder
            .build()
            .post()
            .uri(loginUrl)
            .header("Content-Type", "application/json")
            .bodyValue(
                Map.of(
                    "loginId", loginId,
                    "password", password))
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

    if (res == null || res.get("access_token") == null) {
      throw new IllegalStateException("개발용 access_token 발급 실패");
    }

    String token = res.get("access_token").asText();
    log.info("[DEV LOGIN] Access Token 발급 성공");

    return token;
  }

  /** 토큰 가져오기 (없으면 자동발급) */
  public synchronized String getAccessToken() {
    if (cachedToken == null) {
      cachedToken = requestNewToken();
    }
    return cachedToken;
  }

  /** 토큰 만료 시 재발급 */
  public synchronized String refreshAndGetNewToken() {
    cachedToken = requestNewToken();
    return cachedToken;
  }
}
