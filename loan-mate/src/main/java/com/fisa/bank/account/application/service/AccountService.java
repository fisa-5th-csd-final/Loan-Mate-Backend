package com.fisa.bank.account.application.service;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.account.application.repository.AccountDetailRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountDetailRepository accountDetailRepository;
  private final RequesterInfo requesterInfo;

  public List<AccountDetail> getAccounts() {
    Long userId = requesterInfo.getCoreBankingUserId();
    return accountDetailRepository.findAccountsByUserId(UserId.of(userId));
  }
}
