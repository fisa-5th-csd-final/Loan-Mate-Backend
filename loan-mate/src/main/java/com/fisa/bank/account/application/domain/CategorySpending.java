package com.fisa.bank.account.application.domain;

import java.math.BigDecimal;

public record CategorySpending(ConsumptionCategory category, BigDecimal amount, int percent) {}
