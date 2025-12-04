package com.fisa.bank.account.application.service.ai;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.account.application.client.AccountAiClient;
import com.fisa.bank.account.application.dto.request.AiRecommendRequest;
import com.fisa.bank.account.application.dto.response.AiExpenditureResponse;
import com.fisa.bank.account.application.model.IncomeBreakdown;
import com.fisa.bank.account.application.model.UserAccountContext;
import com.fisa.bank.account.application.service.helper.IncomeCalculator;
import com.fisa.bank.account.application.service.helper.UserAccountContextService;
import com.fisa.bank.account.application.service.helper.UserSpendingLimitResolver;
import com.fisa.bank.account.application.util.SpendingRatioLoader;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Service
@RequiredArgsConstructor
@Transactional
public class AiExpenditureService {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private final AccountAiClient accountAiClient;
  private final IncomeCalculator.MonthlySpendingCalculator monthlySpendingCalculator;
  private final SpendingRatioLoader spendingRatioLoader;
  private final UserAccountContextService userAccountContextService;
  private final IncomeCalculator incomeCalculator;
  private final UserSpendingLimitResolver userSpendingLimitResolver;

  public AiExpenditureResponse requestExpenditure(Integer year, Integer month) {

    YearMonth targetMonth = resolveYearMonth(year, month);

    UserAccountContext userAccountContext = userAccountContextService.loadContext();

    Map<ConsumptionCategory, BigDecimal> ageGroupRatio =
        roundRatios(spendingRatioLoader.getRatios(userAccountContext.serviceUser().getBirthday()));
    Map<ConsumptionCategory, BigDecimal> userLimitRatio =
        roundRatios(userSpendingLimitResolver.resolveLimitRatio(ageGroupRatio));

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
    BigDecimal roundedIncome = roundValue(totalIncome);

    Map<ConsumptionCategory, BigDecimal> spendingRatio =
        roundRatios(buildSpendingRatio(spending, totalIncome));

    AiRecommendRequest aiRequest =
        new AiRecommendRequest(
            new AiRecommendRequest.SpendingRatio(spendingRatio, roundedIncome),
            ageGroupRatio,
            new AiRecommendRequest.UserLimitRatio(userLimitRatio));

    JsonNode recommendation = accountAiClient.fetchRecommendation(aiRequest);

    return new AiExpenditureResponse(recommendation);
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

  private Map<ConsumptionCategory, BigDecimal> roundRatios(
      Map<ConsumptionCategory, BigDecimal> ratios) {
    Map<ConsumptionCategory, BigDecimal> rounded = new EnumMap<>(ConsumptionCategory.class);
    if (ratios == null) {
      return rounded;
    }

    for (ConsumptionCategory category : ConsumptionCategory.values()) {
      BigDecimal value = ratios.getOrDefault(category, ZERO);
      rounded.put(category, value.setScale(1, RoundingMode.HALF_UP));
    }

    return rounded;
  }

  private BigDecimal roundValue(BigDecimal value) {
    if (value == null) {
      return ZERO.setScale(1, RoundingMode.HALF_UP);
    }
    return value.setScale(1, RoundingMode.HALF_UP);
  }
}
