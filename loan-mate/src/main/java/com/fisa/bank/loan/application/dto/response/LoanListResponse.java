package com.fisa.bank.loan.application.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.swing.*;

import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.persistence.enums.RiskLevel;

@Getter
@RequiredArgsConstructor
public class LoanListResponse {
  private final Long loanId;
  private final String loanName;

  @Setter private RiskLevel riskLevel;

  public static LoanListResponse from(Loan loan) {
    // TODO: 위험도 세팅
    return new LoanListResponse(loan.getLoanId(), loan.getLoanName());
  }
}
