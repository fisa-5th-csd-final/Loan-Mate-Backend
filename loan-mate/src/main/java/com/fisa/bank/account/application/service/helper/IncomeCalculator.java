package com.fisa.bank.account.application.service.helper;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.model.IncomeBreakdown;
import com.fisa.bank.account.application.repository.SpendingRepository;
import com.fisa.bank.account.application.usecase.GetPreviousSalaryIncomeUseCase;
import com.fisa.bank.account.persistence.repository.jpa.JpaAccountTransactionRepository;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Component
@RequiredArgsConstructor
public class IncomeCalculator implements GetPreviousSalaryIncomeUseCase {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private final JpaAccountTransactionRepository accountTransactionRepository;
  private final ManualLedgerRepository manualLedgerRepository;

  @Override
  public BigDecimal execute(AccountId accountId, YearMonth targetMonth) {
    YearMonth salaryMonth = targetMonth.minusMonths(1);
    LocalDateTime salaryStart = salaryMonth.atDay(1).atStartOfDay();
    LocalDateTime salaryEnd = salaryMonth.plusMonths(1).atDay(1).atStartOfDay();
    return safe(accountTransactionRepository.sumMonthlyIncome(accountId, salaryStart, salaryEnd));
  }

  public IncomeBreakdown calculatePreviousSalaryCurrentManual(
      AccountId accountId, Long serviceUserId, YearMonth targetMonth) {

    LocalDate manualStart = targetMonth.atDay(1);
    LocalDate manualEnd = targetMonth.plusMonths(1).atDay(1);

    BigDecimal salaryIncome = execute(accountId, targetMonth);
    BigDecimal manualIncome =
        safe(
            manualLedgerRepository.sumAmountByUserIdAndTypeBetween(
                serviceUserId, ManualLedgerType.INCOME, manualStart, manualEnd));

    return new IncomeBreakdown(salaryIncome, manualIncome);
  }

  private BigDecimal safe(BigDecimal value) {
    return value == null ? ZERO : value;
  }

  @Component
  @RequiredArgsConstructor
  public static class MonthlySpendingCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final SpendingRepository spendingRepository;
    private final ManualLedgerRepository manualLedgerRepository;

    public Map<ConsumptionCategory, BigDecimal> collectWithManualLedger(
        Long accountId, Long serviceUserId, int year, int month) {

      return collectWithManualLedger(List.of(accountId), serviceUserId, year, month);
    }

    public Map<ConsumptionCategory, BigDecimal> collectWithManualLedger(
        List<Long> accountIds, Long serviceUserId, int year, int month) {

      LocalDate startDate = LocalDate.of(year, month, 1);
      LocalDate endDate = startDate.plusMonths(1);

      Map<ConsumptionCategory, BigDecimal> spending = new EnumMap<>(ConsumptionCategory.class);

      if (accountIds != null) {
        accountIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .forEach(
                id ->
                    mergeSpending(
                        spendingRepository.getMonthlySpending(id, year, month), spending));
      }

      // 수기 입력 지출 가져옴.
      mergeManualLedgerSpending(serviceUserId, startDate, endDate, spending);

      return spending;
    }

    private void mergeManualLedgerSpending(
        Long serviceUserId,
        LocalDate startDate,
        LocalDate endDate,
        Map<ConsumptionCategory, BigDecimal> target) {

      Map<ConsumptionCategory, BigDecimal> manualSpending =
          manualLedgerRepository
              .findByUserIdAndTypeAndSavedAtBetween(
                  serviceUserId, ManualLedgerType.EXPENSE, startDate, endDate)
              .stream()
              .collect(
                  Collectors.groupingBy(
                      entry ->
                          entry.category() == null ? ConsumptionCategory.ETC : entry.category(),
                      () -> new EnumMap<>(ConsumptionCategory.class),
                      Collectors.mapping(
                          entry -> entry.amount() == null ? ZERO : entry.amount(),
                          Collectors.reducing(ZERO, BigDecimal::add))));

      manualSpending.forEach((category, amount) -> target.merge(category, amount, BigDecimal::add));
    }

    private void mergeSpending(
        Map<ConsumptionCategory, BigDecimal> source, Map<ConsumptionCategory, BigDecimal> target) {

      source.forEach((category, amount) -> target.merge(category, amount, BigDecimal::add));
    }
  }
}
