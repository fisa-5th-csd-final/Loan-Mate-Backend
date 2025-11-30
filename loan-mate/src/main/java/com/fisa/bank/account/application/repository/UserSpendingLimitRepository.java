package com.fisa.bank.account.application.repository;

import java.util.Optional;

import com.fisa.bank.account.application.model.spending.UserSpendingLimit;

public interface UserSpendingLimitRepository {

  UserSpendingLimit save(UserSpendingLimit limit);

  Optional<UserSpendingLimit> findByServiceUserId(Long serviceUserId);
}
