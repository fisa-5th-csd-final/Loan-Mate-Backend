package com.fisa.bank.loan.application.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import com.fisa.bank.loan.persistence.enums.RiskLevel;
import com.fisa.bank.persistence.loan.enums.RepaymentStatus;

@Getter
@RequiredArgsConstructor
public class Loan {
  private final Long loanId;
  private final String loanName;
  private final LocalDateTime createdAt;
  private final LocalDateTime lastRepaymentDate;
  private final int term;
  private final RepaymentStatus repaymentStatus;
  private RiskLevel riskLevel;
  private LoanDetail loanDetail;
}
