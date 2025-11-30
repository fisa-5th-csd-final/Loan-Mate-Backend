package com.fisa.bank.account.application.usecase;

import com.fisa.bank.account.application.model.spending.RecommendedSpending;

public interface GetRecommendedSpendingUseCase {

  RecommendedSpending execute(int year, int month);
}
