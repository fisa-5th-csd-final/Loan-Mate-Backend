package com.fisa.bank.account.persistence.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.account.repository.AccountRepository;
import com.fisa.bank.persistence.user.entity.id.UserId;

public interface JpaAccountRepository extends AccountRepository {
  Optional<Account> findFirstByUser_UserIdAndIsForIncomeTrue(UserId userId);

  List<Account> findByUser_UserId(UserId userId);
}
