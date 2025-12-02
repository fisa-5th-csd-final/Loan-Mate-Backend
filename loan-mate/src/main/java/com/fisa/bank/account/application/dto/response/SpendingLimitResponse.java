package com.fisa.bank.account.application.dto.response;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record SpendingLimitResponse(
    @JsonProperty("user_limit_amount") Map<ConsumptionCategory, BigDecimal> userLimitAmount) {}
