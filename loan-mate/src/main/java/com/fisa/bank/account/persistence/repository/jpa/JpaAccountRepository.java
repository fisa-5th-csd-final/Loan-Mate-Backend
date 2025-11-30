package com.fisa.bank.account.persistence.repository.jpa;

import java.util.Optional;

import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.account.repository.AccountRepository;
import com.fisa.bank.persistence.user.entity.User;

public interface JpaAccountRepository extends AccountRepository {
  Optional<Account> findFirstByUserAndIsForIncomeTrue(User user);
}
