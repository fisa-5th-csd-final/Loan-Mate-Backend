package com.fisa.bank.loan.application.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class LoansWithPrepaymentBenefitResponse {
    private final Long loanLedgerId;
    private final BigDecimal balance;
  private final String loanName;
  private final BigDecimal benefit;
  private final BigDecimal mustPaidAmount;
    private final String accountNumber;

  public static LoansWithPrepaymentBenefitResponse from(
          Long loanLedgerId, BigDecimal balance, String loanName, BigDecimal benefit, BigDecimal mustPaidAmount, String accountNumber) {
    return new LoansWithPrepaymentBenefitResponse(loanLedgerId, balance, loanName, benefit, mustPaidAmount, accountNumber);
  }
}
