package com.fisa.bank.account.application.service;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.account.application.repository.AccountDetailRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.user.entity.id.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountDetailRepository accountDetailRepository;
    private final RequesterInfo requesterInfo;

    public AccountDetail getAccount(Long accountId) {
        Long userId = requesterInfo.getCoreBankingUserId();
        return accountDetailRepository.findAccountById(UserId.of(userId), accountId);
    }
}
