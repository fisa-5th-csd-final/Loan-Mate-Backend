package com.fisa.bank.account.application.dto.request;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record SpendingLimitRequest(
    @JsonProperty("user_limit_ratio") Map<ConsumptionCategory, BigDecimal> userLimitRatio) {}
