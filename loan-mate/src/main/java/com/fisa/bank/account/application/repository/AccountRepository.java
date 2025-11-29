package com.fisa.bank.account.application.repository;

import java.util.Optional;

import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.user.entity.User;

public interface AccountRepository {

  Optional<Account> findSalaryAccount(User user);
}
