package com.fisa.bank.account.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fisa.bank.account.application.repository.SpendingRepository;
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

    List<CategoryAmount> list = cardRepo.sumByCategory(accId, year, month);

    for (CategoryAmount p : list) {
      result.put(p.getCategory(), p.getTotal());
    }

    BigDecimal etc = accountRepo.sumMonthEtc(accId, year, month);
    if (etc == null) etc = BigDecimal.ZERO; // null이면 0으로 대체

    result.merge(ConsumptionCategory.ETC, etc, BigDecimal::add);

    return result;
  }
}
