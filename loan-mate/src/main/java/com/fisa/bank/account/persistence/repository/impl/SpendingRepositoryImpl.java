package com.fisa.bank.account.persistence.repository.impl;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fisa.bank.account.application.repository.SpendingRepository;
import com.fisa.bank.account.persistence.repository.CategoryAmount;
import com.fisa.bank.account.persistence.repository.jpa.JpaAccountTransactionRepository;
import com.fisa.bank.account.persistence.repository.jpa.JpaCardTransactionRepository;
import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Repository
@RequiredArgsConstructor
public class SpendingRepositoryImpl implements SpendingRepository {

  private final JpaCardTransactionRepository cardRepo;
  private final JpaAccountTransactionRepository accountRepo;

  @Override
  public Map<ConsumptionCategory, BigDecimal> getMonthlySpending(
      Long accountId, int year, int month) {

    AccountId accId = AccountId.of(accountId);

    Map<ConsumptionCategory, BigDecimal> result = new HashMap<>();

    LocalDateTime startDate = LocalDate.of(year, month, 1).atStartOfDay();
    LocalDateTime endDate = startDate.plusMonths(1);

    List<CategoryAmount> list = cardRepo.sumByCategory(accId, startDate, endDate);

    for (CategoryAmount p : list) {
      result.put(p.getCategory(), p.getTotal());
    }

    BigDecimal etc = accountRepo.sumMonthEtc(accId, startDate, endDate);
    if (etc == null) {
      etc = BigDecimal.ZERO;
    }

    result.merge(ConsumptionCategory.ETC, etc, BigDecimal::add);

    return result;
  }
}
