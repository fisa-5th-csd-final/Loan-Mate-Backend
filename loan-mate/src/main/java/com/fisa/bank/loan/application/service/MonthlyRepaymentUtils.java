package com.fisa.bank.loan.application.service;

import java.math.BigDecimal;
import java.util.List;

import com.fisa.bank.model.MonthlyRepayment;

public final class MonthlyRepaymentUtils {

  private MonthlyRepaymentUtils() {}

  public static BigDecimal firstMonthlyPaymentOrZero(List<MonthlyRepayment> repayments) {
    return repayments == null
        ? BigDecimal.ZERO
        : repayments.stream()
            .findFirst()
            .map(MonthlyRepayment::getMonthlyPayment)
            .orElse(BigDecimal.ZERO);
  }
}
