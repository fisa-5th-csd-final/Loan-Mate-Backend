package com.fisa.bank.account.persistence.repository;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.account.application.repository.AccountDetailRepository;
import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.user.entity.id.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountDetailRepositoryImpl implements AccountDetailRepository {
    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public AccountDetail findAccountById(UserId userId, Long accountId) {
        AccountId accountIdObj = AccountId.of(accountId);
        Account account = jpaAccountRepository.findByAccountIdAndUserId(accountIdObj, userId).orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다"));

        return toDomain(account);
    }

    private static AccountDetail toDomain(Account entity) {
        return new AccountDetail(
                entity.getAccountId().getValue(),
                entity.getAccountNumber(),
                entity.getBankCode(),
                entity.getBalance(),
                entity.getCreatedAt(),
                entity.isForIncome()
        );
    }
}
