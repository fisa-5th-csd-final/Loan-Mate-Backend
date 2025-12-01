package com.fisa.bank.account.application.service.spending;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.account.application.model.IncomeBreakdown;
import com.fisa.bank.account.application.model.UserAccountContext;
import com.fisa.bank.account.application.model.spending.RecommendedSpending;
import com.fisa.bank.account.application.model.spending.UserSpendingLimit;
import com.fisa.bank.account.application.service.helper.IncomeCalculator;
import com.fisa.bank.account.application.service.helper.UserAccountContextService;
import com.fisa.bank.account.application.usecase.GetRecommendedSpendingUseCase;
import com.fisa.bank.account.application.usecase.GetUserSpendingLimitUseCase;
import com.fisa.bank.account.application.util.SpendingRatioLoader;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;
import com.fisa.bank.user.application.model.ServiceUser;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedSpendingService implements GetRecommendedSpendingUseCase {

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal BUDGET_RATIO = BigDecimal.valueOf(0.4);

  private final LoanReader loanReader;
  private final UserAccountContextService userAccountContextService;
  private final IncomeCalculator incomeCalculator;
  private final SpendingRatioLoader ratioLoader;
  private final GetUserSpendingLimitUseCase getUserSpendingLimitUseCase;

  @Override
  public RecommendedSpending execute(int year, int month) {

    validateMonth(month);

    YearMonth currentMonth = YearMonth.of(year, month);

    UserAccountContext userAccountContext = userAccountContextService.loadContext();
    AccountId accountId = userAccountContext.salaryAccount().getAccountId();

    IncomeBreakdown income =
        incomeCalculator.calculatePreviousSalaryCurrentManual(
            accountId, userAccountContext.serviceUser().getUserId(), currentMonth);

    // 사용자가 직접 설정한 금액 한도가 있으면 그대로 반환
    var userLimitsOpt =
        getUserSpendingLimitUseCase
            .execute()
            .map(UserSpendingLimit::limits)
            .map(this::normalizeAmounts)
            .filter(limits -> !limits.isEmpty());
    if (userLimitsOpt.isPresent()) {
      Map<ConsumptionCategory, BigDecimal> userLimits = fillMissing(userLimitsOpt.get());
      BigDecimal totalBudget =
          userLimits.values().stream().reduce(ZERO, BigDecimal::add).setScale(0, RoundingMode.DOWN);
      return new RecommendedSpending(totalBudget, userLimits);
    }

    BigDecimal monthlyRepayment = sumMonthlyRepayment();
    BigDecimal availableIncome = income.total().subtract(monthlyRepayment).max(ZERO);
    BigDecimal variableSpendingBudget =
        availableIncome.multiply(BUDGET_RATIO).setScale(0, RoundingMode.DOWN);

    // 사용자 한도 설정이 있으면 우선 적용하고, 없으면 연령대 비율 사용
    Map<ConsumptionCategory, BigDecimal> categoryRecommendation =
        buildCategoryRecommendation(
            variableSpendingBudget, resolveCategoryRatios(userAccountContext.serviceUser()));

    return new RecommendedSpending(variableSpendingBudget, categoryRecommendation);
  }

  private void validateMonth(int month) {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("월은 1~12 사이여야 합니다.");
    }
  }

  // 월 상환금
  private BigDecimal sumMonthlyRepayment() {
    List<LoanDetail> loans = loanReader.findLoanDetails();
    return loans.stream()
        .map(LoanDetail::getMonthlyRepayment)
        .filter(Objects::nonNull)
        .reduce(ZERO, BigDecimal::add);
  }

  private Map<ConsumptionCategory, BigDecimal> resolveCategoryRatios(ServiceUser user) {
    return ratioLoader.getRatios(user.getBirthday());
  }

  // 연령대 기반 추천 금액 혹은 사용자 한도 기반 추천 금액
  private Map<ConsumptionCategory, BigDecimal> buildCategoryRecommendation(
      BigDecimal variableBudget, Map<ConsumptionCategory, BigDecimal> ratios) {

    Map<ConsumptionCategory, BigDecimal> result = new EnumMap<>(ConsumptionCategory.class);
    BigDecimal totalAllocated = ZERO;

    for (var entry : ratios.entrySet()) {
      BigDecimal allocated =
          variableBudget.multiply(entry.getValue()).setScale(0, RoundingMode.DOWN);

      result.put(entry.getKey(), allocated);
      totalAllocated = totalAllocated.add(allocated);
    }

    BigDecimal remainder = variableBudget.subtract(totalAllocated);
    if (remainder.compareTo(ZERO) > 0) {
      result.computeIfPresent(ConsumptionCategory.ENTERTAINMENT, (k, v) -> v.add(remainder));
    }

    for (ConsumptionCategory category : ConsumptionCategory.values()) {
      result.putIfAbsent(category, ZERO);
    }

    return result;
  }

  private Map<ConsumptionCategory, BigDecimal> normalizeAmounts(
      Map<ConsumptionCategory, BigDecimal> limits) {
    Map<ConsumptionCategory, BigDecimal> result = new EnumMap<>(ConsumptionCategory.class);
    if (limits == null) {
      return result;
    }
    limits.forEach(
        (category, value) -> {
          if (category != null && value != null) {
            result.put(category, value.max(ZERO));
          }
        });
    return result;
  }

  private Map<ConsumptionCategory, BigDecimal> fillMissing(
      Map<ConsumptionCategory, BigDecimal> limits) {
    Map<ConsumptionCategory, BigDecimal> result = new EnumMap<>(ConsumptionCategory.class);
    result.putAll(limits);
    for (ConsumptionCategory category : ConsumptionCategory.values()) {
      result.putIfAbsent(category, ZERO);
    }
    return result;
  }
}
