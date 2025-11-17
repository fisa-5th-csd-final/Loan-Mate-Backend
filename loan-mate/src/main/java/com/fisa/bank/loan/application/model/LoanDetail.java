package com.fisa.bank.loan.application.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fisa.bank.persistence.loan.enums.LoanType;
import com.fisa.bank.persistence.loan.enums.RepaymentType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanDetail {
    private String name;
  private BigDecimal remainPrincipal;
  private BigDecimal principal;
  private BigDecimal monthlyRepayment;
  private String accountNumber;
  private LoanType loanType;
  private RepaymentType repaymentType;
}
