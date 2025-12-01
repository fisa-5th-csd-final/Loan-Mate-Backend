package com.fisa.bank.account.application.service.helper;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.model.spending.UserSpendingLimit;
import com.fisa.bank.account.application.usecase.GetUserSpendingLimitUseCase;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Component
@RequiredArgsConstructor
public class UserSpendingLimitResolver {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private final GetUserSpendingLimitUseCase getUserSpendingLimitUseCase;

  /** 사용자 설정 금액 한도를 불러온다. */
  public Optional<Map<ConsumptionCategory, BigDecimal>> findUserLimitAmounts() {
    return getUserSpendingLimitUseCase
        .execute()
        .map(UserSpendingLimit::limits)
        .map(this::normalizeAmounts)
        .filter(limits -> !limits.isEmpty());
  }

  /** 사용자 한도 금액을 비율로 변환해서 반환한다. 없을 경우 연령대별 바른 소비 비율을 불러옴 */
  public Map<ConsumptionCategory, BigDecimal> resolveLimitRatio(
      Map<ConsumptionCategory, BigDecimal> defaultRatio) {

    return findUserLimitAmounts()
        .map(this::convertAmountToRatio)
        .filter(ratios -> !ratios.isEmpty())
        .orElseGet(() -> fillMissing(copy(defaultRatio)));
  }

  /** 전달된 맵에 없는 카테고리는 0으로 채워서 반환. */
  public Map<ConsumptionCategory, BigDecimal> fillMissing(
      Map<ConsumptionCategory, BigDecimal> limits) {
    Map<ConsumptionCategory, BigDecimal> result = new EnumMap<>(ConsumptionCategory.class);
    if (limits != null) {
      result.putAll(limits);
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

  private Map<ConsumptionCategory, BigDecimal> convertAmountToRatio(
      Map<ConsumptionCategory, BigDecimal> limits) {

    BigDecimal total =
        limits.values().stream().filter(java.util.Objects::nonNull).reduce(ZERO, BigDecimal::add);

    if (total.compareTo(ZERO) <= 0) {
      return Map.of();
    }

    Map<ConsumptionCategory, BigDecimal> ratios = new EnumMap<>(ConsumptionCategory.class);
    limits.forEach(
        (category, amount) -> {
          if (category != null && amount != null) {
            ratios.put(category, amount.divide(total, 4, RoundingMode.HALF_UP));
          }
        });

    return fillMissing(ratios);
  }

  private Map<ConsumptionCategory, BigDecimal> copy(Map<ConsumptionCategory, BigDecimal> source) {
    if (source == null) {
      return new EnumMap<>(ConsumptionCategory.class);
    }
    return new EnumMap<>(source);
  }
}
