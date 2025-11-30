package com.fisa.bank.account.application.client;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.account.application.dto.request.AiRecommendRequest;
import com.fisa.bank.common.application.util.ai.AiClient;

@Component
@RequiredArgsConstructor
public class AccountAiClient {

  private static final String RECOMMEND_ENDPOINT = "/api/ai/recommend";

  private final AiClient aiClient;

  public JsonNode fetchRecommendation(AiRecommendRequest request) {
    return aiClient.fetchOne(RECOMMEND_ENDPOINT, request, JsonNode.class);
  }
}
