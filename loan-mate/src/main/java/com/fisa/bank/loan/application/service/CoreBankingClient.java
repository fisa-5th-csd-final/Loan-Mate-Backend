package com.fisa.bank.loan.application.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class CoreBankingClient {

  // 테스트용 유저 토큰
  @Value("${corebanking.token}")
  private String token;

  private final WebClient webClient =
      WebClient.builder().defaultHeader("Authorization", "Bearer" + token).build();

  public List<Map<String, Object>> fetchLoans() {
    String url = "http://localhost:8080/api/loans/ledgers";

    return webClient
        .get()
        .uri(url)
        .retrieve()
        .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
        .collectList()
        .block();
  }
}
