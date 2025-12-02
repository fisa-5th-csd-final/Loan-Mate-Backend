package com.fisa.bank.loan.application.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class LoansWithPrepaymentBenefitResponse {
  private final String loanName;
  private final BigDecimal benefit;
  private final BigDecimal mustPaidAmount;

  public static LoansWithPrepaymentBenefitResponse from(
      String loanName, BigDecimal benefit, BigDecimal mustPaidAmount) {
    return new LoansWithPrepaymentBenefitResponse(loanName, benefit, mustPaidAmount);
  }
}
