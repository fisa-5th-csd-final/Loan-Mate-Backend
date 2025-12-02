package com.fisa.bank.account.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.account.application.repository.AccountDetailRepository;
import com.fisa.bank.account.persistence.repository.jpa.JpaAccountRepository;
import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Repository
@RequiredArgsConstructor
public class AccountDetailRepositoryImpl implements AccountDetailRepository {
  private final JpaAccountRepository jpaAccountRepository;

  @Override
  public List<AccountDetail> findAccountsByUserId(UserId userId) {
    return jpaAccountRepository.findByUser_UserId(userId).stream()
        .map(AccountDetailRepositoryImpl::toDomain)
        .toList();
  }

  private static AccountDetail toDomain(Account entity) {
    return new AccountDetail(
        entity.getAccountId().getValue(),
        entity.getAccountNumber(),
        entity.getBankCode(),
        entity.getBalance(),
        entity.getCreatedAt(),
        entity.isForIncome());
  }
}
