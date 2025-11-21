package com.fisa.bank.loan.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import com.fisa.bank.loan.application.model.Loan;

@Getter
@Builder
public class LoanAutoDepositResponse {
  private Long loanLedgerId;
  private LocalDateTime nextRepaymentDate;
  private Boolean autoDepositEnabled;

  public static LoanAutoDepositResponse from(Loan loan) {
    return LoanAutoDepositResponse.builder()
        .loanLedgerId(loan.getLoanId())
        .nextRepaymentDate(loan.getNextRepaymentDate())
        .autoDepositEnabled(loan.getAutoDepositEnabled())
        .build();
  }
}
