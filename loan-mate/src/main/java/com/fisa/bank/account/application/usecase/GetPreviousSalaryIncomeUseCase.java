package com.fisa.bank.account.application.usecase;

import java.math.BigDecimal;
import java.time.YearMonth;

import com.fisa.bank.persistence.account.entity.id.AccountId;

public interface GetPreviousSalaryIncomeUseCase {

  BigDecimal execute(AccountId accountId, YearMonth targetMonth);
}
