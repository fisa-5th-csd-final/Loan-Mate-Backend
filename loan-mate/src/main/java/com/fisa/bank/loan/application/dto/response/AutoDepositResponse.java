package com.fisa.bank.loan.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

import com.fisa.bank.persistence.loan.entity.id.LoanLedgerId;

@Getter
@Builder
public class AutoDepositResponse {
  private LoanLedgerId loanLedgerId;
  private String loanName;
  private BigDecimal accountBalance;
  private boolean autoDepositEnabled;
}
