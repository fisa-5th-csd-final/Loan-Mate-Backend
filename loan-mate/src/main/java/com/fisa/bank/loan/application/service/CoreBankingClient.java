package com.fisa.bank.loan.application.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.loan.application.domain.LoanDetail;

@Component
@RequiredArgsConstructor
public class CoreBankingClient {

  // 테스트용 유저 토큰
  @Value("${corebanking.token}")
  private String token;

  private final WebClient.Builder builder;

  private WebClient getClient() {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private static final String BASE_URL = "http://localhost:8080/api/loans";

  public List<Map<String, Object>> fetchLoans() {
    String url = BASE_URL + "ledgers";

    return getClient()
        .get()
        .uri(url)
        .retrieve()
        .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
        .collectList()
        .block();
  }

  public LoanDetail fetchLoanDetail(Long loanId) {
    JsonNode root =
        getClient()
            .get() // GET 요청
            .uri(BASE_URL + "/ledger/" + loanId) // CORE BANK의 END POINT
            .retrieve() // 요청 보내고, 응답 수신
            .bodyToMono(JsonNode.class)
            .block();

    if (root == null) {
      throw new IllegalStateException("응답이 null 입니다.");
    }
    JsonNode dataNode = root.path("data");

    return LoanDetail.from(dataNode);
  }
}
