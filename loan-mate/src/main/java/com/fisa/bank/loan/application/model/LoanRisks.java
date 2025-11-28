package com.fisa.bank.loan.application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanRisks {
  private BigDecimal overallRisk;
  private List<LoanRiskDetail> loans;
}
