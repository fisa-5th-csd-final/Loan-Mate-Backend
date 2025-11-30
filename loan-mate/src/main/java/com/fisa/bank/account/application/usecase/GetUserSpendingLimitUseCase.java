package com.fisa.bank.account.application.usecase;

import java.util.Optional;

import com.fisa.bank.account.application.model.UserSpendingLimit;

public interface GetUserSpendingLimitUseCase {

  Optional<UserSpendingLimit> execute();
}
