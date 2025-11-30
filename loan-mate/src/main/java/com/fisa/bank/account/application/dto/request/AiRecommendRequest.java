package com.fisa.bank.account.application.dto.request;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record AiRecommendRequest(
    @JsonProperty("spending_ratio") SpendingRatio spendingRatio,
    @JsonProperty("age_group_ratio") Map<ConsumptionCategory, BigDecimal> ageGroupRatio,
    @JsonProperty("user_limit_ratio") UserLimitRatio userLimitRatio) {

  public record SpendingRatio(Map<ConsumptionCategory, BigDecimal> categories, BigDecimal income) {}

  public record UserLimitRatio(Map<ConsumptionCategory, BigDecimal> limits) {}
}
