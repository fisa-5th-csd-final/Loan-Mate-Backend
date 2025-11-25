package com.fisa.bank.loan.application.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.persistence.enums.RiskLevel;

@Getter
@RequiredArgsConstructor
public class LoanListResponse {
  private final Long loanId;
  private final String loanName;

  private final RiskLevel riskLevel;

  public static LoanListResponse from(Loan loan, Map<Long, BigDecimal> riskMap) {
    BigDecimal risk = riskMap.get(loan.getLoanId());
    RiskLevel level = RiskLevel.fromRiskScore(risk);
    return new LoanListResponse(loan.getLoanId(), loan.getLoanName(), level);
  }
}
