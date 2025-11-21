package com.fisa.bank.loan.application.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class LoansWithPrepaymentBenefitResponse {
  private final String loanName;
  private final BigDecimal benefit;

  public static LoansWithPrepaymentBenefitResponse from(String loanName, BigDecimal benefit) {
    return new LoansWithPrepaymentBenefitResponse(loanName, benefit);
  }
}
