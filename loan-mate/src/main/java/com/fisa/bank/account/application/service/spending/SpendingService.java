package com.fisa.bank.account.application.service.spending;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fisa.bank.account.application.model.spending.CategorySpending;
import com.fisa.bank.account.application.model.spending.MonthlySpending;
import com.fisa.bank.account.application.repository.AccountRepository;
import com.fisa.bank.account.application.service.helper.IncomeCalculator;
import com.fisa.bank.account.application.service.helper.UserAccountContextService;
import com.fisa.bank.account.application.usecase.GetMonthlySpendingUseCase;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Service
@RequiredArgsConstructor
public class SpendingService implements GetMonthlySpendingUseCase {

  private final IncomeCalculator.MonthlySpendingCalculator monthlySpendingCalculator;
  private final UserAccountContextService userAccountContextService;
  private final AccountRepository accountRepository;

  @Override
  public MonthlySpending execute(int year, int month) {

    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("월(month)은 1에서 12 사이의 값이어야 합니다.");
    }

    var context = userAccountContextService.loadContext();
    Long coreUserId = context.coreUserId();
    Long serviceUserId = context.serviceUser().getUserId();

    var accountIds =
        accountRepository.findByUserId(UserId.of(coreUserId)).stream()
            .map(account -> account.getAccountId().getValue())
            .toList();

    Map<ConsumptionCategory, BigDecimal> categoryMap =
        monthlySpendingCalculator.collectWithManualLedger(accountIds, serviceUserId, year, month);

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
}
