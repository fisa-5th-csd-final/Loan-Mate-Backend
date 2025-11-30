package com.fisa.bank.account.application.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.model.IncomeBreakdown;
import com.fisa.bank.account.persistence.repository.JpaAccountTransactionRepository;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.persistence.account.entity.id.AccountId;

@Component
@RequiredArgsConstructor
public class IncomeCalculator {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private final JpaAccountTransactionRepository accountTransactionRepository;
  private final ManualLedgerRepository manualLedgerRepository;

  public IncomeBreakdown calculatePreviousSalaryCurrentManual(
      AccountId accountId, Long serviceUserId, YearMonth targetMonth) {

    YearMonth salaryMonth = targetMonth.minusMonths(1);
    LocalDateTime salaryStart = salaryMonth.atDay(1).atStartOfDay();
    LocalDateTime salaryEnd = salaryMonth.plusMonths(1).atDay(1).atStartOfDay();

    LocalDate manualStart = targetMonth.atDay(1);
    LocalDate manualEnd = targetMonth.plusMonths(1).atDay(1);

    BigDecimal salaryIncome =
        safe(accountTransactionRepository.sumMonthlyIncome(accountId, salaryStart, salaryEnd));
    BigDecimal manualIncome =
        safe(
            manualLedgerRepository.sumAmountByUserIdAndTypeBetween(
                serviceUserId, ManualLedgerType.INCOME, manualStart, manualEnd));

    return new IncomeBreakdown(salaryIncome, manualIncome);
  }

  private BigDecimal safe(BigDecimal value) {
    return value == null ? ZERO : value;
  }
}
