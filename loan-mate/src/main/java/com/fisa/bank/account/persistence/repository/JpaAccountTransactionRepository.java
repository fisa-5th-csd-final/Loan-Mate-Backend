package com.fisa.bank.account.persistence.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fisa.bank.account.persistence.entity.AccountTransaction;

public interface JpaAccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

  @Query(
      """
        SELECT SUM(a.amount)
        FROM AccountTransactionEntity a
        WHERE a.accountId = :accountId
        AND a.isIncome = false
        AND YEAR(a.createdAt) = :year
        AND MONTH(a.createdAt) = :month
    """)
  BigDecimal sumMonthEtc(Long accountId, int year, int month);
}
