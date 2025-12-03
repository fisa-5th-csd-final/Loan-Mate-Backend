package com.fisa.bank.loan.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
@Builder
public class LoansWithPrepaymentBenefitResponse {
  private final Long loanLedgerId;
  private final BigDecimal balance;
  private final String loanName;
  private final BigDecimal benefit;
  private final BigDecimal mustPaidAmount;
  private final String accountNumber;
}
