package com.fisa.bank.account.application.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fisa.bank.account.application.domain.CategorySpending;
import com.fisa.bank.account.application.domain.ConsumptionCategory;
import com.fisa.bank.account.application.domain.MonthlySpending;
import com.fisa.bank.account.application.repository.SpendingRepository;
import com.fisa.bank.account.application.usecase.GetMonthlySpendingUseCase;

@Service
@RequiredArgsConstructor
public class SpendingService implements GetMonthlySpendingUseCase {

  private final SpendingRepository spendingRepository;

  @Override
  public MonthlySpending execute(Long accountId) {

    // 현재 날짜 기준으로
    LocalDate now = LocalDate.now();
    int year = now.getYear();
    int month = now.getMonthValue();

    Map<ConsumptionCategory, BigDecimal> categoryMap =
        spendingRepository.getMonthlySpending(accountId, year, month);

    BigDecimal totalSpent = categoryMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

    List<CategorySpending> categories =
        categoryMap.entrySet().stream()
            .map(
                e ->
                    new CategorySpending(
                        e.getKey(), e.getValue(), calcPercent(e.getValue(), totalSpent)))
            .toList();

    return new MonthlySpending(year, month, totalSpent, categories);
  }

  private int calcPercent(BigDecimal amount, BigDecimal total) {
    if (total.compareTo(BigDecimal.ZERO) == 0) return 0;
    return amount
        .multiply(BigDecimal.valueOf(100))
        .divide(total, 0, RoundingMode.HALF_UP)
        .intValue();
  }
}
