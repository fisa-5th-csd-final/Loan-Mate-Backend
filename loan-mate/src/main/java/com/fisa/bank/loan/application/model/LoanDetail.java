package com.fisa.bank.loan.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fisa.bank.persistence.loan.enums.LoanType;
import com.fisa.bank.persistence.loan.enums.RepaymentStatus;
import com.fisa.bank.persistence.loan.enums.RepaymentType;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class LoanDetail {
  private Long loanLedgerId;
  private String name;
  private BigDecimal remainPrincipal;
  private BigDecimal principal;
  private BigDecimal monthlyRepayment;
  private BigDecimal interestPayment;
  private String accountNumber;
  private LoanType loanType;
  private RepaymentType repaymentType;
  private LocalDateTime lastRepaymentDate;
  private LocalDateTime nextRepaymentDate;
  private LocalDateTime createdAt;
  private int term;
  private RepaymentStatus repaymentStatus;

  @Setter private Integer progress;
}
