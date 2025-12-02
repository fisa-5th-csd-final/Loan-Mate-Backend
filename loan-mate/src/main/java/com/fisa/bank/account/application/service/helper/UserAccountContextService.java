package com.fisa.bank.account.application.service.helper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.exception.SalaryAccountNotFoundException;
import com.fisa.bank.account.application.model.UserAccountContext;
import com.fisa.bank.account.application.repository.AccountRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.user.entity.id.UserId;
import com.fisa.bank.user.application.exception.ServiceUserNotFoundException;
import com.fisa.bank.user.application.model.ServiceUser;
import com.fisa.bank.user.application.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserAccountContextService {

  private final RequesterInfo requesterInfo;
  private final UserRepository serviceUserRepository;
  private final AccountRepository accountRepository;

  /** ServiceUser + CoreUserId + SalaryAccount 로 구성된 Context 로드 */
  public UserAccountContext loadContext() {

    Long coreUserId = requesterInfo.getCoreBankingUserId();
    Long serviceUserId = requesterInfo.getServiceUserId();

    // ServiceUser 조회 (서비스 DB)
    ServiceUser serviceUser =
        serviceUserRepository
            .findById(serviceUserId)
            .orElseThrow(ServiceUserNotFoundException::new);

    // Salary Account 조회 (core user ID 기반)
    Account salaryAccount =
        accountRepository
            .findSalaryAccount(UserId.of(coreUserId))
            .orElseThrow(() -> new SalaryAccountNotFoundException(coreUserId));

    return new UserAccountContext(serviceUser, coreUserId, salaryAccount);
  }
}
