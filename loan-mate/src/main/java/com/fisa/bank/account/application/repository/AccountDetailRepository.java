package com.fisa.bank.account.application.repository;

import java.util.List;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.persistence.user.entity.id.UserId;

public interface AccountDetailRepository {
  List<AccountDetail> findAccountsByUserId(UserId userId);
}
