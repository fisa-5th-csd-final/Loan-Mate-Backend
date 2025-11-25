package com.fisa.bank.loan.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class LoanComment {
  private final Long loanLedgerId;
  private final String comment;
}
