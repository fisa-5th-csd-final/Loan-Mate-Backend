package com.fisa.bank.loan.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class LoanMonthlyRepayResponse {

  private Long trxLId;
  private LocalDateTime date;
  private String transactionType;
  private BigDecimal amount;
  private BigDecimal remainPrincipal;

  private BigDecimal repaymentInterestAmount;
  private BigDecimal repaymentPrincipalAmount;
}
