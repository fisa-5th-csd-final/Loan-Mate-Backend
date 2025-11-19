package com.fisa.bank.common.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanLedgerDetailResponse {
  private String name;
  private BigDecimal remainPrincipal;
  private BigDecimal principal;
  private BigDecimal monthlyRepayment;
  private String accountNumber;
  private Boolean autoDepositEnabled;
}
