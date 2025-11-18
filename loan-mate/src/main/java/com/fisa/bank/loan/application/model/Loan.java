package com.fisa.bank.loan.application.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fisa.bank.loan.persistence.enums.RiskLevel;

@Getter
@RequiredArgsConstructor
public class Loan {
  private final Long loanId;
  private final String loanName;
  private RiskLevel riskLevel;
  private LoanDetail loanDetail;
}
