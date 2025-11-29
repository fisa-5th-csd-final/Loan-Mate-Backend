package com.fisa.bank.account.application.repository;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.persistence.user.entity.id.UserId;

public interface AccountDetailRepository {
    AccountDetail findAccountById(UserId userId, Long accountId);
}
