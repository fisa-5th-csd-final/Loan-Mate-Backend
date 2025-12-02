package com.fisa.bank.account.application.model.spending;

import java.math.BigDecimal;
import java.util.Map;

import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record RecommendedSpending(
    BigDecimal variableSpendingBudget,
    Map<ConsumptionCategory, BigDecimal> categoryRecommendation) {}
