package com.fisa.bank.account.application.usecase;

import com.fisa.bank.account.application.domain.MonthlySpending;

public interface GetMonthlySpendingUseCase {
  MonthlySpending execute(Long accountId);
}
