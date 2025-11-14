package com.fisa.bank.account.application.repository;

import java.math.BigDecimal;
import java.util.Map;

import com.fisa.bank.account.application.domain.ConsumptionCategory;

public interface SpendingRepository {
  Map<ConsumptionCategory, BigDecimal> getMonthlySpending(Long accountId, int year, int month);
}
