package com.fisa.bank.loan.application.dto.response;

import lombok.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AiSimulationResponse {
  @JsonProperty("base_risk_score")
  private BigDecimal baseRiskScore;

  @JsonProperty("simulated_risk_score")
  private BigDecimal simulatedRiskScore;

  private BigDecimal delta;
  private String explanation;
}
