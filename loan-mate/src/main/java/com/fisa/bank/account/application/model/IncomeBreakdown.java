package com.fisa.bank.account.application.model;

import java.math.BigDecimal;

public record IncomeBreakdown(BigDecimal salaryIncome, BigDecimal manualIncome) {

  public BigDecimal total() {
    return salaryIncome.add(manualIncome);
  }
}
