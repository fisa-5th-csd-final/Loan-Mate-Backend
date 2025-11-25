package com.fisa.bank.loan.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class LoanComment {
  private final Long loanLedgerId;
  private final String comment;
}
