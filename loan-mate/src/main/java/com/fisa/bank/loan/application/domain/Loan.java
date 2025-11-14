package com.fisa.bank.loan.application.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fisa.bank.loan.persistence.enums.RiskLevel;

@Getter
@RequiredArgsConstructor
public class Loan {
  private final String loanName;
  private RiskLevel riskLevel;
}
