package com.fisa.bank.account.application.repository;

import java.util.List;
import java.util.Optional;

import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.user.entity.id.UserId;

public interface AccountRepository {

  Optional<Account> findSalaryAccount(UserId userId);

  List<Account> findByUserId(UserId userId);
}
