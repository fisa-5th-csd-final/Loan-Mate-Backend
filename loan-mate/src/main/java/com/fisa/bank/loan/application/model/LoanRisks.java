package com.fisa.bank.loan.application.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class LoanRisks {
  private BigDecimal overallRisk;
  private List<LoanRiskDetail> loans;
}
