package com.fisa.bank.loan.application.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanRiskDetail {

  private Long loanLedgerId;
  private BigDecimal risk;
  private String explanation;
}
