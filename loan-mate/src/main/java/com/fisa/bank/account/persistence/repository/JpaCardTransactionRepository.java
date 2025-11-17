package com.fisa.bank.account.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fisa.bank.persistence.account.entity.CardTransaction;
import com.fisa.bank.persistence.account.entity.id.AccountId;

public interface JpaCardTransactionRepository extends JpaRepository<CardTransaction, Long> {

  @Query(
      """
        SELECT new com.fisa.bank.account.persistence.repository.CategoryAmount(
            t.category,
            SUM(t.amount)
        )
        FROM CardTransaction t
        WHERE t.account.accountId = :accountId
          AND YEAR(t.createdAt) = :year
          AND MONTH(t.createdAt) = :month
        GROUP BY t.category
    """)
  List<CategoryAmount> sumByCategory(
      @Param("accountId") AccountId accountId, @Param("year") int year, @Param("month") int month);
}
