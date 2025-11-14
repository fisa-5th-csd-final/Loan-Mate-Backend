package com.fisa.bank.account.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fisa.bank.account.application.domain.ConsumptionCategory;
import com.fisa.bank.account.application.repository.SpendingRepository;

@Repository
@RequiredArgsConstructor
public class SpendingRepositoryImpl implements SpendingRepository {

  private final JpaCardTransactionRepository cardRepo;
  private final JpaAccountTransactionRepository accountRepo;

  @Override
  public Map<ConsumptionCategory, BigDecimal> getMonthlySpending(
      Long accountId, int year, int month) {

    Map<ConsumptionCategory, BigDecimal> result = new HashMap<>();

    List<CategoryAmount> list = cardRepo.sumByCategory(accountId, year, month);

    for (CategoryAmount p : list) {
      result.put(p.getCategory(), p.getTotal());
    }

    BigDecimal etc = accountRepo.sumMonthEtc(accountId, year, month);
    if (etc == null) etc = BigDecimal.ZERO;

    result.put(ConsumptionCategory.ETC, etc);

    return result;
  }
}
