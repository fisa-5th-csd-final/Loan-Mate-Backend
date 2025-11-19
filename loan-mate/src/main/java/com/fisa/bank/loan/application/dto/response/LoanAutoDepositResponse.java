package com.fisa.bank.loan.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LoanAutoDepositResponse {
  private Long loanLedgerId;
  private LocalDateTime nextRepaymentDate;
  private Boolean autoDepositEnabled;
}
