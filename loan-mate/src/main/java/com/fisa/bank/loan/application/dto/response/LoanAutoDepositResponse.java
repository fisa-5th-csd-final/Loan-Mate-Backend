package com.fisa.bank.loan.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import com.fisa.bank.persistence.loan.entity.LoanLedger;

@Getter
@Builder
public class LoanAutoDepositResponse {
  private Long loanLedgerId;
  private LocalDateTime nextRepaymentDate;
  private Boolean autoDepositEnabled;

  public static LoanAutoDepositResponse from(LoanLedger loanLedger) {
    return LoanAutoDepositResponse.builder()
        .loanLedgerId(loanLedger.getLoanLedgerId().getValue())
        .nextRepaymentDate(loanLedger.getNextRepaymentDate())
        .autoDepositEnabled(loanLedger.isAutoDepositEnabled())
        .build();
  }
}
