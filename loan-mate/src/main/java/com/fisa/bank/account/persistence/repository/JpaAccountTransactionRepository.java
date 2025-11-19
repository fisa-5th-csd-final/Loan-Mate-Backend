package com.fisa.bank.account.persistence.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fisa.bank.persistence.account.entity.AccountTransaction;
import com.fisa.bank.persistence.account.entity.id.AccountId;

public interface JpaAccountTransactionRepository
    extends JpaRepository<AccountTransaction, AccountId> {

  @Query(
      """
    SELECT SUM(a.amount)
    FROM AccountTransaction a
    WHERE a.account.accountId = :accountId
      AND a.isIncome = false
      AND a.createdAt >= :startDate
      AND a.createdAt < :endDate
""")
  BigDecimal sumMonthEtc(
      @Param("accountId") AccountId accountId,
      @Param("startDate") java.time.LocalDateTime startDate,
      @Param("endDate") java.time.LocalDateTime endDate);
}
