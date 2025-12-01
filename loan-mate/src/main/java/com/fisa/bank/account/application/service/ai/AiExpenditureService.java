package com.fisa.bank.account.application.service.ai;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.account.application.client.AccountAiClient;
import com.fisa.bank.account.application.dto.request.AiRecommendRequest;
import com.fisa.bank.account.application.model.IncomeBreakdown;
import com.fisa.bank.account.application.model.UserAccountContext;
import com.fisa.bank.account.application.model.spending.UserSpendingLimit;
import com.fisa.bank.account.application.service.helper.IncomeCalculator;
import com.fisa.bank.account.application.service.helper.UserAccountContextService;
import com.fisa.bank.account.application.usecase.GetUserSpendingLimitUseCase;
import com.fisa.bank.account.application.util.SpendingRatioLoader;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Service
@RequiredArgsConstructor
@Transactional
public class AiExpenditureService {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private final AccountAiClient accountAiClient;
  private final GetUserSpendingLimitUseCase getUserSpendingLimitUseCase;
  private final IncomeCalculator.MonthlySpendingCalculator monthlySpendingCalculator;
  private final SpendingRatioLoader spendingRatioLoader;
  private final UserAccountContextService userAccountContextService;
  private final IncomeCalculator incomeCalculator;

  public JsonNode requestExpenditure(Integer year, Integer month) {

    YearMonth targetMonth = resolveYearMonth(year, month);

    UserAccountContext userAccountContext = userAccountContextService.loadContext();

    Map<ConsumptionCategory, BigDecimal> ageGroupRatio =
        spendingRatioLoader.getRatios(userAccountContext.serviceUser().getBirthday());
    Map<ConsumptionCategory, BigDecimal> userLimitRatio = resolveUserLimitRatio(ageGroupRatio);

    Long serviceUserId = userAccountContext.serviceUser().getUserId();
    var accountId = userAccountContext.salaryAccount().getAccountId();

    Map<ConsumptionCategory, BigDecimal> spending =
        monthlySpendingCalculator.collectWithManualLedger(
            accountId.getValue(),
            serviceUserId,
            targetMonth.getYear(),
            targetMonth.getMonthValue());

    IncomeBreakdown income =
        incomeCalculator.calculatePreviousSalaryCurrentManual(
            accountId, serviceUserId, targetMonth);
    BigDecimal totalIncome = income.total();

    Map<ConsumptionCategory, BigDecimal> spendingRatio = buildSpendingRatio(spending, totalIncome);

    AiRecommendRequest aiRequest =
        new AiRecommendRequest(
            new AiRecommendRequest.SpendingRatio(spendingRatio, totalIncome),
            ageGroupRatio,
            new AiRecommendRequest.UserLimitRatio(userLimitRatio));

    return accountAiClient.fetchRecommendation(aiRequest);
  }

  private Map<ConsumptionCategory, BigDecimal> resolveUserLimitRatio(
      Map<ConsumptionCategory, BigDecimal> ageGroupRatio) {
    return getUserSpendingLimitUseCase
        .execute()
        .map(UserSpendingLimit::limits)
        .filter(limits -> limits != null && !limits.isEmpty())
        .map(this::convertAmountToRatio)
        .filter(ratios -> !ratios.isEmpty())
        .orElseGet(() -> copyLimits(ageGroupRatio));
  }

  private Map<ConsumptionCategory, BigDecimal> copyLimits(
      Map<ConsumptionCategory, BigDecimal> limits) {
    Map<ConsumptionCategory, BigDecimal> copy = new EnumMap<>(ConsumptionCategory.class);
    if (limits != null) {
      copy.putAll(limits);
    }
    return copy;
  }

  // DB에 비율로 저장하므로 ai에 보낼 때, 다시 비율로 바꿔줘야 함
  private Map<ConsumptionCategory, BigDecimal> convertAmountToRatio(
      Map<ConsumptionCategory, BigDecimal> limits) {
    Map<ConsumptionCategory, BigDecimal> ratios = new EnumMap<>(ConsumptionCategory.class);
    if (limits == null) {
      return ratios;
    }

    BigDecimal total =
        limits.values().stream().filter(Objects::nonNull).reduce(ZERO, BigDecimal::add);

    if (total.compareTo(ZERO) <= 0) {
      return ratios;
    }

    limits.forEach(
        (category, amount) -> {
          if (category != null && amount != null) {
            ratios.put(category, amount.divide(total, 4, RoundingMode.HALF_UP));
          }
        });

    for (ConsumptionCategory category : ConsumptionCategory.values()) {
      ratios.putIfAbsent(category, ZERO);
    }

    return ratios;
  }

  private YearMonth resolveYearMonth(Integer year, Integer month) {
    YearMonth now = YearMonth.now();
    int resolvedYear = year != null ? year : now.getYear();
    int resolvedMonth = month != null ? month : now.getMonthValue();

    if (resolvedMonth < 1 || resolvedMonth > 12) {
      throw new IllegalArgumentException("월은 1~12 사이여야 합니다.");
    }
    return YearMonth.of(resolvedYear, resolvedMonth);
  }

  private Map<ConsumptionCategory, BigDecimal> buildSpendingRatio(
      Map<ConsumptionCategory, BigDecimal> spending, BigDecimal income) {

    Map<ConsumptionCategory, BigDecimal> ratios = new EnumMap<>(ConsumptionCategory.class);

    BigDecimal totalSpent = spending.values().stream().reduce(ZERO, BigDecimal::add);
    BigDecimal denominator = income != null && income.compareTo(ZERO) > 0 ? income : totalSpent;

    if (denominator.compareTo(ZERO) == 0) {
      denominator = BigDecimal.ONE;
    }

    for (ConsumptionCategory category : ConsumptionCategory.values()) {
      BigDecimal amount = spending.getOrDefault(category, ZERO);
      ratios.put(category, amount.divide(denominator, 4, RoundingMode.HALF_UP));
    }

    return ratios;
  }
}
