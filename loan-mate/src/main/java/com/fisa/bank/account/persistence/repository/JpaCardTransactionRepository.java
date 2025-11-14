package com.fisa.bank.account.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fisa.bank.account.persistence.entity.CardTransaction;

public interface JpaCardTransactionRepository extends JpaRepository<CardTransaction, Long> {

  @Query(
      """
        SELECT t.category AS category, SUM(t.amount) AS total
        FROM CardTransactionEntity t
        WHERE t.accountId = :accountId
        AND YEAR(t.createdAt) = :year
        AND MONTH(t.createdAt) = :month
        GROUP BY t.category
    """)
  List<CategoryAmount> sumByCategory(Long accountId, int year, int month);
}
