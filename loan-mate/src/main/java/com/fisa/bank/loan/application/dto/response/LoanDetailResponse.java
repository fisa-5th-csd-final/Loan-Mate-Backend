package com.fisa.bank.loan.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.persistence.loan.enums.LoanType;
import com.fisa.bank.persistence.loan.enums.RepaymentType;

@Getter
@AllArgsConstructor
@Builder
public class LoanDetailResponse {
  private final Long loanId;
  private final String loanName;
  private final BigDecimal remainPrincipal;
  private final BigDecimal principal;
  private final BigDecimal monthlyRepayment;
  private final BigDecimal interestPayment;
  private final String accountNumber;
  private final LoanType loanType;
  private final RepaymentType repaymentType;
  private final int progress;
  private final LocalDateTime nextRepaymentDate;

  public static LoanDetailResponse from(Long loanId, LoanDetail loanDetail) {
    return LoanDetailResponse.builder()
        .loanId(loanId)
        .loanName(loanDetail.getName())
        .loanType(loanDetail.getLoanType())
        .monthlyRepayment(loanDetail.getMonthlyRepayment())
        .interestPayment(loanDetail.getInterestPayment())
        .repaymentType(loanDetail.getRepaymentType())
        .accountNumber(loanDetail.getAccountNumber())
        .remainPrincipal(loanDetail.getRemainPrincipal())
        .principal(loanDetail.getPrincipal())
        .progress(loanDetail.getProgress())
        .nextRepaymentDate(loanDetail.getNextRepaymentDate())
        .build();
  }
}
