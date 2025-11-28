package com.fisa.bank.loan.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LoanAutoDeposit {
  private final Long loanLedgerId;
  private final String loanName;
  private final BigDecimal accountBalance;
  private final boolean autoDepositEnabled;
}
