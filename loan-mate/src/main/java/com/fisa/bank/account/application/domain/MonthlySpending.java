package com.fisa.bank.account.application.domain;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySpending(
    int year, int month, BigDecimal totalSpent, List<CategorySpending> categories) {}
