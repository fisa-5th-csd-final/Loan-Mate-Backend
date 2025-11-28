package com.fisa.bank.loan.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AutoDepositResponse {
  private Long loanLedgerId;
  private String loanName;
  private BigDecimal accountBalance;
  private boolean autoDepositEnabled;

  // LoanAutoDeposit → AutoDepositResponse 변환
  public static AutoDepositResponse from(
      com.fisa.bank.loan.application.model.LoanAutoDeposit domain) {
    return AutoDepositResponse.builder()
        .loanLedgerId(domain.getLoanLedgerId())
        .loanName(domain.getLoanName())
        .accountBalance(domain.getAccountBalance())
        .autoDepositEnabled(domain.isAutoDepositEnabled())
        .build();
  }
}
