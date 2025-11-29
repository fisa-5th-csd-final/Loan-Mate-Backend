package com.fisa.bank.loan.application.dto.response;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

@RequiredArgsConstructor
public class AiSimulationResponse {
  @JsonProperty("base_risk_score")
  private final BigDecimal baseRiskScore;

  @JsonProperty("simulated_risk_score")
  private final BigDecimal simulatedRiskScore;

  private final BigDecimal delta;
  private final String explanation;
}
