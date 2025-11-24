package com.fisa.bank.loan.application.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrepaymentInfo {
  private Long loanLedgerId;
  private String loanProductName;
  private BigDecimal earlyRepayment;
  private List<InterestDetail> interestDetailResponses;
}
