package com.fisa.bank.account.application.model.spending;

import java.math.BigDecimal;

import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record CategorySpending(
    ConsumptionCategory category, BigDecimal amount, BigDecimal percent) {}
