package com.fisa.bank.loan.application.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
public class PrepaymentInfo {
  private final Long loanLedgerId;
  private final String loanProductName;
  private final BigDecimal earlyRepayment;
  private final List<InterestDetail> interestDetails;
}
