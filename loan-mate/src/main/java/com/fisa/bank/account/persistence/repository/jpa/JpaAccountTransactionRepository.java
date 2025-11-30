package com.fisa.bank.account.persistence.repository.jpa;

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

  // 계좌 거래 (급여 통장의 급여 확인)
  @Query(
      """
    SELECT COALESCE(SUM(a.amount), 0)
    FROM AccountTransaction a
    WHERE a.account.accountId = :accountId
      AND a.isIncome = true
      AND a.createdAt >= :startDate
      AND a.createdAt < :endDate
""")
  BigDecimal sumMonthlyIncome(
      @Param("accountId") AccountId accountId,
      @Param("startDate") java.time.LocalDateTime startDate,
      @Param("endDate") java.time.LocalDateTime endDate);
}
