package com.fisa.bank.account.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.user.entity.id.UserId;

public interface JpaAccountRepository extends JpaRepository<Account, AccountId> {
  @Query(
      """
    select a from Account a
    where a.accountId = :accountId
      and a.user.userId = :userId
    """)
  Optional<Account> findByAccountIdAndUserId(
      @Param("accountId") AccountId accountId, @Param("userId") UserId userId);
}
