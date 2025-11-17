package com.fisa.bank.loan.application.model;

import lombok.Getter;

import java.math.BigDecimal;

import com.fisa.bank.loan.persistence.enums.LoanType;
import com.fisa.bank.loan.persistence.enums.RepaymentType;

@Getter
public class LoanDetail {
  private BigDecimal remainPrincipal;
  private BigDecimal principal;
  private BigDecimal monthlyRepayment;
  private String accountNumber;
  private LoanType loanType;
  private RepaymentType repaymentType;
}
