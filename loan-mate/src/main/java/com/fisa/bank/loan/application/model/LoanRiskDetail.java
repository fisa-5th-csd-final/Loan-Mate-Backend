package com.fisa.bank.loan.application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanRiskDetail {

  private Long loanLedgerId;
  private BigDecimal risk;
  private String explanation;

}
