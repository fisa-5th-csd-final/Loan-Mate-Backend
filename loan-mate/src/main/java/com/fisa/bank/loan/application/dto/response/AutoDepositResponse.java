package com.fisa.bank.loan.application.dto.response;

import com.fisa.bank.persistence.loan.entity.id.LoanLedgerId;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AutoDepositResponse {
  private LoanLedgerId loanLedgerId;
  private String loanName;
  private BigDecimal accountBalance;
  private boolean autoDepositEnabled;
}
