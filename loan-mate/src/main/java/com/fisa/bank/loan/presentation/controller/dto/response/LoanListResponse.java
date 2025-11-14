package com.fisa.bank.loan.presentation.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.persistence.enums.RiskLevel;

@Getter
@RequiredArgsConstructor
public class LoanListResponse {
  private final String loanName;
  // TODO: 나중에 위험도 계산 가능하면 final로 변경
  private RiskLevel riskLevel;

  public static LoanListResponse from(Loan loan) {
    // TODO: 위험도 세팅
    return new LoanListResponse(loan.getLoanName());
  }
}
