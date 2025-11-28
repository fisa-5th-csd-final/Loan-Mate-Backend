package com.fisa.bank.loan.application.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanRisks {
  private BigDecimal overallRisk;
  private List<LoanRiskDetail> loans;
}
