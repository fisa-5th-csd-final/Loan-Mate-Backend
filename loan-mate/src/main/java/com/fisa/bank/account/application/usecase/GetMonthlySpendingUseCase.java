package com.fisa.bank.account.application.usecase;

import com.fisa.bank.account.application.model.MonthlySpending;

public interface GetMonthlySpendingUseCase {
  MonthlySpending execute(Long accountId, int year, int month);
}
