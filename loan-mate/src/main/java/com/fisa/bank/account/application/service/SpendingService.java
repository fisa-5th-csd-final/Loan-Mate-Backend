package com.fisa.bank.account.application.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fisa.bank.account.application.model.CategorySpending;
import com.fisa.bank.account.application.model.MonthlySpending;
import com.fisa.bank.account.application.repository.SpendingRepository;
import com.fisa.bank.account.application.usecase.GetMonthlySpendingUseCase;
import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Service
@RequiredArgsConstructor
public class SpendingService implements GetMonthlySpendingUseCase {

  private final SpendingRepository spendingRepository;
  private final ManualLedgerRepository manualLedgerRepository;
  private final RequesterInfo requesterInfo;

  @Override
  public MonthlySpending execute(Long accountId, int year, int month) {

    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("월(month)은 1에서 12 사이의 값이어야 합니다.");
    }
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1);

    Map<ConsumptionCategory, BigDecimal> categoryMap = new EnumMap<>(ConsumptionCategory.class);

    categoryMap.putAll(spendingRepository.getMonthlySpending(accountId, year, month));
    getManualLedgerSpending(startDate, endDate)
        .forEach((category, amount) -> categoryMap.merge(category, amount, BigDecimal::add));

    BigDecimal totalSpent = categoryMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

    List<CategorySpending> categories =
        categoryMap.entrySet().stream()
            .map(
                e ->
                    new CategorySpending(
                        e.getKey(),
                        e.getValue().setScale(0, RoundingMode.DOWN),
                        calcPercent(e.getValue(), totalSpent)))
            // enum 정의 순으로 카테고리 정렬
            .sorted(Comparator.comparing(e -> e.category().ordinal()))
            .toList();

    return new MonthlySpending(year, month, totalSpent.setScale(0, RoundingMode.DOWN), categories);
  }

  private BigDecimal calcPercent(BigDecimal amount, BigDecimal total) {
    if (total.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return amount
        .multiply(BigDecimal.valueOf(100)) // 100 * amount
        .divide(total, 1, RoundingMode.HALF_UP); // 소수점 1자리 반올림(BigDecimal 반환)
  }

  private Map<ConsumptionCategory, BigDecimal> getManualLedgerSpending(
      LocalDate startDate, LocalDate endDate) {

    Long serviceUserId = requesterInfo.getServiceUserId();

    return manualLedgerRepository
        .findByUserIdAndType(serviceUserId, ManualLedgerType.EXPENSE)
        .stream()
        .filter(entry -> isWithin(entry.savedAt(), startDate, endDate))
        .collect(
            Collectors.groupingBy(
                entry -> entry.category() == null ? ConsumptionCategory.ETC : entry.category(),
                () -> new EnumMap<>(ConsumptionCategory.class),
                Collectors.mapping(
                    ManualLedgerEntry::amount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
  }

  private boolean isWithin(LocalDate target, LocalDate start, LocalDate end) {
    return (target.isEqual(start) || target.isAfter(start)) && target.isBefore(end);
  }
}
