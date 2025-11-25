package com.fisa.bank.loan.application.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoanRiskDetail {

  private Long loanLedgerId;
  private BigDecimal risk;
}
