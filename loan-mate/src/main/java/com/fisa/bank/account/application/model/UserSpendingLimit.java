package com.fisa.bank.account.application.model;

import java.math.BigDecimal;
import java.util.Map;

import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record UserSpendingLimit(
    Long id, Long serviceUserId, Map<ConsumptionCategory, BigDecimal> limits) {}
