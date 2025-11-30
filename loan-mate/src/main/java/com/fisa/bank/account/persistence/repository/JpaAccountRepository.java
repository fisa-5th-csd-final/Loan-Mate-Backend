package com.fisa.bank.account.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.user.entity.id.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.account.repository.AccountRepository;
import com.fisa.bank.persistence.user.entity.User;

public interface JpaAccountRepository extends JpaRepository<Account, AccountId> {
  @Query("""
        select a from Account a
        where a.user.userId = :userId
    """)
  List<Account> findAllByUserId(@Param("userId") UserId userId);

  Optional<Account> findFirstByUserAndIsForIncomeTrue(User user);
}