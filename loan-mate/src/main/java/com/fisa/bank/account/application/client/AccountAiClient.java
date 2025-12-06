package com.fisa.bank.account.application.client;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.account.application.dto.request.AiRecommendRequest;
import com.fisa.bank.common.application.util.ai.AiClient;

@Component
@RequiredArgsConstructor
public class AccountAiClient {

  private static final String RECOMMEND_ENDPOINT = "/recommend";

  private final AiClient aiClient;

  @Cacheable(
      cacheNames = "aiExpenditure",
      key =
          "@springRequesterInfo.serviceUserId + ':' + (#year != null ? #year : T(java.time.YearMonth).now().getYear()) + '-' + (#month != null ? #month : T(java.time.YearMonth).now().getMonthValue())")
  public JsonNode fetchRecommendation(AiRecommendRequest request) {
    return aiClient.fetchOne(RECOMMEND_ENDPOINT, request, JsonNode.class);
  }
}
