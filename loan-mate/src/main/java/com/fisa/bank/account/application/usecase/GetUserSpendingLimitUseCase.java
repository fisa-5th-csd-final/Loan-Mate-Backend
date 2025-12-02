package com.fisa.bank.account.application.usecase;

import java.util.Optional;

import com.fisa.bank.account.application.model.spending.UserSpendingLimit;

public interface GetUserSpendingLimitUseCase {

  Optional<UserSpendingLimit> execute();
}
