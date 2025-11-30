package com.fisa.bank.account.persistence.repository.impl;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fisa.bank.account.application.repository.AccountRepository;
import com.fisa.bank.account.persistence.repository.jpa.JpaAccountRepository;
import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.user.entity.User;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

  private final JpaAccountRepository jpaAccountRepository;

  @Override
  public Optional<Account> findSalaryAccount(User user) {
    return jpaAccountRepository.findFirstByUserAndIsForIncomeTrue(user);
  }
}
