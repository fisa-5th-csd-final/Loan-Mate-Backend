package com.fisa.bank.account.application.service.helper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.exception.SalaryAccountNotFoundException;
import com.fisa.bank.account.application.model.UserAccountContext;
import com.fisa.bank.account.application.repository.AccountRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.user.entity.User;
import com.fisa.bank.persistence.user.entity.id.UserId;
import com.fisa.bank.persistence.user.repository.UserRepository;
import com.fisa.bank.user.application.exception.ServiceUserNotFoundException;
import com.fisa.bank.user.application.model.ServiceUser;

@Component
@RequiredArgsConstructor
public class UserAccountContextService {

  private final RequesterInfo requesterInfo;
  private final UserRepository coreUserRepository;
  private final com.fisa.bank.user.application.repository.UserRepository serviceUserRepository;
  private final AccountRepository accountRepository;

  public UserAccountContext loadContext() {
    ServiceUser serviceUser = getServiceUser();
    User coreUser = getCoreUser();
    Account salaryAccount = findSalaryAccount(coreUser);
    return new UserAccountContext(serviceUser, coreUser, salaryAccount);
  }

  private ServiceUser getServiceUser() {
    Long serviceUserId = requesterInfo.getServiceUserId();
    return serviceUserRepository
        .findById(serviceUserId)
        .orElseThrow(ServiceUserNotFoundException::new);
  }

  private User getCoreUser() {
    Long coreUserId = requesterInfo.getCoreBankingUserId();
    return coreUserRepository
        .findById(UserId.of(coreUserId))
        .orElseThrow(ServiceUserNotFoundException::new);
  }

  private Account findSalaryAccount(User coreUser) {
    return accountRepository
        .findSalaryAccount(coreUser)
        .orElseThrow(() -> new SalaryAccountNotFoundException(coreUser.getUserId().getValue()));
  }
}
